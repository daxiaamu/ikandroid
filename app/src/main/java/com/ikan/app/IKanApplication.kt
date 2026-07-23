package com.ikan.app

import android.app.Application
import com.ikan.app.data.AppPreferences
import com.ikan.app.data.IKanDatabase
import com.ikan.app.data.IKanRepository
import com.ikan.app.data.IkanClient
import com.ikan.app.model.CatalogPage
import com.ikan.app.model.HomeCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class IKanApplication : Application() {
    private val startupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val database by lazy { IKanDatabase.create(this) }
    val repository by lazy { IKanRepository(IkanClient(), database.libraryDao()) }
    val preferences by lazy { AppPreferences(this) }
    val playbackEngine by lazy { PlaybackEngine(this) }

    lateinit var initialHome: Deferred<CatalogPage>
        private set

    override fun onCreate() {
        super.onCreate()
        // Start the first network request while Android is still drawing its system launch window.
        initialHome = startupScope.async { repository.catalog(HomeCategory.RECOMMEND) }
        // Open the segment cache off the UI thread before the first detail navigation needs it.
        startupScope.launch { playbackEngine }
    }
}
