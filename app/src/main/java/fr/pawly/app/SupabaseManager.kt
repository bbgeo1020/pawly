package fr.pawly.app

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth //
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseManager {
    private const val SUPABASE_URL = "https://oshtvrwbdebwwqttwyoc.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9zaHR2cndiZGVid3dxdHR3eW9jIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQyNjUyMTksImV4cCI6MjA4OTg0MTIxOX0.0Ssk2xC7oHOOjSM5u6jlVjFsCe4m8DbkilFovpcv024"

    val client = createSupabaseClient(SUPABASE_URL, SUPABASE_ANON_KEY) {
        install(Auth) // 👈 Installe le module d'authentification
        install(Postgrest) // Installe le module de base de données SQL
    }
}