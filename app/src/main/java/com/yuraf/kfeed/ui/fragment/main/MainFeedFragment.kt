package com.yuraf.kfeed.ui.fragment.main

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.yuraf.kfeed.BuildConfig
import com.yuraf.kfeed.R
import com.yuraf.kfeed.data.SharedPrefsRepository
import com.yuraf.kfeed.data.SharedPrefsRepositoryImpl.Companion.KEY_LOCATION_SERVICE_ENABLED
import com.yuraf.kfeed.databinding.MainFragmentBinding
import com.yuraf.kfeed.location.LocationTrackingService
import com.yuraf.kfeed.location.LocationUtils
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainFeedFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        fun newInstance() = MainFeedFragment()
    }

    private val viewModel: MainFeedViewModel by viewModel()
    private lateinit var binding: MainFragmentBinding

    private lateinit var locationTrackingBroadcastReceiver: LocationTrackingBroadcastReceiver
    private var locationTrackingService: LocationTrackingService? = null
    private var locationServiceBound = false

    private val sharedPreferences: SharedPreferences by inject()
    private val sharedPrefsRepository: SharedPrefsRepository by inject()

    private val photoAdapter by lazy { PhotoAdapter { searchPhoto ->
        searchPhoto.localLocation?.let { location ->
            viewModel.loadImage(location)
        } }
    }

    private val locationServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationTrackingService.LocalBinder
            locationTrackingService = binder.service
            locationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationTrackingService = null
            locationServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationTrackingBroadcastReceiver = LocationTrackingBroadcastReceiver()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = MainFragmentBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            feed.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            feed.adapter = photoAdapter

            actionButton.setOnClickListener {
                if (sharedPrefsRepository.isLocationServiceEnabled()) {
                    locationTrackingService?.unsubscribeToLocationUpdates()
                } else {
                    if (isProvidedFineLocation()) {
                        checkIsActivatedLocation()
                    } else {
                        requestFineLocationPermission()
                    }
                }
            }
        }

        viewModel.itemsInfo.observe(viewLifecycleOwner) { info ->
            val items = info.items
            val isScrollToTop = info.isAddedNew

            binding.emptyFeedText.isVisible = items.isEmpty()

            photoAdapter.submitList(items) {
                if (isScrollToTop) {
                    binding.feed.smoothScrollToPosition(0)
                }
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            binding.loaderBar.isVisible = it
        }

        viewModel.error.observe(viewLifecycleOwner) {
            Snackbar.make(binding.main, it, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        super.onStart()

        updateActionButtonState(sharedPrefsRepository.isLocationServiceEnabled())
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val serviceIntent = Intent(context, LocationTrackingService::class.java)
        requireActivity().bindService(serviceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationTrackingBroadcastReceiver,
            IntentFilter(LocationTrackingService.ACTION_LOCATION_BROADCAST)
        )

        viewModel.checkPhotos()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationTrackingBroadcastReceiver)

        super.onPause()
    }

    override fun onStop() {
        if (locationServiceBound) {
            requireActivity().unbindService(locationServiceConnection)
            locationServiceBound = false
        }

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onStop()
    }

    private fun updateActionButtonState(isStart: Boolean) {
        if (isStart) {
            binding.actionButton.text = getString(R.string.stop)
            binding.actionButton.icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_stop_circle_24)
        } else {
            binding.actionButton.text = getString(R.string.start)
            binding.actionButton.icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_play_circle_outline_24)
        }
    }

    /**
     * PERMISSIONS
     */
    private fun isProvidedFineLocation(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun requestFineLocationPermission() {
        permissionsResult.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val permissionsResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkIsActivatedLocation()
        } else {
            updateActionButtonState(false)

            Snackbar.make(binding.main,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.settings) {
                    showAppSettings()
                }.show()
        }
    }

    private fun checkIsActivatedLocation() {
        if (LocationUtils.isLocationEnabled(requireContext())) {
            locationTrackingService?.subscribeToLocationUpdates()
        } else {
            Snackbar.make(binding.main, R.string.turn_on_location, Snackbar.LENGTH_LONG)
                .setAction(R.string.location) {
                    openLocationSettings()
                }.show()
        }
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        if (key == KEY_LOCATION_SERVICE_ENABLED) {
            updateActionButtonState(prefs.getBoolean(KEY_LOCATION_SERVICE_ENABLED, false))
        }
    }

    /**
     * SETTINGS
     */
    private fun showAppSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
    }

    private fun openLocationSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
    }

    /**
     * BROADCAST RECEIVER
     */
    private inner class LocationTrackingBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(LocationTrackingService.EXTRA_LOCATION)

            location?.let {
                viewModel.loadImage(it)
            }
        }
    }
}