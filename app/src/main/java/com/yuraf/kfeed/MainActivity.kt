package com.yuraf.kfeed

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yuraf.kfeed.ui.fragment.main.MainFeedFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFeedFragment.newInstance())
                .commitNow()
        }
    }
}