package com.example.quickstorephilippinesandroidapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.quickstorephilippinesandroidapp.databinding.ActivityMainBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Test Supabase connection
        testSupabaseConnection()

        // Record device installation
        insertDeviceInstallation()
    }

    private fun testSupabaseConnection() {
        lifecycleScope.launch {
            try {
                // Test if Supabase is accessible
                val supabaseUrl = MyApplication.supabase.supabaseUrl
                Log.d("Supabase", "✅ Supabase connected! URL: $supabaseUrl")

                // Show success message
                runOnUiThread {
                    Snackbar.make(
                        binding.drawerLayout,
                        "✅ Connected to Supabase!",
                        Snackbar.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("Supabase", "❌ Connection failed", e)
                runOnUiThread {
                    Snackbar.make(
                        binding.drawerLayout,
                        "❌ Supabase connection failed: ${e.message}",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("OK", {}).show()
                }
            }
        }
    }

    private fun insertDeviceInstallation() {
        lifecycleScope.launch {
            try {
                val deviceId = getUniqueDeviceId()
                Log.d("DeviceInstall", "📱 Device ID: $deviceId")

                // Try to insert directly - let Supabase handle duplicates
                // (You might want to add a unique constraint on device_id in your DB)
                val deviceInfo = mapOf(
                    "device_id" to deviceId,
                    "manufacturer" to android.os.Build.MANUFACTURER,
                    "model" to android.os.Build.MODEL,
                    "android_version" to "Android ${android.os.Build.VERSION.RELEASE}"
                )

                Log.d("DeviceInstall", "💾 Inserting device record...")
                MyApplication.supabase.postgrest["devices"].insert(deviceInfo)
                Log.d("DeviceInstall", "✅ SUCCESS: Device installation recorded!")

                runOnUiThread {
                    Snackbar.make(
                        binding.drawerLayout,
                        "✅ Device registered!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                // Check if it's a duplicate constraint error
                if (e.message?.contains("duplicate") == true) {
                    Log.d("DeviceInstall", "ℹ️ Device already registered")
                    runOnUiThread {
                        Snackbar.make(
                            binding.drawerLayout,
                            "ℹ️ Device already registered",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e("DeviceInstall", "❌ FAILED: Failed to record installation", e)
                    runOnUiThread {
                        Snackbar.make(
                            binding.drawerLayout,
                            "❌ Registration failed",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun getUniqueDeviceId(): String {
        // Simple approach - you might want to use a more robust method
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
            "unknown"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}