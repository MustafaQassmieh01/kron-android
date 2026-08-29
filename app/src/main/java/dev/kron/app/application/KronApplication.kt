package dev.kron.app.application

import android.app.Application
import dev.kron.app.application.settings.AppSettings
import dev.kron.app.services.kron.api.KronApiService
import dev.kron.app.services.kron.store.event.EventStorageService

class KronApplication : Application() {
    lateinit var appSettings: AppSettings
        private set
    lateinit var apiService: KronApiService
        private set
    lateinit var eventStorage: EventStorageService
        private set

    override fun onCreate() {
        super.onCreate()
        appSettings = AppSettings(this)
        apiService = KronApiService(this)
        eventStorage = EventStorageService(this)
        eventStorage.performAutomaticCleanup()
    }
}
