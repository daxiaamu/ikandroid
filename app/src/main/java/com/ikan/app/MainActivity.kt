package com.ikan.app

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.ViewGroup
import android.view.View
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import com.ikan.app.cast.DlnaController
import com.ikan.app.cast.DlnaDevice
import com.ikan.app.data.LibraryEntity
import com.ikan.app.model.CatalogPage
import com.ikan.app.model.HomeCategory
import com.ikan.app.model.PlayEpisode
import com.ikan.app.model.PlayLine
import com.ikan.app.model.ThemeMode
import com.ikan.app.model.Video
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val pipState = mutableStateOf(false)
    private val pipReturningState = mutableStateOf(false)
    private var pipSourceRect: Rect? = null
    private var autoPipEnabled = false
    private var pipReturnJob: Job? = null
    private var playbackPlayer: ExoPlayer? = null
    private val warmPlayback = Runnable { obtainPlaybackPlayer() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as IKanApplication
            val viewModel: MainViewModel = viewModel(factory = MainViewModel.factory(app))
            val theme by viewModel.theme.collectAsStateWithLifecycle()
            IKanTheme(theme) {
                IKanApp(
                    viewModel = viewModel,
                    isPip = pipState.value,
                    isPipReturning = pipReturningState.value,
                    enterPip = ::enterVideoPip,
                    configureAutoPip = ::configureAutoPip,
                )
            }
        }
        // ExoPlayer construction is main-thread work. Do it after the first home frame, not on
        // the first frame of the shared-element transition into detail.
        window.decorView.postDelayed(warmPlayback, 700L)
    }

    fun obtainPlaybackPlayer(): ExoPlayer = playbackPlayer ?: synchronized(this) {
        playbackPlayer ?: (application as IKanApplication).playbackEngine.createPlayer().also {
            playbackPlayer = it
        }
    }

    override fun onDestroy() {
        window.decorView.removeCallbacks(warmPlayback)
        playbackPlayer?.release()
        playbackPlayer = null
        super.onDestroy()
    }

    override fun onPictureInPictureModeChanged(inPip: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(inPip, newConfig)
        pipState.value = inPip
        pipReturnJob?.cancel()
        if (inPip) {
            pipReturningState.value = false
        } else {
            // Keep the video surface visually stable until the system has finished expanding PiP.
            pipReturningState.value = true
            pipReturnJob = lifecycleScope.launch {
                delay(320)
                pipReturningState.value = false
            }
        }
    }

    private fun configureAutoPip(enabled: Boolean) {
        autoPipEnabled = enabled
        setPictureInPictureParams(buildPipParams())
    }

    private fun enterVideoPip() {
        enterPictureInPictureMode(buildPipParams())
    }

    fun updatePipSourceRect(view: View) {
        if (isInPictureInPictureMode) return
        val rect = Rect()
        if (view.isAttachedToWindow && view.getGlobalVisibleRect(rect) && !rect.isEmpty) {
            pipSourceRect = rect
            setPictureInPictureParams(buildPipParams())
        }
    }

    private fun buildPipParams(): PictureInPictureParams {
        val builder = PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9))
        pipSourceRect?.let(builder::setSourceRectHint)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder
                .setAutoEnterEnabled(autoPipEnabled)
                .setSeamlessResizeEnabled(true)
        }
        return builder.build()
    }
}

private enum class MainTab(val label: String, val icon: ImageVector) {
    HOME("首页", Icons.Default.Home),
    FAVORITES("收藏", Icons.Default.Favorite),
    HISTORY("播放历史", Icons.Default.History),
    SETTINGS("设置", Icons.Default.Settings),
}

private data class Playing(val line: PlayLine, val episode: PlayEpisode, val startPosition: Long = 0)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun IKanApp(
    viewModel: MainViewModel,
    isPip: Boolean,
    isPipReturning: Boolean,
    enterPip: () -> Unit,
    configureAutoPip: (Boolean) -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    var detailVideo by remember { mutableStateOf<Video?>(null) }
    var frozenHistory by remember { mutableStateOf<List<LibraryEntity>?>(null) }
    val appScope = rememberCoroutineScope()

    fun openVideo(video: Video) {
        detailVideo = video
        detailId = video.id
        viewModel.loadDetail(video.id)
    }

    SharedTransitionLayout {
        AnimatedContent(
            targetState = detailId,
            transitionSpec = {
                if (targetState == null) {
                    fadeIn(tween(180)) togetherWith fadeOut(tween(1))
                } else {
                    fadeIn(tween(220)) togetherWith fadeOut(tween(1))
                }
            },
            label = "影片详情过渡",
        ) { id ->
            val sharedPosterModifier: @Composable (Video) -> Modifier = { video ->
                Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState("poster-${video.id}"),
                    animatedVisibilityScope = this@AnimatedContent,
                    boundsTransform = { _, _ -> tween(320) },
                    placeholderSize = SharedTransitionScope.PlaceholderSize.ContentSize,
                )
            }
            val sharedTitleModifier: @Composable (Video) -> Modifier = { video ->
                Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState("title-${video.id}"),
                    animatedVisibilityScope = this@AnimatedContent,
                    boundsTransform = { _, _ -> tween(320) },
                    placeholderSize = SharedTransitionScope.PlaceholderSize.ContentSize,
                )
            }
            val navigationOverlayModifier = Modifier.renderInSharedTransitionScopeOverlay(
                zIndexInOverlay = 10f,
                renderInOverlay = { detailId == null && isTransitionActive },
            )
            if (id != null) {
                DetailRoute(
                    videoId = id,
                    sourceVideo = detailVideo,
                    posterModifier = sharedPosterModifier,
                    titleModifier = sharedTitleModifier,
                    viewModel = viewModel,
                    isPip = isPip,
                    isPipReturning = isPipReturning,
                    enterPip = enterPip,
                    configureAutoPip = configureAutoPip,
                    onBack = {
                        detailId = null
                        // Keep the outgoing detail tree alive until its poster/title have reached
                        // the retained catalog card. Clearing it immediately removes the source.
                        appScope.launch {
                            delay(340)
                            if (detailId == null) {
                                viewModel.closeDetail()
                                frozenHistory = null
                            }
                        }
                    },
                )
            } else {
                MainTabs(
                    selected = tab,
                    onSelected = { tab = it },
                    navigationModifier = navigationOverlayModifier,
                ) { padding ->
                    when (tab) {
                        MainTab.HOME -> HomeScreen(viewModel, padding, ::openVideo, sharedPosterModifier, sharedTitleModifier)
                        MainTab.FAVORITES -> {
                            val favorites by viewModel.favorites.collectAsStateWithLifecycle()
                            LibraryScreen(
                                title = "我的收藏",
                                emptyText = "还没有收藏影片",
                                entries = favorites,
                                modifier = Modifier.padding(padding),
                                onVideo = ::openVideo,
                                posterModifier = sharedPosterModifier,
                                titleModifier = sharedTitleModifier,
                            )
                        }
                        MainTab.HISTORY -> {
                            val history by viewModel.history.collectAsStateWithLifecycle()
                            LibraryScreen(
                                title = "播放历史",
                                emptyText = "还没有播放记录",
                                entries = frozenHistory ?: history,
                                modifier = Modifier.padding(padding),
                                onVideo = { video ->
                                    frozenHistory = history
                                    openVideo(video)
                                },
                                posterModifier = sharedPosterModifier,
                                titleModifier = sharedTitleModifier,
                                onClear = viewModel::clearHistory,
                            )
                        }
                        MainTab.SETTINGS -> SettingsScreen(
                            theme = viewModel.theme.collectAsStateWithLifecycle().value,
                            modifier = Modifier.padding(padding),
                            onTheme = viewModel::setTheme,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainTabs(
    selected: MainTab,
    onSelected: (MainTab) -> Unit,
    navigationModifier: Modifier = Modifier,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 840.dp
        if (wide) {
            Row(Modifier.fillMaxSize()) {
                NavigationRail(navigationModifier.fillMaxHeight().navigationBarsPadding().statusBarsPadding()) {
                    MainTab.entries.forEach { tab ->
                        NavigationRailItem(
                            selected = selected == tab,
                            onClick = { onSelected(tab) },
                            icon = { Icon(tab.icon, null) },
                            label = { Text(tab.label) },
                        )
                    }
                }
                Box(Modifier.weight(1f)) {
                    content(androidx.compose.foundation.layout.PaddingValues())
                }
            }
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar(navigationModifier) {
                        MainTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = selected == tab,
                                onClick = { onSelected(tab) },
                                icon = { Icon(tab.icon, null) },
                                label = { Text(tab.label) },
                            )
                        }
                    }
                },
                content = content,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    viewModel: MainViewModel,
    padding: androidx.compose.foundation.layout.PaddingValues,
    onVideo: (Video) -> Unit,
    posterModifier: @Composable (Video) -> Modifier,
    titleModifier: @Composable (Video) -> Modifier,
) {
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val searching by viewModel.searching.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var query by rememberSaveable { mutableStateOf("") }
    var searchFocused by remember { mutableStateOf(false) }

    fun submitSearch() {
        if (query.isNotBlank()) {
            viewModel.search(query)
            focusManager.clearFocus()
        }
    }

    Column(Modifier.fillMaxSize().padding(padding).statusBarsPadding()) {
        Text("爱看", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .onFocusChanged { searchFocused = it.isFocused }
                .onPreviewKeyEvent { event ->
                    if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
                        submitSearch()
                        true
                    } else false
                },
            singleLine = true,
            placeholder = { Text("搜索影片、短剧、演职人员") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { submitSearch() }),
            trailingIcon = {
                IconButton(onClick = { submitSearch() }, enabled = query.isNotBlank()) {
                    Icon(Icons.Default.Search, "搜索")
                }
            },
        )
        if (searchFocused && searchHistory.isNotEmpty()) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("搜索历史", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    TextButton(onClick = viewModel::clearSearchHistory) { Text("清除") }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    searchHistory.forEach { history ->
                        AssistChip(
                            onClick = { query = history; viewModel.search(history); focusManager.clearFocus() },
                            label = { Text(history, maxLines = 1) },
                        )
                    }
                }
            }
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(HomeCategory.entries) { item ->
                FilterChip(
                    selected = !searching && category == item,
                    onClick = { viewModel.loadCategory(item) },
                    label = { Text(item.label) },
                )
            }
        }
        CatalogContent(
            state = catalog,
            onVideo = onVideo,
            posterModifier = posterModifier,
            titleModifier = titleModifier,
            onFilter = viewModel::loadPath,
            onNext = { path -> viewModel.loadPath(path, catalog.page?.title ?: category.label) },
            onRetry = { viewModel.loadCategory(category) },
        )
    }
}

@Composable
private fun CatalogContent(
    state: CatalogUiState,
    onVideo: (Video) -> Unit,
    posterModifier: @Composable (Video) -> Modifier,
    titleModifier: @Composable (Video) -> Modifier,
    onFilter: (String, String) -> Unit,
    onNext: (String) -> Unit,
    onRetry: () -> Unit,
) {
    when {
        state.loading && state.page == null -> CenterLoading()
        state.error != null -> ErrorState(state.error, onRetry)
        state.page != null -> {
            val page = state.page
            if (page.sections.isNotEmpty()) {
                LazyColumn(Modifier.fillMaxSize()) {
                    page.sections.forEach { section ->
                        item { SectionTitle(section.title) }
                        item {
                            LazyRow(
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(section.videos, key = { it.id }) {
                                    VideoCard(it, onVideo, Modifier.width(116.dp), posterModifier, titleModifier)
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            } else {
                CatalogGrid(page, state.loading, onVideo, posterModifier, titleModifier, onFilter, onNext)
            }
        }
    }
}

@Composable
private fun CatalogGrid(
    page: CatalogPage,
    loading: Boolean,
    onVideo: (Video) -> Unit,
    posterModifier: @Composable (Video) -> Modifier,
    titleModifier: @Composable (Video) -> Modifier,
    onFilter: (String, String) -> Unit,
    onNext: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        if (page.filters.isNotEmpty()) {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(page.filters) { filter ->
                    FilterChip(
                        selected = filter.selected,
                        onClick = { if (!filter.selected) onFilter(filter.path, filter.label) },
                        label = { Text(filter.label) },
                    )
                }
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(112.dp),
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(page.videos, key = { it.id }) {
                VideoCard(it, onVideo, posterModifier = posterModifier, titleModifier = titleModifier)
            }
            page.nextPath?.let { path ->
                item {
                    Button(onClick = { onNext(path) }, enabled = !loading) {
                        Text(if (loading) "加载中" else "下一页")
                    }
                }
            }
        }
    }
}

private fun posterRequest(context: Context, url: String): ImageRequest {
    val headers = NetworkHeaders.Builder()
        .set("User-Agent", "Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 Chrome/124 Mobile Safari/537.36")
    if (Uri.parse(url).host.orEmpty().contains("doubanio.com")) {
        headers.set("Referer", "https://movie.douban.com/")
    }
    return ImageRequest.Builder(context)
        .data(url)
        .memoryCacheKey("poster:$url")
        .diskCacheKey("poster:$url")
        .httpHeaders(headers.build())
        .build()
}

@Composable
private fun PosterImage(
    url: String,
    description: String?,
    modifier: Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val request = remember(url) { posterRequest(context, url) }
    var failed by remember(url) { mutableStateOf(false) }
    Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = request,
            contentDescription = description,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            onSuccess = { failed = false },
            onError = { failed = true },
        )
        if (url.isBlank() || failed) Icon(Icons.Default.BrokenImage, null, Modifier.size(36.dp))
    }
}

@Composable
private fun VideoCard(
    video: Video,
    onVideo: (Video) -> Unit,
    modifier: Modifier = Modifier,
    posterModifier: @Composable (Video) -> Modifier = { Modifier },
    titleModifier: @Composable (Video) -> Modifier = { Modifier },
) {
    Column(modifier.clickable { onVideo(video) }) {
        Card(
            modifier = posterModifier(video),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(2.dp),
        ) {
            Box(Modifier.fillMaxWidth().aspectRatio(0.71f).background(MaterialTheme.colorScheme.surfaceVariant)) {
                PosterImage(video.poster, video.title, Modifier.fillMaxSize())
            }
        }
        Text(
            video.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = titleModifier(video).padding(top = 6.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DetailRoute(
    videoId: String,
    sourceVideo: Video?,
    posterModifier: @Composable (Video) -> Modifier,
    titleModifier: @Composable (Video) -> Modifier,
    viewModel: MainViewModel,
    isPip: Boolean,
    isPipReturning: Boolean,
    enterPip: () -> Unit,
    configureAutoPip: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.detail.collectAsStateWithLifecycle()
    val activity = LocalContext.current as MainActivity
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    var playing by remember(state.detail?.video?.id) { mutableStateOf<Playing?>(null) }
    var autoResumed by remember(state.detail?.video?.id) { mutableStateOf(false) }
    var enterTransitionSettled by remember(videoId) { mutableStateOf(false) }
    val player = remember(videoId) { activity.obtainPlaybackPlayer() }

    LaunchedEffect(videoId) {
        delay(340)
        enterTransitionSettled = true
    }

    DisposableEffect(player) {
        onDispose {
            configureAutoPip(false)
            player.stop()
            player.clearMediaItems()
        }
    }

    LaunchedEffect(state.detail, state.resume, enterTransitionSettled) {
        if (!enterTransitionSettled) return@LaunchedEffect
        val detail = state.detail ?: return@LaunchedEffect
        if (!autoResumed) {
            val resume = state.resume
            if (resume?.streamUrl != null) {
                val line = detail.lines.firstOrNull { it.id == resume.lineId }
                    ?: PlayLine(resume.lineId.orEmpty(), "上次线路", emptyList())
                val episode = line.episodes.firstOrNull { it.url == resume.streamUrl }
                    ?: PlayEpisode(resume.episodeName ?: "继续播放", resume.streamUrl)
                playing = Playing(line, episode, resume.positionMs)
            } else {
                detail.lines.firstOrNull()?.let { line ->
                    line.episodes.firstOrNull()?.let { episode ->
                        playing = Playing(line, episode, 0)
                    }
                }
            }
            autoResumed = true
        }
    }

    val activePlaying = playing
    fun leaveDetail() {
        activePlaying?.let { current ->
            val position = if (player.currentMediaItem != null) {
                player.currentPosition.coerceAtLeast(0L)
            } else {
                current.startPosition
            }
            val duration = player.duration.takeUnless { it == C.TIME_UNSET || it < 0 }
                ?: state.resume?.durationMs
                ?: 0L
            viewModel.recordPlayback(
                current.line.id,
                current.episode.name,
                current.episode.url,
                position,
                duration,
            )
        }
        onBack()
    }

    BackHandler {
        if (isLandscape) activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        else leaveDetail()
    }

    LaunchedEffect(player, activePlaying?.episode?.url) {
        val current = activePlaying ?: return@LaunchedEffect
        if (player.currentMediaItem?.localConfiguration?.uri?.toString() != current.episode.url) {
            val item = MediaItem.Builder()
                .setUri(current.episode.url)
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(state.detail?.video?.title)
                        .setSubtitle(current.episode.name)
                        .build(),
                )
                .build()
            player.setMediaItem(item, current.startPosition)
            player.prepare()
            player.playWhenReady = true
        }
    }
    if (activePlaying != null && state.detail != null && isLandscape) {
        NativePlayer(
            player = player,
            title = state.detail!!.video.title,
            playing = activePlaying,
            fullscreen = true,
            modifier = Modifier.fillMaxSize(),
            isPip = isPip,
            isPipReturning = isPipReturning,
            enterPip = enterPip,
            configureAutoPip = configureAutoPip,
            onBack = { activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT },
            onProgress = { position, duration ->
                playing = activePlaying.copy(startPosition = position)
                viewModel.recordPlayback(
                    activePlaying.line.id,
                    activePlaying.episode.name,
                    activePlaying.episode.url,
                    position,
                    duration,
                )
            },
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (enterTransitionSettled) state.detail?.video?.title ?: sourceVideo?.title ?: "影片详情"
                        else sourceVideo?.title ?: "影片详情",
                    )
                },
                navigationIcon = {
                    IconButton(onClick = ::leaveDetail) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                },
                actions = {
                    if (enterTransitionSettled && state.detail != null) IconButton(onClick = {
                        viewModel.toggleFavorite { added ->
                            Toast.makeText(activity, if (added) "已收藏" else "已取消收藏", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            if (state.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            if (state.favorite) "取消收藏" else "收藏",
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            !enterTransitionSettled || state.loading -> {
                Column(Modifier.fillMaxSize().padding(padding)) {
                    // Reserve the final player bounds immediately so shared poster/title targets
                    // never move when detail data and the autoplay item arrive on later frames.
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(androidx.compose.ui.graphics.Color.Black),
                    )
                    if (sourceVideo != null) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            PosterImage(
                                sourceVideo.poster,
                                sourceVideo.title,
                                Modifier
                                    .width(116.dp)
                                    .aspectRatio(0.71f)
                                    .then(posterModifier(sourceVideo))
                                    .clip(RoundedCornerShape(8.dp)),
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    sourceVideo.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = titleModifier(sourceVideo),
                                )
                                CircularProgressIndicator(Modifier.size(28.dp))
                            }
                        }
                    } else {
                        CenterLoading(Modifier.weight(1f))
                    }
                }
            }
            state.error != null -> ErrorState(state.error!!, { viewModel.loadDetail(videoId) }, Modifier.padding(padding))
            state.detail != null -> {
                val detail = state.detail!!
                val posterVideo = sourceVideo ?: detail.video
                Column(Modifier.fillMaxSize().padding(padding)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(androidx.compose.ui.graphics.Color.Black),
                    ) {
                        activePlaying?.let { current ->
                            NativePlayer(
                                player = player,
                                title = detail.video.title,
                                playing = current,
                                fullscreen = false,
                                modifier = Modifier.fillMaxSize(),
                                isPip = isPip,
                                isPipReturning = isPipReturning,
                                enterPip = enterPip,
                                configureAutoPip = configureAutoPip,
                                onBack = {},
                                onProgress = { position, duration ->
                                    playing = current.copy(startPosition = position)
                                    viewModel.recordPlayback(
                                        current.line.id,
                                        current.episode.name,
                                        current.episode.url,
                                        position,
                                        duration,
                                    )
                                },
                            )
                        }
                    }
                    LazyColumn(
                        Modifier.fillMaxWidth().weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 14.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        item {
                        Row(
                            Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            PosterImage(
                                posterVideo.poster,
                                null,
                                Modifier
                                    .width(116.dp)
                                    .aspectRatio(0.71f)
                                    .then(posterModifier(posterVideo))
                                    .clip(RoundedCornerShape(8.dp)),
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(
                                    detail.video.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = titleModifier(posterVideo),
                                )
                                detail.video.subtitle.takeIf(String::isNotBlank)?.let { Text(it) }
                                Text(listOf(detail.video.year, detail.video.area).filter(String::isNotBlank).joinToString(" · "))
                                detail.video.actors.takeIf(String::isNotBlank)?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 4, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                    item {
                        Column(Modifier.padding(horizontal = 16.dp)) {
                            Text("播放线路", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("不同线路的清晰度和速度可能不同，播放不畅时可直接切换。", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (detail.lines.isEmpty()) item { Text("暂无可用线路", Modifier.padding(horizontal = 16.dp)) }
                    detail.lines.forEach { line ->
                        item {
                            Column(Modifier.padding(horizontal = 16.dp)) {
                                Text(line.name, style = MaterialTheme.typography.labelLarge)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                line.episodes.forEach { episode ->
                                    val selected = activePlaying?.line?.id == line.id &&
                                        activePlaying.episode.url == episode.url
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            if (!selected) {
                                                val current = activePlaying
                                                val currentIndex = current?.line?.episodes?.indexOfFirst {
                                                    it.url == current.episode.url
                                                } ?: -1
                                                val targetIndex = line.episodes.indexOfFirst { it.url == episode.url }
                                                val sameEpisode = current != null && (
                                                    current.episode.name == episode.name ||
                                                        (currentIndex >= 0 && currentIndex == targetIndex &&
                                                            current.line.episodes.size == line.episodes.size)
                                                    )
                                                val resumePosition = if (sameEpisode) {
                                                    player.currentPosition.coerceAtLeast(0L)
                                                } else {
                                                    0L
                                                }
                                                playing = Playing(line, episode, resumePosition)
                                            }
                                        },
                                        label = { Text(episode.name) },
                                        leadingIcon = { Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp)) },
                                    )
                                }
                                }
                            }
                        }
                    }
                }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NativePlayer(
    player: ExoPlayer,
    title: String,
    playing: Playing,
    fullscreen: Boolean,
    modifier: Modifier,
    isPip: Boolean,
    isPipReturning: Boolean,
    enterPip: () -> Unit,
    configureAutoPip: (Boolean) -> Unit,
    onBack: () -> Unit,
    onProgress: (Long, Long) -> Unit,
) {
    val context = LocalContext.current
    val activity = context as MainActivity
    val scope = rememberCoroutineScope()
    val dlna = remember { DlnaController() }
    var showCast by remember { mutableStateOf(false) }
    var devices by remember { mutableStateOf<List<DlnaDevice>>(emptyList()) }
    var discovering by remember { mutableStateOf(false) }
    var castError by remember { mutableStateOf<String?>(null) }
    var showPlaybackSettings by remember { mutableStateOf(false) }
    var gestureFeedback by remember { mutableStateOf<String?>(null) }
    var controlsVisible by remember(player) { mutableStateOf(true) }
    var buffering by remember(player) { mutableStateOf(player.playbackState == Player.STATE_BUFFERING) }
    var playbackError by remember(player) { mutableStateOf<PlaybackException?>(player.playerError) }
    val currentOnProgress by rememberUpdatedState(onProgress)
    fun saveProgress() {
        val duration = player.duration.takeUnless { it == C.TIME_UNSET } ?: 0
        currentOnProgress(player.currentPosition, duration)
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                buffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) playbackError = null
            }

            override fun onPlayerError(error: PlaybackException) {
                playbackError = error
                buffering = false
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(player) {
        configureAutoPip(true)
        while (true) {
            delay(10_000)
            saveProgress()
        }
    }
    DisposableEffect(player, fullscreen) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        if (fullscreen) {
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            saveProgress()
            configureAutoPip(false)
            if (fullscreen) controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    Surface(modifier, color = androidx.compose.ui.graphics.Color.Black) {
        Box(Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    (LayoutInflater.from(ctx).inflate(R.layout.player_view, null, false) as GesturePlayerView).apply {
                        this.player = player
                        onGestureFeedback = { gestureFeedback = it }
                        setControllerVisibilityListener(
                            PlayerView.ControllerVisibilityListener { visibility ->
                                controlsVisible = visibility == View.VISIBLE
                            },
                        )
                        useController = !isPip && !isPipReturning
                        setShowPreviousButton(false)
                        setShowNextButton(false)
                        setControllerShowTimeoutMs(if (fullscreen) 4_000 else 2_500)
                        findViewById<View>(androidx.media3.ui.R.id.exo_center_controls)?.apply {
                            val controlsScale = if (fullscreen) 1f else 0.72f
                            scaleX = controlsScale
                            scaleY = controlsScale
                        }
                        findViewById<View>(androidx.media3.ui.R.id.exo_settings)?.apply {
                            visibility = View.GONE
                        }
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                        keepScreenOn = true
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        post { activity.updatePipSourceRect(this) }
                    }
                },
                update = {
                    it.player = player
                    it.useController = !isPip && !isPipReturning
                    it.setShowPreviousButton(false)
                    it.setShowNextButton(false)
                    it.setControllerShowTimeoutMs(if (fullscreen) 4_000 else 2_500)
                    it.findViewById<View>(androidx.media3.ui.R.id.exo_center_controls)?.apply {
                        val controlsScale = if (fullscreen) 1f else 0.72f
                        scaleX = controlsScale
                        scaleY = controlsScale
                    }
                    it.findViewById<View>(androidx.media3.ui.R.id.exo_settings)?.apply {
                        visibility = View.GONE
                    }
                    it.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    if (!isPip && !isPipReturning) {
                        it.post { activity.updatePipSourceRect(it) }
                    }
                },
                onRelease = {
                    (it as? GesturePlayerView)?.onGestureFeedback = null
                    it.player = null
                },
                modifier = Modifier.fillMaxSize(),
            )
            gestureFeedback?.let { message ->
                Text(
                    text = message,
                    textAlign = TextAlign.Center,
                    color = androidx.compose.ui.graphics.Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.72f),
                            RoundedCornerShape(12.dp),
                        )
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                )
            }
            if (buffering && playbackError == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = androidx.compose.ui.graphics.Color.White,
                )
            }
            playbackError?.let {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("当前线路加载失败", color = androidx.compose.ui.graphics.Color.White)
                    Button(onClick = {
                        playbackError = null
                        player.prepare()
                        player.play()
                    }) { Text("重试") }
                }
            }
            if (!isPip && !isPipReturning && controlsVisible) {
                Row(
                    Modifier
                        .align(if (fullscreen) Alignment.TopCenter else Alignment.TopEnd)
                        .then(if (fullscreen) Modifier.fillMaxWidth() else Modifier)
                        .background(
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = if (fullscreen) 0.55f else 0.4f),
                            if (fullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(bottomStart = 12.dp),
                        )
                        .height(if (fullscreen) 56.dp else 48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (fullscreen) IconButton(onClick = { saveProgress(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = androidx.compose.ui.graphics.Color.White)
                    }
                    if (fullscreen) Text(title, color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.weight(1f), maxLines = 1)
                    IconButton(onClick = {
                        showCast = true
                        discovering = true
                        castError = null
                        scope.launch {
                            runCatching { dlna.discover() }.onSuccess { devices = it }
                                .onFailure { castError = it.message ?: "搜索设备失败" }
                            discovering = false
                        }
                    }) { Icon(Icons.Default.Cast, "DLNA 投屏", tint = androidx.compose.ui.graphics.Color.White) }
                    IconButton(onClick = { showPlaybackSettings = true }) {
                        Icon(Icons.Default.Settings, "速度与音轨", tint = androidx.compose.ui.graphics.Color.White)
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f),
                            RoundedCornerShape(topStart = 12.dp),
                        )
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = enterPip) {
                        Icon(Icons.Default.PictureInPicture, "画中画", tint = androidx.compose.ui.graphics.Color.White)
                    }
                    IconButton(onClick = {
                        saveProgress()
                        activity.requestedOrientation =
                            if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            }
                    }) {
                        Icon(Icons.Default.Fullscreen, "横竖屏切换", tint = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }
        }
    }

    if (showPlaybackSettings) {
        val audioGroups = player.currentTracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }
        AlertDialog(
            onDismissRequest = { showPlaybackSettings = false },
            title = { Text("速度与音轨") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("播放速度", style = MaterialTheme.typography.titleSmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                            FilterChip(
                                selected = kotlin.math.abs(player.playbackParameters.speed - speed) < 0.01f,
                                onClick = {
                                    player.setPlaybackSpeed(speed)
                                    showPlaybackSettings = false
                                },
                                label = { Text(if (speed == 1f) "正常" else "${speed}x") },
                            )
                        }
                    }
                    HorizontalDivider()
                    Text("音轨", style = MaterialTheme.typography.titleSmall)
                    if (audioGroups.isEmpty()) {
                        Text("当前视频没有可切换的音轨", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    player.trackSelectionParameters = player.trackSelectionParameters
                                        .buildUpon()
                                        .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
                                        .build()
                                    showPlaybackSettings = false
                                },
                                label = { Text("自动") },
                            )
                            var audioNumber = 0
                            audioGroups.forEach { group ->
                                repeat(group.length) { trackIndex ->
                                    audioNumber += 1
                                    val format = group.getTrackFormat(trackIndex)
                                    val label = format.label?.takeIf(String::isNotBlank)
                                        ?: format.language?.takeIf { it.isNotBlank() && it != "und" }
                                        ?: if (audioNumber == 1) "默认音轨" else "音轨 $audioNumber"
                                    FilterChip(
                                        selected = group.isTrackSelected(trackIndex),
                                        onClick = {
                                            player.trackSelectionParameters = player.trackSelectionParameters
                                                .buildUpon()
                                                .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
                                                .addOverride(TrackSelectionOverride(group.mediaTrackGroup, trackIndex))
                                                .build()
                                            showPlaybackSettings = false
                                        },
                                        label = { Text(label) },
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPlaybackSettings = false }) { Text("关闭") } },
        )
    }

    if (showCast) AlertDialog(
        onDismissRequest = { showCast = false },
        title = { Text("选择 DLNA 设备") },
        text = {
            when {
                discovering -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)); Text("正在搜索同一局域网内的设备…")
                }
                castError != null -> Text(castError!!)
                devices.isEmpty() -> Text("没有发现可投屏设备。请确认电视与手机处于同一 Wi‑Fi，并已开启 DLNA。")
                else -> Column { devices.forEach { device ->
                    ListItem(
                        headlineContent = { Text(device.name) },
                        leadingContent = { Icon(Icons.Default.Cast, null) },
                        modifier = Modifier.clickable {
                            scope.launch {
                                runCatching { dlna.play(device, playing.episode.url, "$title - ${playing.episode.name}") }
                                    .onSuccess { player.pause(); showCast = false }
                                    .onFailure { castError = it.message ?: "投屏失败" }
                            }
                        },
                    )
                } }
            }
        },
        confirmButton = { TextButton(onClick = { showCast = false }) { Text("关闭") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScreen(
    title: String,
    emptyText: String,
    entries: List<LibraryEntity>,
    modifier: Modifier,
    onVideo: (Video) -> Unit,
    posterModifier: @Composable (Video) -> Modifier,
    titleModifier: @Composable (Video) -> Modifier,
    onClear: (() -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = {
                    if (onClear != null && entries.isNotEmpty()) IconButton(onClick = onClear) {
                        Icon(Icons.Default.ClearAll, "清空历史")
                    }
                },
            )
        },
    ) { padding ->
        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text(emptyText) }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(112.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(entries, key = { it.videoId }) { entry ->
                    Column(Modifier.animateItem()) {
                        VideoCard(
                            Video(entry.videoId, entry.title, entry.poster),
                            onVideo,
                            posterModifier = posterModifier,
                            titleModifier = titleModifier,
                        )
                        if (entry.playedAt != null && entry.durationMs > 0) {
                            val percent = (entry.positionMs * 100 / entry.durationMs).coerceIn(0, 100)
                            Text("${entry.episodeName.orEmpty()} · $percent%", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(theme: ThemeMode, modifier: Modifier, onTheme: (ThemeMode) -> Unit) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val updater = remember { AppUpdater(context.applicationContext) }
    var themeDialog by remember { mutableStateOf(false) }
    var checkingUpdate by remember { mutableStateOf(false) }
    var availableUpdate by remember { mutableStateOf<UpdateInfo?>(null) }

    fun open(url: String, missing: String) {
        if (url.isBlank()) scope.launch { snackbar.showSnackbar(missing) }
        else context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { TopAppBar(title = { Text("设置") }) },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item { SettingsHeader("外观") }
            item {
                SettingsRow(
                    icon = when (theme) { ThemeMode.DARK -> Icons.Default.DarkMode; ThemeMode.LIGHT -> Icons.Default.LightMode; else -> Icons.Default.Settings },
                    title = "APP 风格",
                    subtitle = when (theme) { ThemeMode.DARK -> "暗色"; ThemeMode.LIGHT -> "亮色"; else -> "跟随系统" },
                    onClick = { themeDialog = true },
                )
            }
            item { HorizontalDivider(Modifier.padding(horizontal = 16.dp)) }
            item { SettingsHeader("更新与关于") }
            item {
                SettingsRow(
                    Icons.Default.Language,
                    "通过网页观看",
                    "在系统浏览器中打开 www1.ikanbot.com",
                    external = true,
                ) {
                    open("https://www1.ikanbot.com/", "无法打开网页")
                }
            }
            item {
                SettingsRow(
                    Icons.Default.SystemUpdate,
                    "检查更新",
                    if (checkingUpdate) "正在从 GitHub 检查…" else "当前版本 ${BuildConfig.VERSION_NAME}",
                    clickable = !checkingUpdate,
                ) {
                    checkingUpdate = true
                    scope.launch {
                        runCatching { updater.check() }
                            .onSuccess { info ->
                                if (updater.isNewer(info.version)) availableUpdate = info
                                else snackbar.showSnackbar("已是最新版本 ${BuildConfig.VERSION_NAME}")
                            }
                            .onFailure { snackbar.showSnackbar(it.message ?: "检查更新失败") }
                        checkingUpdate = false
                    }
                }
            }
            item {
                SettingsRow(Icons.Default.Code, "GitHub 仓库", BuildConfig.GITHUB_URL.ifBlank { "尚未配置仓库地址" }, external = true) {
                    open(BuildConfig.GITHUB_URL, "请先配置 IKAN_GITHUB_URL")
                }
            }
            item {
                SettingsRow(Icons.Default.Person, "作者主页", BuildConfig.AUTHOR_URL.ifBlank { "尚未配置作者主页" }, external = true) {
                    open(BuildConfig.AUTHOR_URL, "请先配置 IKAN_AUTHOR_URL")
                }
            }
            item {
                SettingsRow(Icons.Default.Info, "关于爱看", "版本 ${BuildConfig.VERSION_NAME} · 原生高性能播放器", clickable = false) { }
            }
        }
    }

    if (themeDialog) AlertDialog(
        onDismissRequest = { themeDialog = false },
        title = { Text("APP 风格") },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    val label = when (mode) { ThemeMode.SYSTEM -> "跟随系统"; ThemeMode.LIGHT -> "亮色"; ThemeMode.DARK -> "暗色" }
                    ListItem(
                        headlineContent = { Text(label) },
                        leadingContent = { if (theme == mode) Icon(Icons.Default.Check, null) },
                        modifier = Modifier.clickable { onTheme(mode); themeDialog = false },
                    )
                }
            }
        },
        confirmButton = {},
    )

    availableUpdate?.let { info ->
        AlertDialog(
            onDismissRequest = { availableUpdate = null },
            title = { Text("发现新版本 ${info.version}") },
            text = { Text(info.name.ifBlank { "GitHub Release ${info.version}" }) },
            dismissButton = {
                TextButton(onClick = { availableUpdate = null }) { Text("稍后") }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                        !context.packageManager.canRequestPackageInstalls()
                    ) {
                        context.startActivity(
                            Intent(
                                android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:${context.packageName}"),
                            ),
                        )
                        scope.launch { snackbar.showSnackbar("授权后请再次点击下载并安装") }
                    } else {
                        runCatching { updater.download(info) }
                            .onSuccess {
                                availableUpdate = null
                                scope.launch { snackbar.showSnackbar("已开始下载，完成后将打开安装界面") }
                            }
                            .onFailure { error ->
                                scope.launch { snackbar.showSnackbar(error.message ?: "下载失败") }
                            }
                    }
                }) { Text("下载并安装") }
            },
        )
    }
}

@Composable
private fun SettingsHeader(text: String) {
    Text(text, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp))
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    external: Boolean = false,
    clickable: Boolean = true,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        leadingContent = { Icon(icon, null) },
        trailingContent = {
            if (external) Icon(Icons.Default.OpenInNew, "打开外部链接", Modifier.size(18.dp))
            else if (clickable) Text("›", style = MaterialTheme.typography.titleLarge)
        },
        modifier = if (clickable) Modifier.clickable(onClick = onClick) else Modifier,
    )
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 10.dp))
}

@Composable
private fun CenterLoading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun ErrorState(error: String, retry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(error, modifier = Modifier.padding(16.dp))
        Button(onClick = retry) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(8.dp)); Text("重试") }
    }
}

@Composable
private fun IKanTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val systemDark = (LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val dark = mode == ThemeMode.DARK || (mode == ThemeMode.SYSTEM && systemDark)
    val colors: ColorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (dark) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colors, content = content)
}
