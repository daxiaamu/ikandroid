package com.ikan.app

import android.app.Application
import com.ikan.app.data.AppPreferences
import com.ikan.app.data.CatalogCache
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
import java.io.File

class IKanApplication : Application() {
    private val startupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val database by lazy { IKanDatabase.create(this) }
    val repository by lazy {
        IKanRepository(
            remote = IkanClient(),
            daoProvider = { database.libraryDao() },
            homeCache = CatalogCache(File(filesDir, "home-catalog.json")),
        )
    }
    val preferences by lazy { AppPreferences(this) }
    val playbackEngine by lazy { PlaybackEngine(this) }

    lateinit var initialHome: Deferred<CatalogPage>
        private set
    lateinit var cachedHome: Deferred<CatalogPage?>
        private set

    override fun onCreate() {
        super.onCreate()
        cachedHome = startupScope.async { repository.cachedHome() }
        // Refresh in parallel; the ViewModel can render the last successful snapshot first.
        initialHome = startupScope.async { repository.catalog(HomeCategory.RECOMMEND) }
    }
}
