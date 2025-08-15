package com.example.quickstorephilippinesandroidapp

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.example.quickstorephilippinesandroidapp.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.ys.rkapi.MyManager

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    companion object {
        var CLIENT_ID: String? = null
        var DEVICE_ID: String? = null
        var onClientIdAvailable: (() -> Unit)? = null
        var myManager: MyManager? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "onCreate called")
        Log.d("MainActivity", "CLIENT_ID on start: $CLIENT_ID")
        Log.d("MainActivity", "DEVICE_ID: $DEVICE_ID")

        // Inflate the new layout (DrawerLayout is root)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ❌ No Toolbar anymore — remove this line
        // setSupportActionBar(binding.appBarMain.toolbar)

        // Initialize drawer and navigation
        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        // Configure drawer behavior
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_admin),
            drawerLayout
        )


        navView.setupWithNavController(navController)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = getColor(android.R.color.transparent)
        }

        // Initialize YFACE SDK
        initYFaceSdk()
    }

    private fun initYFaceSdk() {
        try {
            myManager = MyManager.getInstance(this)
            myManager?.bindAIDLService(this)

            myManager?.setConnectClickInterface(object : MyManager.ServiceConnectedInterface {
                override fun onConnect() {
                    Log.d("YFACE SDK", "✅ AIDL Service connected!")
                    myManager?.gpioManager?.pullUpWhiteLight()
                }
            })
        } catch (e: Exception) {
            Log.e("YFACE SDK", "❌ Failed to initialize YFACE SDK", e)
        }
    }

    override fun onDestroy() {
        myManager?.unBindAIDLService(this)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @SuppressLint("HardwareIds")
    private fun getUniqueDeviceId(): String {
        return android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown_device_id"
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to get app version", e)
            "unknown"
        }
    }
}