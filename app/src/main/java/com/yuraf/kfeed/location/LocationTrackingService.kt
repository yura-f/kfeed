package com.yuraf.kfeed.location

import android.app.*
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.yuraf.kfeed.MainActivity
import com.yuraf.kfeed.R
import com.yuraf.kfeed.data.SharedPrefsRepository
import com.yuraf.kfeed.logic.SearchAction
import com.yuraf.kfeed.logic.SearchRepository
import com.yuraf.kfeed.utils.ioScheduler
import com.yuraf.kfeed.utils.toText
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * @author Yura F (yura-f.github.io)
 */
class LocationTrackingService : Service() {
    companion object {
        const val ACTION_LOCATION_BROADCAST = "ACTION_LOCATION_BROADCAST"
        const val EXTRA_LOCATION = "EXTRA_LOCATION"
        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION = "EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"

        private const val NOTIFICATION_ID = 88910131
        private const val NOTIFICATION_CHANNEL_ID = "kfeed_channel_13"
    }

    private var configurationChange = false
    private var serviceRunningInForeground = false
    private val localBinder = LocalBinder()

    private lateinit var notificationManager: NotificationManager

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var currentLocation: Location? = null

    private val sharedPrefsRepository: SharedPrefsRepository by inject()
    private val searchRepository: SearchRepository by inject()

    private val compositeDisposable = CompositeDisposable()
    private val actions: PublishSubject<SearchAction> = PublishSubject.create()

    inner class LocalBinder : Binder() {
        internal val service: LocationTrackingService
            get() = this@LocationTrackingService
    }

    override fun onCreate() {
        Timber.d("onCreate()")

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(60)
            fastestInterval = TimeUnit.SECONDS.toMillis(30)
            smallestDisplacement = 100f

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        compositeDisposable.add(ioScheduler(searchRepository.searchPhoto(actions)).subscribe())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                currentLocation = locationResult.lastLocation

                Timber.d("currentLocation = " + currentLocation?.toText())

                val intent = Intent(ACTION_LOCATION_BROADCAST)
                intent.putExtra(EXTRA_LOCATION, currentLocation)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                if (serviceRunningInForeground) {
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        generateNotification(currentLocation)
                    )

                    currentLocation?.let { actions.onNext(SearchAction.LoadPhoto(it)) }
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                Timber.d("isLocationAvailable = " + locationAvailability.isLocationAvailable)
                super.onLocationAvailability(locationAvailability)
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand()")

        val cancelLocationTrackingFromNotification =
            intent.getBooleanExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false)

        if (cancelLocationTrackingFromNotification) {
            unsubscribeToLocationUpdates()
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        Timber.d("onBind()")

        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false

        return localBinder
    }

    override fun onRebind(intent: Intent) {
        Timber.d("onRebind()")

        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false

        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Timber.d("onUnbind()")

        if (!configurationChange) {
            Timber.d("Start foreground service")
            val notification = generateNotification(currentLocation)
            startForeground(NOTIFICATION_ID, notification)

            serviceRunningInForeground = true
        }

        return true
    }

    override fun onDestroy() {
        sharedPrefsRepository.saveLocationServiceEnabled(false)

        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        configurationChange = true
    }

    fun subscribeToLocationUpdates() {
        Timber.d("subscribeToLocationUpdates()")

        sharedPrefsRepository.saveLocationServiceEnabled(true)

        startService(Intent(applicationContext, LocationTrackingService::class.java))

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (unlikely: SecurityException) {
            sharedPrefsRepository.saveLocationServiceEnabled(false)
            Timber.e("Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    fun unsubscribeToLocationUpdates() {
        Timber.d("unsubscribeToLocationUpdates()")

        try {
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("Location Callback removed.")
                    stopSelf()
                } else {
                    Timber.d("Failed to remove Location Callback.")
                }
            }
            sharedPrefsRepository.saveLocationServiceEnabled(false)
        } catch (unlikely: SecurityException) {
            sharedPrefsRepository.saveLocationServiceEnabled(true)

            Timber.e("Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    private fun generateNotification(location: Location?): Notification {
        Timber.d("generateNotification()")

        val mainNotificationText = location?.toText() ?: getString(R.string.no_current_location)
        val titleText = getString(R.string.your_last_location)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                titleText,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)

        val launchActivityIntent = Intent(this, MainActivity::class.java)

        val cancelIntent = Intent(this, LocationTrackingService::class.java)
        cancelIntent.putExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, true)

        val servicePendingIntent =
            PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val activityPendingIntent = PendingIntent.getActivity(this, 0, launchActivityIntent, 0)

        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)

        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(0, getString(R.string.launch_app), activityPendingIntent)
            .addAction(0, getString(R.string.stop), servicePendingIntent)
            .build()
    }
}