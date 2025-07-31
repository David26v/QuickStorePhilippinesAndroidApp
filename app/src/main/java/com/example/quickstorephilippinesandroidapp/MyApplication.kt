package com.example.quickstorephilippinesandroidapp

import android.app.Application
import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.gotrue.Auth

class MyApplication : Application() {
    companion object {
        lateinit var supabase: SupabaseClient
    }

    override fun onCreate() {
        super.onCreate()

        try {
            supabase = createSupabaseClient(
                supabaseUrl = "https://wwadijlmxgujiykjdfnf.supabase.co",
                supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind3YWRpamxteGd1aml5a2pkZm5mIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTM4NTg3MDgsImV4cCI6MjA2OTQzNDcwOH0.0zKrufvmUbNsA6Bk9_nhIbWuiffQHmdhfTnMIZ9z0cU"
            ) {
                install(Postgrest)
                install(Auth)
            }
            Log.d("Supabase", "✅ Supabase initialized successfully")
        } catch (e: Exception) {
            Log.e("Supabase", "❌ Failed to initialize Supabase", e)
        }
    }
}