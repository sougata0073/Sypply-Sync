package com.sougata.supplysync.cloud

import com.sougata.supplysync.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage


object SupabaseClient {

    private const val SUPABASE_URL = "https://irninnfvzfgtcixjgxfz.supabase.co"
    private const val SUPABASE_API_KEY = BuildConfig.SUPABASE_API_KEY

    val client = createSupabaseClient(
        supabaseUrl = this.SUPABASE_URL,
        supabaseKey = this.SUPABASE_API_KEY
    ) {
        install(Storage)
    }

}