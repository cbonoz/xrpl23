package com.startupsurveys

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.startupsurveys.ui.home.HomeFragment
import com.startupsurveys.util.PrefManager


class MainActivity : FragmentActivity() {

    lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefManager = PrefManager(this)
        navigateTo(HomeFragment())
    }

    fun navigateTo(fragment: Fragment, bundle: Bundle = bundleOf()) {
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment, fragment)
            .commit()
    }


}