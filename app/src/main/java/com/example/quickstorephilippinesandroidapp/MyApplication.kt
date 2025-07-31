package com.example.quickstorephilippinesandroidapp

import android.app.Application
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

        supabase = createSupabaseClient(
            supabaseUrl = "https://wwadijlmxgujiykjdfnf.supabase.co",
            supabaseKey = "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind3YWRpamxteGd1aml5a2pkZm5mIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTM4NTg3MDgsImV4cCI6MjA2OTQzNDcwOH0"
        ) {
            install(Postgrest)
            install(Auth)
        }
    }
}