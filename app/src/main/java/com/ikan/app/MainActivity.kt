package com.ikan.app

import android.Manifest
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Palette
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.movableContentWithReceiverOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
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
import androidx.media3.exoplayer.offline.Download
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.materialkolor.dynamicColorScheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val pipState = mutableStateOf(false)
    private val pipReturningState = mutableStateOf(false)
    private val paletteRevision = mutableStateOf(0)
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
            val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle()
            val customThemeColor by viewModel.customThemeColor.collectAsStateWithLifecycle()
            IKanTheme(theme, dynamicColor, customThemeColor, paletteRevision.value) {
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

    override fun onResume() {
        super.onResume()
        // Some vendor systems update Monet overlays without dispatching a configuration that
        // Compose observes. Re-read the system palette whenever the app returns to foreground.
        paletteRevision.value++
        val pendingUpdate = UpdateInstaller.pendingOrCompleted(this)
        if (pendingUpdate >= 0) {
            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    UpdateInstallActivity.open(this, pendingUpdate)
                }
            }
        }
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
    CACHE("缓存", Icons.Default.Download),
    SETTINGS("设置", Icons.Default.Settings),
}

private class TabSwipeState {
    var pagerOriginInRoot = Offset.Zero
    val exclusions = mutableMapOf<Any, ComposeRect>()
    var animationJob: Job? = null
    var navigationTapJob: Job? = null
}

private val LocalTabSwipeState =
    staticCompositionLocalOf<TabSwipeState?> { null }

@Composable
private fun Modifier.excludeFromTabSwipe(key: Any): Modifier {
    val registry = LocalTabSwipeState.current
    val registration = remember(key) { Any() }
    DisposableEffect(registry, registration) {
        onDispose { registry?.exclusions?.remove(registration) }
    }
    return if (registry == null) {
        this
    } else {
        onGloballyPositioned { coordinates ->
            registry.exclusions[registration] = coordinates.boundsInRoot()
        }
    }
}

private data class Playing(val line: PlayLine, val episode: PlayEpisode, val startPosition: Long = 0)

private data class PlayerPresentation(
    val title: String,
    val playing: Playing?,
    val fullscreen: Boolean,
    val isPip: Boolean,
    val isPipReturning: Boolean,
    val enterPip: () -> Unit,
    val configureAutoPip: (Boolean) -> Unit,
    val onBack: () -> Unit,
    val onFullscreenToggle: () -> Unit,
    val cachedEpisode: CachedEpisode?,
    val onCacheEpisode: () -> Unit,
    val onCacheLine: () -> Unit,
    val onPlaybackEnded: () -> Unit,
    val onProgress: (Long, Long) -> Unit,
)

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
    var requestedCachedEpisode by remember { mutableStateOf<CachedEpisode?>(null) }
    var frozenHistory by remember { mutableStateOf<List<LibraryEntity>?>(null) }
    val appScope = rememberCoroutineScope()
    val homeListState = rememberLazyListState()
    val homeGridState = rememberLazyGridState()
    val homeSectionPositions = remember { mutableMapOf<String, Pair<Int, Int>>() }
    val favoritesGridState = rememberLazyGridState()
    val historyGridState = rememberLazyGridState()
    val cacheListState = rememberLazyListState()
    val settingsListState = rememberLazyListState()
    val transitionChromeAlpha by animateFloatAsState(
        targetValue = if (detailId == null) 1f else 0f,
        animationSpec = tween(320),
        label = "页面前景透明度",
    )

    fun openVideo(video: Video) {
        requestedCachedEpisode = null
        detailVideo = video
        detailId = video.id
        viewModel.loadDetail(video.id)
    }

    fun openCachedEpisode(item: CachedEpisode) {
        requestedCachedEpisode = item
        detailVideo = Video(item.videoId, item.title, item.poster)
        detailId = item.videoId
        viewModel.loadDetail(item.videoId)
    }

    fun isTabAtTop(tab: MainTab): Boolean = when (tab) {
        MainTab.HOME -> {
            if (viewModel.catalog.value.page?.sections?.isNotEmpty() == true) {
                homeListState.firstVisibleItemIndex == 0 &&
                    homeListState.firstVisibleItemScrollOffset == 0
            } else {
                homeGridState.firstVisibleItemIndex == 0 &&
                    homeGridState.firstVisibleItemScrollOffset == 0
            }
        }
        MainTab.FAVORITES ->
            favoritesGridState.firstVisibleItemIndex == 0 &&
                favoritesGridState.firstVisibleItemScrollOffset == 0
        MainTab.HISTORY ->
            historyGridState.firstVisibleItemIndex == 0 &&
                historyGridState.firstVisibleItemScrollOffset == 0
        MainTab.CACHE ->
            cacheListState.firstVisibleItemIndex == 0 &&
                cacheListState.firstVisibleItemScrollOffset == 0
        MainTab.SETTINGS ->
            settingsListState.firstVisibleItemIndex == 0 &&
                settingsListState.firstVisibleItemScrollOffset == 0
    }

    fun refreshTab(tab: MainTab) {
        if (tab == MainTab.HOME) viewModel.refreshCatalog()
        // Library and cache tabs are backed by live Room/Media3 flows and are already current.
    }

    fun handleTabReselection(tab: MainTab, forceRefresh: Boolean) {
        appScope.launch {
            if (forceRefresh || isTabAtTop(tab)) {
                refreshTab(tab)
            } else {
                when (tab) {
                    MainTab.HOME -> {
                        if (viewModel.catalog.value.page?.sections?.isNotEmpty() == true) {
                            homeListState.animateScrollToItem(0)
                        } else {
                            homeGridState.animateScrollToItem(0)
                        }
                    }
                    MainTab.FAVORITES -> favoritesGridState.animateScrollToItem(0)
                    MainTab.HISTORY -> historyGridState.animateScrollToItem(0)
                    MainTab.CACHE -> cacheListState.animateScrollToItem(0)
                    MainTab.SETTINGS -> settingsListState.animateScrollToItem(0)
                }
            }
        }
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
                Modifier
                    .wrapContentWidth(Alignment.Start)
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState("title-${video.id}"),
                        animatedVisibilityScope = this@AnimatedContent,
                        boundsTransform = { _, _ -> tween(320) },
                        placeholderSize = SharedTransitionScope.PlaceholderSize.ContentSize,
                        resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(
                            contentScale = ContentScale.FillWidth,
                        ),
                    )
            }
            val transitionChromeModifier = Modifier.renderInSharedTransitionScopeOverlay(
                zIndexInOverlay = 10f,
                renderInOverlay = { isTransitionActive },
            ).alpha(transitionChromeAlpha)
            val detailTopBarModifier = Modifier.renderInSharedTransitionScopeOverlay(
                zIndexInOverlay = 20f,
                renderInOverlay = { isTransitionActive },
            )
            if (id != null) {
                DetailRoute(
                    videoId = id,
                    sourceVideo = detailVideo,
                    requestedCachedEpisode = requestedCachedEpisode,
                    posterModifier = sharedPosterModifier,
                    titleModifier = sharedTitleModifier,
                    viewModel = viewModel,
                    isPip = isPip,
                    isPipReturning = isPipReturning,
                    enterPip = enterPip,
                    configureAutoPip = configureAutoPip,
                    deferDetailContent = detailId != null && isTransitionActive,
                    topBarModifier = detailTopBarModifier,
                    onBack = {
                        detailId = null
                        // Keep the outgoing detail tree alive until its poster/title have reached
                        // the retained catalog card. Clearing it immediately removes the source.
                        appScope.launch {
                            delay(340)
                            if (detailId == null) {
                                viewModel.closeDetail()
                                frozenHistory = null
                                requestedCachedEpisode = null
                            }
                        }
                    },
                )
            } else {
                MainTabs(
                    selected = tab,
                    onSelected = { tab = it },
                    onReselected = { handleTabReselection(it, false) },
                    onDoubleClicked = { handleTabReselection(it, true) },
                    navigationModifier = transitionChromeModifier,
                ) { visibleTab, padding ->
                    when (visibleTab) {
                        MainTab.HOME -> HomeScreen(
                            viewModel,
                            padding,
                            ::openVideo,
                            sharedPosterModifier,
                            sharedTitleModifier,
                            transitionChromeModifier,
                            homeListState,
                            homeGridState,
                            homeSectionPositions,
                        )
                        MainTab.FAVORITES -> {
                            val favorites by viewModel.favorites.collectAsStateWithLifecycle()
                            LibraryScreen(
                                title = "我的收藏",
                                emptyText = "还没有收藏影片",
                                entries = favorites,
                                modifier = Modifier.fillMaxSize(),
                                navigationPadding = padding,
                                onVideo = ::openVideo,
                                posterModifier = sharedPosterModifier,
                                titleModifier = sharedTitleModifier,
                                transitionChromeModifier = transitionChromeModifier,
                                gridState = favoritesGridState,
                            )
                        }
                        MainTab.HISTORY -> {
                            val history by viewModel.history.collectAsStateWithLifecycle()
                            val downloads by viewModel.downloads.collectAsStateWithLifecycle()
                            LibraryScreen(
                                title = "播放历史",
                                emptyText = "还没有播放记录",
                                entries = frozenHistory ?: history,
                                modifier = Modifier.fillMaxSize(),
                                navigationPadding = padding,
                                onVideo = { video ->
                                    frozenHistory = history
                                    openVideo(video)
                                },
                                posterModifier = sharedPosterModifier,
                                titleModifier = sharedTitleModifier,
                                transitionChromeModifier = transitionChromeModifier,
                                gridState = historyGridState,
                                onClear = viewModel::clearHistory,
                                cachedVideoIds = downloads.mapTo(mutableSetOf()) { it.videoId },
                            )
                        }
                        MainTab.CACHE -> {
                            val downloads by viewModel.downloads.collectAsStateWithLifecycle()
                            CacheScreen(
                                downloads = downloads,
                                modifier = Modifier.fillMaxSize(),
                                navigationPadding = padding,
                                onPlay = ::openCachedEpisode,
                                onDelete = viewModel::removeDownload,
                                onSetPaused = viewModel::setDownloadPaused,
                                onClear = viewModel::clearDownloads,
                                listState = cacheListState,
                            )
                        }
                        MainTab.SETTINGS -> SettingsScreen(
                            theme = viewModel.theme.collectAsStateWithLifecycle().value,
                            dynamicColor = viewModel.dynamicColor.collectAsStateWithLifecycle().value,
                            customThemeColor = viewModel.customThemeColor.collectAsStateWithLifecycle().value,
                            listState = settingsListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = padding.calculateBottomPadding()),
                            onTheme = viewModel::setTheme,
                            onDynamicColor = viewModel::setDynamicColor,
                            onCustomThemeColor = viewModel::setCustomThemeColor,
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
    onReselected: (MainTab) -> Unit,
    onDoubleClicked: (MainTab) -> Unit,
    navigationModifier: Modifier = Modifier,
    content: @Composable (MainTab, androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    val tabs = MainTab.entries
    val pagerState = rememberPagerState(
        initialPage = selected.ordinal,
        pageCount = { tabs.size },
    )
    val scope = rememberCoroutineScope()
    val visibleTab = tabs[pagerState.currentPage]
    val swipeState = remember { TabSwipeState() }

    LaunchedEffect(pagerState.settledPage) {
        val settledTab = tabs[pagerState.settledPage]
        if (settledTab != selected) onSelected(settledTab)
    }

    fun selectTab(tab: MainTab) {
        val isCurrentSettledTab =
            pagerState.currentPage == tab.ordinal &&
                pagerState.currentPageOffsetFraction == 0f
        if (isCurrentSettledTab) {
            val pendingTap = swipeState.navigationTapJob
            if (pendingTap?.isActive == true) {
                pendingTap.cancel()
                swipeState.navigationTapJob = null
                onDoubleClicked(tab)
            } else {
                swipeState.navigationTapJob = scope.launch {
                    delay(android.view.ViewConfiguration.getDoubleTapTimeout().toLong())
                    swipeState.navigationTapJob = null
                    onReselected(tab)
                }
            }
            return
        }

        swipeState.navigationTapJob?.cancel()
        swipeState.navigationTapJob = null
        onSelected(tab)
        if (pagerState.currentPage != tab.ordinal || pagerState.currentPageOffsetFraction != 0f) {
            swipeState.animationJob?.cancel()
            swipeState.animationJob =
                scope.launch { pagerState.animateScrollToPage(tab.ordinal) }
        }
    }

    @Composable
    fun PagerContent(
        modifier: Modifier,
        padding: androidx.compose.foundation.layout.PaddingValues,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = modifier
                .onGloballyPositioned { coordinates ->
                    swipeState.pagerOriginInRoot = coordinates.localToRoot(Offset.Zero)
                }
                .pointerInput(pagerState, tabs.size) {
                    val flingThreshold = 400.dp.toPx()
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downInRoot = down.position + swipeState.pagerOriginInRoot
                        if (swipeState.exclusions.values.any { it.contains(downInRoot) }) {
                            return@awaitEachGesture
                        }

                        // A fresh drag must take ownership immediately; dispatchRawDelta does not
                        // cancel an animateScrollToPage that is still settling from the last swipe.
                        swipeState.animationJob?.cancel()
                        swipeState.animationJob = null
                        val startPage = pagerState.currentPage
                        val velocityTracker = VelocityTracker()
                        velocityTracker.addPosition(down.uptimeMillis, down.position)
                        var totalX = 0f
                        var totalY = 0f
                        var horizontalDrag = false
                        var verticalGesture = false
                        var pressed: Boolean
                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                                ?: break
                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                            val delta = change.positionChange()
                            totalX += delta.x
                            totalY += delta.y
                            if (!horizontalDrag && !verticalGesture &&
                                kotlin.math.max(kotlin.math.abs(totalX), kotlin.math.abs(totalY)) >
                                viewConfiguration.touchSlop
                            ) {
                                horizontalDrag = kotlin.math.abs(totalX) > kotlin.math.abs(totalY)
                                verticalGesture = !horizontalDrag
                            }
                            if (horizontalDrag && delta.x != 0f) {
                                change.consume()
                                pagerState.dispatchRawDelta(-delta.x)
                            }
                            pressed = change.pressed
                        } while (pressed)

                        if (horizontalDrag) {
                            val velocityX = velocityTracker.calculateVelocity().x
                            val pagePosition =
                                pagerState.currentPage + pagerState.currentPageOffsetFraction
                            val pageDelta = pagePosition - startPage
                            val target = when {
                                velocityX <= -flingThreshold -> startPage + 1
                                velocityX >= flingThreshold -> startPage - 1
                                pageDelta >= 0.5f -> startPage + 1
                                pageDelta <= -0.5f -> startPage - 1
                                else -> startPage
                            }.coerceIn(tabs.indices)
                            swipeState.animationJob =
                                scope.launch { pagerState.animateScrollToPage(target) }
                        }
                    }
                },
            userScrollEnabled = false,
            key = { tabs[it] },
        ) { page ->
            CompositionLocalProvider(LocalTabSwipeState provides swipeState) {
                content(tabs[page], padding)
            }
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 600.dp
        if (wide) {
            Row(Modifier.fillMaxSize()) {
                NavigationRail(navigationModifier.fillMaxHeight().navigationBarsPadding().statusBarsPadding()) {
                    MainTab.entries.forEach { tab ->
                        NavigationRailItem(
                            selected = visibleTab == tab,
                            onClick = { selectTab(tab) },
                            icon = { Icon(tab.icon, null) },
                            label = { Text(tab.label) },
                        )
                    }
                }
                PagerContent(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    padding = androidx.compose.foundation.layout.PaddingValues(),
                )
            }
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar(navigationModifier) {
                        MainTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = visibleTab == tab,
                                onClick = { selectTab(tab) },
                                icon = { Icon(tab.icon, null) },
                                label = { Text(tab.label) },
                            )
                        }
                    }
                },
                content = { padding ->
                    PagerContent(Modifier.fillMaxSize(), padding)
                },
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
    transitionChromeModifier: Modifier,
    listState: LazyListState,
    gridState: LazyGridState,
    sectionPositions: MutableMap<String, Pair<Int, Int>>,
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

    BoxWithConstraints(Modifier.fillMaxSize().padding(padding).statusBarsPadding()) {
        val medium = maxWidth >= 600.dp
        val expanded = maxWidth >= 840.dp
        val horizontalPadding = when {
            expanded -> 32.dp
            medium -> 24.dp
            else -> 16.dp
        }
        val cardWidth = when {
            expanded -> 160.dp
            medium -> 144.dp
            else -> 116.dp
        }
        val gridMinWidth = when {
            expanded -> 160.dp
            medium -> 144.dp
            else -> 112.dp
        }

        Column(
            Modifier
                .widthIn(max = 1400.dp)
                .fillMaxSize()
                .align(Alignment.TopCenter),
        ) {
        Column(
            Modifier
                .fillMaxWidth()
                .then(transitionChromeModifier)
                .background(MaterialTheme.colorScheme.background),
        ) {
        Text("爱看", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = horizontalPadding, top = 8.dp, bottom = 8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = horizontalPadding)
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
            Column(
                Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = horizontalPadding, vertical = 8.dp),
            ) {
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
            modifier = Modifier
                .fillMaxWidth()
                .excludeFromTabSwipe("home-categories"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = horizontalPadding, vertical = 8.dp),
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
        }
        CatalogContent(
            state = catalog,
            onVideo = onVideo,
            posterModifier = posterModifier,
            titleModifier = titleModifier,
            listState = listState,
            gridState = gridState,
            sectionPositions = sectionPositions,
            cardWidth = cardWidth,
            gridMinWidth = gridMinWidth,
            horizontalPadding = horizontalPadding,
            transitionChromeModifier = transitionChromeModifier,
            onFilter = viewModel::loadPath,
            onNext = { path -> viewModel.loadPath(path, catalog.page?.title ?: category.label) },
            onRetry = { viewModel.loadCategory(category) },
        )
        }
    }
}

@Composable
private fun CatalogContent(
    state: CatalogUiState,
    onVideo: (Video) -> Unit,
    posterModifier: @Composable (Video) -> Modifier,
    titleModifier: @Composable (Video) -> Modifier,
    listState: LazyListState,
    gridState: LazyGridState,
    sectionPositions: MutableMap<String, Pair<Int, Int>>,
    cardWidth: androidx.compose.ui.unit.Dp,
    gridMinWidth: androidx.compose.ui.unit.Dp,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    transitionChromeModifier: Modifier,
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
                LazyColumn(Modifier.fillMaxSize(), state = listState) {
                    page.sections.forEachIndexed { index, section ->
                        item { SectionTitle(section.title) }
                        item {
                            val sectionKey = "${page.title}:$index:${section.title}"
                            val savedPosition = sectionPositions[sectionKey] ?: (0 to 0)
                            val sectionState = remember(sectionKey) {
                                LazyListState(
                                    firstVisibleItemIndex = savedPosition.first,
                                    firstVisibleItemScrollOffset = savedPosition.second,
                                )
                            }
                            LaunchedEffect(sectionKey, sectionState) {
                                snapshotFlow {
                                    sectionState.firstVisibleItemIndex to
                                        sectionState.firstVisibleItemScrollOffset
                                }.collect { sectionPositions[sectionKey] = it }
                            }
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .excludeFromTabSwipe("section:$sectionKey"),
                                state = sectionState,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = horizontalPadding),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(section.videos, key = { it.id }) {
                                    VideoCard(it, onVideo, Modifier.width(cardWidth), posterModifier, titleModifier)
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            } else {
                CatalogGrid(
                    page,
                    state.loading,
                    onVideo,
                    posterModifier,
                    titleModifier,
                    gridState,
                    gridMinWidth,
                    horizontalPadding,
                    transitionChromeModifier,
                    onFilter,
                    onNext,
                )
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
    gridState: LazyGridState,
    gridMinWidth: androidx.compose.ui.unit.Dp,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    transitionChromeModifier: Modifier,
    onFilter: (String, String) -> Unit,
    onNext: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        if (page.filters.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(transitionChromeModifier)
                    .background(MaterialTheme.colorScheme.background)
                    .excludeFromTabSwipe("filters:${page.title}"),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = horizontalPadding),
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
            columns = GridCells.Adaptive(gridMinWidth),
            state = gridState,
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = horizontalPadding, vertical = 16.dp),
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

private fun posterRequest(context: Context, url: String, cacheKey: String): ImageRequest {
    val headers = NetworkHeaders.Builder()
        .set("User-Agent", "Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 Chrome/124 Mobile Safari/537.36")
    if (Uri.parse(url).host.orEmpty().contains("doubanio.com")) {
        headers.set("Referer", "https://movie.douban.com/")
    }
    return ImageRequest.Builder(context)
        .data(url)
        .placeholderMemoryCacheKey(cacheKey)
        .memoryCacheKey(cacheKey)
        .diskCacheKey("poster:$url")
        .httpHeaders(headers.build())
        .build()
}

@Composable
private fun PosterImage(
    url: String,
    description: String?,
    modifier: Modifier,
    cacheKey: String = "poster-url:$url",
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val request = remember(url, cacheKey) { posterRequest(context, url, cacheKey) }
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
                PosterImage(
                    video.poster,
                    video.title,
                    Modifier.fillMaxSize(),
                    cacheKey = "poster-${video.id}",
                )
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
    requestedCachedEpisode: CachedEpisode?,
    posterModifier: @Composable (Video) -> Modifier,
    titleModifier: @Composable (Video) -> Modifier,
    viewModel: MainViewModel,
    isPip: Boolean,
    isPipReturning: Boolean,
    enterPip: () -> Unit,
    configureAutoPip: (Boolean) -> Unit,
    deferDetailContent: Boolean,
    topBarModifier: Modifier,
    onBack: () -> Unit,
) {
    val state by viewModel.detail.collectAsStateWithLifecycle()
    val activity = LocalContext.current as MainActivity
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val tabletWindow = configuration.smallestScreenWidthDp >= 600
    var playing by remember(videoId) {
        mutableStateOf(
            requestedCachedEpisode?.let { cached ->
                val episode = PlayEpisode(cached.episodeName, cached.url)
                Playing(
                    PlayLine(cached.lineId, cached.lineName, listOf(episode)),
                    episode,
                    0,
                )
            },
        )
    }
    var autoResumed by remember(videoId) { mutableStateOf(false) }
    var explicitFullscreen by rememberSaveable(videoId) { mutableStateOf(false) }
    val player = remember(videoId) { activity.obtainPlaybackPlayer() }
    val fullscreen = explicitFullscreen || (isLandscape && !tabletWindow)

    DisposableEffect(player) {
        onDispose {
            configureAutoPip(false)
            player.stop()
            player.clearMediaItems()
        }
    }

    LaunchedEffect(state.detail, state.resume, requestedCachedEpisode) {
        val detail = state.detail ?: return@LaunchedEffect
        if (!autoResumed) {
            val requested = requestedCachedEpisode
            val resume = state.resume
            if (requested != null) {
                val line = detail.lines.firstOrNull { line ->
                    line.id == requested.lineId || line.episodes.any { it.url == requested.url }
                } ?: PlayLine(requested.lineId, requested.lineName, emptyList())
                val episode = line.episodes.firstOrNull { it.url == requested.url }
                    ?: PlayEpisode(requested.episodeName, requested.url)
                playing = Playing(line, episode, 0)
            } else if (resume?.streamUrl != null) {
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
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val cachedEpisode = activePlaying?.episode?.url?.let { url ->
        downloads.firstOrNull { it.url == url }
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1202,
            )
        }
    }

    fun cacheCurrentEpisode() {
        val current = activePlaying ?: return
        requestNotificationPermission()
        viewModel.cacheEpisode(current.line, current.episode)
        Toast.makeText(activity, "已加入后台缓存", Toast.LENGTH_SHORT).show()
    }

    fun cacheCurrentLine() {
        val current = activePlaying ?: return
        requestNotificationPermission()
        viewModel.cacheLine(current.line)
        Toast.makeText(activity, "已加入全集缓存，共 ${current.line.episodes.size} 集", Toast.LENGTH_SHORT).show()
    }

    fun playNextEpisode() {
        val current = activePlaying ?: return
        val currentIndex = current.line.episodes.indexOfFirst { it.url == current.episode.url }
        val next = current.line.episodes.getOrNull(currentIndex + 1) ?: return
        val duration = player.duration.takeUnless { it == C.TIME_UNSET || it < 0 } ?: 0L
        viewModel.recordPlayback(
            current.line.id,
            current.episode.name,
            current.episode.url,
            duration,
            duration,
        )
        playing = Playing(current.line, next, 0)
        viewModel.recordPlayback(current.line.id, next.name, next.url, 0, 0)
    }

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

    fun leaveFullscreen() {
        if (tabletWindow) {
            // Establish the final inset geometry before Lookahead starts moving the player. This
            // keeps resize and translation in one continuous bounds animation.
            WindowCompat.getInsetsController(activity.window, activity.window.decorView)
                .show(WindowInsetsCompat.Type.systemBars())
            activity.window.decorView.postOnAnimation { explicitFullscreen = false }
        } else {
            explicitFullscreen = false
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    fun toggleFullscreen() {
        if (fullscreen) {
            leaveFullscreen()
        } else {
            explicitFullscreen = true
            if (!isLandscape) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }
    }

    BackHandler {
        if (fullscreen) leaveFullscreen() else leaveDetail()
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
    val playerPresentation = rememberUpdatedState(
        PlayerPresentation(
            title = state.detail?.video?.title ?: sourceVideo?.title ?: "影片详情",
            playing = activePlaying,
            fullscreen = fullscreen,
            isPip = isPip,
            isPipReturning = isPipReturning,
            enterPip = enterPip,
            configureAutoPip = configureAutoPip,
            onBack = ::leaveFullscreen,
            onFullscreenToggle = ::toggleFullscreen,
            cachedEpisode = cachedEpisode,
            onCacheEpisode = ::cacheCurrentEpisode,
            onCacheLine = ::cacheCurrentLine,
            onPlaybackEnded = ::playNextEpisode,
            onProgress = { position, duration ->
                activePlaying?.let { current ->
                    playing = current.copy(startPosition = position)
                    viewModel.recordPlayback(
                        current.line.id,
                        current.episode.name,
                        current.episode.url,
                        position,
                        duration,
                    )
                }
            },
        ),
    )
    val movablePlayer = remember(player) {
        movableContentWithReceiverOf<LookaheadScope> {
            val presentation = playerPresentation.value
            val playerModifier = if (presentation.isPip) {
                Modifier
            } else {
                Modifier.animateBounds(
                    lookaheadScope = this@movableContentWithReceiverOf,
                    boundsTransform = { _, _ -> tween(360) },
                )
            }
            NativePlayer(
                player = player,
                title = presentation.title,
                playing = presentation.playing,
                fullscreen = presentation.fullscreen,
                modifier = playerModifier.fillMaxSize(),
                isPip = presentation.isPip,
                isPipReturning = presentation.isPipReturning,
                enterPip = presentation.enterPip,
                configureAutoPip = presentation.configureAutoPip,
                onBack = presentation.onBack,
                onFullscreenToggle = presentation.onFullscreenToggle,
                cachedEpisode = presentation.cachedEpisode,
                onCacheEpisode = presentation.onCacheEpisode,
                onCacheLine = presentation.onCacheLine,
                onPlaybackEnded = presentation.onPlaybackEnded,
                onProgress = presentation.onProgress,
            )
        }
    }

    LookaheadScope {
        if (isPip) {
            // Once Android hands the activity window to PiP, keep only the existing player
            // surface in that window. Relaying out the detail scaffold inside the tiny PiP
            // bounds makes the video jump after the system's source-rect animation.
            Box(Modifier.fillMaxSize()) { movablePlayer() }
            return@LookaheadScope
        }
        if (fullscreen) {
            Box(Modifier.fillMaxSize()) { movablePlayer() }
            return@LookaheadScope
        }
        Scaffold(
        topBar = {
            TopAppBar(
                modifier = topBarModifier,
                title = {
                    Text(
                        state.detail?.video?.title ?: sourceVideo?.title ?: "影片详情",
                    )
                },
                navigationIcon = {
                    IconButton(onClick = ::leaveDetail) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                },
                actions = {
                    if (state.detail != null) IconButton(onClick = {
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
            state.loading || deferDetailContent -> {
                val loadingInfo: @Composable (Modifier) -> Unit = { modifier ->
                    if (sourceVideo != null) {
                        Row(
                            modifier = modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp),
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
                                cacheKey = "poster-${sourceVideo.id}",
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
                        CenterLoading(modifier)
                    }
                }
                // Use the same bounds as the loaded layout so neither the player nor the shared
                // poster/title jumps when the asynchronous detail response arrives.
                if (tabletWindow && isLandscape) {
                    Row(
                        Modifier.fillMaxSize().padding(padding).padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(Modifier.weight(1.65f).aspectRatio(16f / 9f)) { movablePlayer() }
                        loadingInfo(Modifier.weight(1f).fillMaxHeight())
                    }
                } else {
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) { movablePlayer() }
                        loadingInfo(Modifier.fillMaxWidth().weight(1f))
                    }
                }
            }
            state.error != null -> {
                if (activePlaying != null) {
                    if (tabletWindow && isLandscape) {
                        Row(
                            Modifier.fillMaxSize().padding(padding).padding(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(Modifier.weight(1.65f).aspectRatio(16f / 9f)) { movablePlayer() }
                            ErrorState(
                                "正在播放已缓存内容\n${state.error}",
                                { viewModel.loadDetail(videoId) },
                                Modifier.weight(1f).fillMaxHeight(),
                            )
                        }
                    } else {
                        Column(Modifier.fillMaxSize().padding(padding)) {
                            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) { movablePlayer() }
                            ErrorState(
                                "正在播放已缓存内容\n${state.error}",
                                { viewModel.loadDetail(videoId) },
                                Modifier.fillMaxWidth().weight(1f),
                            )
                        }
                    }
                } else {
                    ErrorState(state.error!!, { viewModel.loadDetail(videoId) }, Modifier.padding(padding))
                }
            }
            state.detail != null -> {
                val detail = state.detail!!
                val posterVideo = sourceVideo ?: detail.video
                fun selectEpisode(line: PlayLine, episode: PlayEpisode) {
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

                if (tabletWindow && isLandscape) {
                    Row(
                        Modifier.fillMaxSize().padding(padding).padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(Modifier.weight(1.65f).aspectRatio(16f / 9f)) { movablePlayer() }
                        DetailInfoList(
                            detail = detail,
                            posterVideo = posterVideo,
                            activePlaying = activePlaying,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            posterModifier = posterModifier,
                            titleModifier = titleModifier,
                            onEpisode = ::selectEpisode,
                        )
                    }
                } else {
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) { movablePlayer() }
                        DetailInfoList(
                            detail = detail,
                            posterVideo = posterVideo,
                            activePlaying = activePlaying,
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            posterModifier = posterModifier,
                            titleModifier = titleModifier,
                            onEpisode = ::selectEpisode,
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun DetailInfoList(
    detail: com.ikan.app.model.VideoDetail,
    posterVideo: Video,
    activePlaying: Playing?,
    modifier: Modifier,
    posterModifier: @Composable (Video) -> Modifier,
    titleModifier: @Composable (Video) -> Modifier,
    onEpisode: (PlayLine, PlayEpisode) -> Unit,
) {
    LazyColumn(
        modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 14.dp, bottom = 24.dp),
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
                    cacheKey = "poster-${posterVideo.id}",
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
                                onClick = { if (!selected) onEpisode(line, episode) },
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NativePlayer(
    player: ExoPlayer,
    title: String,
    playing: Playing?,
    fullscreen: Boolean,
    modifier: Modifier,
    isPip: Boolean,
    isPipReturning: Boolean,
    enterPip: () -> Unit,
    configureAutoPip: (Boolean) -> Unit,
    onBack: () -> Unit,
    onFullscreenToggle: () -> Unit,
    cachedEpisode: CachedEpisode?,
    onCacheEpisode: () -> Unit,
    onCacheLine: () -> Unit,
    onPlaybackEnded: () -> Unit,
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
    var showCacheOptions by remember { mutableStateOf(false) }
    var gestureFeedback by remember { mutableStateOf<String?>(null) }
    var gestureFeedbackDismissJob by remember { mutableStateOf<Job?>(null) }
    var controlsVisible by remember(player) { mutableStateOf(true) }
    var buffering by remember(player) { mutableStateOf(player.playbackState == Player.STATE_BUFFERING) }
    var playbackError by remember(player) { mutableStateOf<PlaybackException?>(player.playerError) }
    var playerView by remember(player) { mutableStateOf<GesturePlayerView?>(null) }
    val currentOnProgress by rememberUpdatedState(onProgress)
    val currentOnPlaybackEnded by rememberUpdatedState(onPlaybackEnded)
    fun saveProgress() {
        val duration = player.duration.takeUnless { it == C.TIME_UNSET } ?: 0
        currentOnProgress(player.currentPosition, duration)
    }
    fun openCastPicker() {
        if (playing == null) return
        showCast = true
        discovering = true
        castError = null
        scope.launch {
            runCatching { dlna.discover() }.onSuccess { devices = it }
                .onFailure { castError = it.message ?: "搜索设备失败" }
            discovering = false
        }
    }
    fun requestCache() {
        if (playing == null || cachedEpisode != null) return
        if ((playing.line.episodes.size) > 1) {
            showCacheOptions = true
        } else {
            onCacheEpisode()
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                buffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) playbackError = null
                if (playbackState == Player.STATE_ENDED) currentOnPlaybackEnded()
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
    DisposableEffect(player) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        onDispose {
            saveProgress()
            configureAutoPip(false)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
    LaunchedEffect(fullscreen) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        if (fullscreen) {
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
        controlsVisible = true
        playerView?.showController()
    }

    Surface(modifier, color = androidx.compose.ui.graphics.Color.Black) {
        Box(Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    (LayoutInflater.from(ctx).inflate(R.layout.player_view, null, false) as GesturePlayerView).apply {
                        playerView = this
                        this.player = player
                        onGestureFeedback = {
                            gestureFeedback = it
                            gestureFeedbackDismissJob?.cancel()
                            gestureFeedbackDismissJob = if (it == null) {
                                null
                            } else {
                                scope.launch {
                                    delay(1_200L)
                                    gestureFeedback = null
                                }
                            }
                        }
                        setControllerVisibilityListener(
                            PlayerView.ControllerVisibilityListener { visibility ->
                                controlsVisible = visibility == View.VISIBLE
                            },
                        )
                        useController = !isPip && !isPipReturning
                        setShowPreviousButton(false)
                        setShowNextButton(false)
                        setControllerShowTimeoutMs(if (fullscreen) 6_000 else 5_000)
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
                        configureActionHitTargets(
                            visible = controlsVisible && !isPip && !isPipReturning,
                            fullscreen = fullscreen,
                            onBack = onBack,
                            onCast = ::openCastPicker,
                            onSettings = { showPlaybackSettings = true },
                            onCache = ::requestCache,
                            onPip = enterPip,
                            onFullscreen = onFullscreenToggle,
                        )
                        post { activity.updatePipSourceRect(this) }
                    }
                },
                update = {
                    it.player = player
                    it.useController = !isPip && !isPipReturning
                    it.setShowPreviousButton(false)
                    it.setShowNextButton(false)
                    it.setControllerShowTimeoutMs(if (fullscreen) 6_000 else 5_000)
                    it.findViewById<View>(androidx.media3.ui.R.id.exo_center_controls)?.apply {
                        val controlsScale = if (fullscreen) 1f else 0.72f
                        scaleX = controlsScale
                        scaleY = controlsScale
                    }
                    it.findViewById<View>(androidx.media3.ui.R.id.exo_settings)?.apply {
                        visibility = View.GONE
                    }
                    it.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    it.configureActionHitTargets(
                        visible = controlsVisible && !isPip && !isPipReturning,
                        fullscreen = fullscreen,
                        onBack = onBack,
                        onCast = ::openCastPicker,
                        onSettings = { showPlaybackSettings = true },
                        onCache = ::requestCache,
                        onPip = enterPip,
                        onFullscreen = onFullscreenToggle,
                    )
                    if (!isPip && !isPipReturning) {
                        it.post { activity.updatePipSourceRect(it) }
                    }
                },
                onRelease = {
                    (it as? GesturePlayerView)?.onGestureFeedback = null
                    gestureFeedbackDismissJob?.cancel()
                    gestureFeedbackDismissJob = null
                    if (playerView === it) playerView = null
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
                        .zIndex(3f)
                        .background(
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.72f),
                            RoundedCornerShape(12.dp),
                        )
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                )
            }
            if (buffering && playbackError == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center).zIndex(2f),
                    color = androidx.compose.ui.graphics.Color.White,
                )
            }
            playbackError?.let {
                Column(
                    modifier = Modifier.align(Alignment.Center).zIndex(3f),
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
                        .zIndex(4f)
                        .then(if (fullscreen) Modifier.fillMaxWidth() else Modifier)
                        .background(
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = if (fullscreen) 0.55f else 0.4f),
                            if (fullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(bottomStart = 12.dp),
                        )
                        .height(if (fullscreen) 56.dp else 48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (fullscreen) PlayerActionIcon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    if (fullscreen) Text(title, color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.weight(1f), maxLines = 1)
                    PlayerActionIcon(Icons.Default.Cast, "DLNA 投屏", enabled = playing != null)
                    PlayerActionIcon(Icons.Default.Settings, "速度与音轨")
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .zIndex(4f)
                        .background(
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f),
                            RoundedCornerShape(topStart = 12.dp),
                        )
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlayerActionIcon(
                        if (cachedEpisode?.completed == true) Icons.Default.DownloadDone else Icons.Default.Download,
                        when {
                            cachedEpisode?.completed == true -> "已缓存"
                            cachedEpisode != null -> "缓存中"
                            else -> "缓存"
                        },
                        enabled = playing != null && cachedEpisode == null,
                    )
                    PlayerActionIcon(Icons.Default.PictureInPicture, "画中画")
                    PlayerActionIcon(
                        Icons.Default.Fullscreen,
                        "横竖屏切换",
                        iconSize = 30.dp,
                    )
                }
            }
        }
    }

    if (showCacheOptions) {
        AlertDialog(
            onDismissRequest = { showCacheOptions = false },
            title = { Text("缓存视频") },
            text = { Text("选择缓存当前一集，或缓存当前线路的全部剧集。") },
            dismissButton = {
                TextButton(onClick = {
                    showCacheOptions = false
                    onCacheEpisode()
                }) { Text("缓存本集") }
            },
            confirmButton = {
                TextButton(onClick = {
                    showCacheOptions = false
                    onCacheLine()
                }) { Text("缓存全集") }
            },
        )
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
                                val current = playing ?: return@launch
                                runCatching { dlna.play(device, current.episode.url, "$title - ${current.episode.name}") }
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

@Composable
private fun PlayerActionIcon(
    icon: ImageVector,
    description: String,
    enabled: Boolean = true,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
) {
    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Icon(
            icon,
            description,
            modifier = Modifier.size(iconSize),
            tint = androidx.compose.ui.graphics.Color.White.copy(alpha = if (enabled) 1f else 0.45f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScreen(
    title: String,
    emptyText: String,
    entries: List<LibraryEntity>,
    modifier: Modifier,
    navigationPadding: androidx.compose.foundation.layout.PaddingValues,
    onVideo: (Video) -> Unit,
    posterModifier: @Composable (Video) -> Modifier,
    titleModifier: @Composable (Video) -> Modifier,
    transitionChromeModifier: Modifier,
    gridState: LazyGridState,
    onClear: (() -> Unit)? = null,
    cachedVideoIds: Set<String> = emptySet(),
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                modifier = transitionChromeModifier,
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
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(
                        top = padding.calculateTopPadding(),
                        bottom = navigationPadding.calculateBottomPadding(),
                    ),
                contentAlignment = Alignment.Center,
            ) { Text(emptyText) }
        } else {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val medium = maxWidth >= 600.dp
                val expanded = maxWidth >= 840.dp
                val gridMinWidth = when {
                    expanded -> 168.dp
                    medium -> 152.dp
                    else -> 112.dp
                }
                val horizontalPadding = when {
                    expanded -> 32.dp
                    medium -> 24.dp
                    else -> 16.dp
                }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(gridMinWidth),
                    state = gridState,
                    modifier = Modifier
                        .widthIn(max = 1400.dp)
                        .fillMaxSize()
                        .align(Alignment.TopCenter),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = horizontalPadding,
                        top = padding.calculateTopPadding() + 16.dp,
                        end = horizontalPadding,
                        bottom = navigationPadding.calculateBottomPadding() + 16.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
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
                            if (entry.videoId in cachedVideoIds) {
                                Text(
                                    "已缓存",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun CacheScreen(
    downloads: List<CachedEpisode>,
    modifier: Modifier,
    navigationPadding: androidx.compose.foundation.layout.PaddingValues,
    listState: LazyListState,
    onPlay: (CachedEpisode) -> Unit,
    onDelete: (String) -> Unit,
    onSetPaused: (String, Boolean) -> Unit,
    onClear: () -> Unit,
) {
    var confirmClear by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<CachedEpisode?>(null) }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("缓存") },
                actions = {
                    if (downloads.isNotEmpty()) {
                        IconButton(onClick = { confirmClear = true }) {
                            Icon(Icons.Default.ClearAll, "清空全部缓存")
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (downloads.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(
                        top = padding.calculateTopPadding(),
                        bottom = navigationPadding.calculateBottomPadding(),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("还没有缓存节目")
            }
        } else {
            Box(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
                LazyColumn(
                    Modifier
                        .widthIn(max = 960.dp)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .align(Alignment.TopCenter),
                    state = listState,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        bottom = navigationPadding.calculateBottomPadding() + 16.dp,
                    ),
                ) {
                    items(downloads, key = { it.id }) { item ->
                        val dismissState = rememberSwipeToDismissBoxState()
                        LaunchedEffect(dismissState.settledValue) {
                            if (dismissState.settledValue == SwipeToDismissBoxValue.EndToStart) {
                                pendingDelete = item
                                dismissState.reset()
                            }
                        }
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(end = 24.dp),
                                    contentAlignment = Alignment.CenterEnd,
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        "删除缓存",
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                }
                            },
                        ) {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        item.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                supportingContent = {
                                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Text("${item.lineName} · ${item.episodeName}")
                                        Text(
                                            cacheStatus(item),
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                        if (!item.completed && item.percent >= 0f) {
                                            LinearProgressIndicator(
                                                progress = {
                                                    (item.percent / 100f).coerceIn(0f, 1f)
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                drawStopIndicator = {},
                                            )
                                        }
                                    }
                                },
                                leadingContent = {
                                    PosterImage(
                                        item.poster,
                                        item.title,
                                        Modifier
                                            .width(64.dp)
                                            .aspectRatio(0.71f)
                                            .clip(RoundedCornerShape(6.dp)),
                                        cacheKey = "poster-${item.videoId}",
                                    )
                                },
                                trailingContent = {
                                    val canChangeState =
                                        !item.completed && item.state != Download.STATE_REMOVING
                                    val resume =
                                        item.paused || item.state == Download.STATE_FAILED
                                    IconButton(
                                        onClick = { onSetPaused(item.id, !resume) },
                                        enabled = canChangeState,
                                    ) {
                                        Icon(
                                            when {
                                                item.completed -> Icons.Default.DownloadDone
                                                resume -> Icons.Default.PlayArrow
                                                else -> Icons.Default.Pause
                                            },
                                            when {
                                                item.completed -> "已缓存"
                                                resume -> "继续缓存"
                                                else -> "暂停缓存"
                                            },
                                        )
                                    }
                                },
                                modifier = Modifier.combinedClickable(
                                    onClick = { onPlay(item) },
                                    onLongClick = { pendingDelete = item },
                                ),
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("清空全部缓存？") },
            text = { Text("已缓存和正在缓存的节目都会被删除。") },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("取消") }
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmClear = false
                    onClear()
                }) { Text("清空") }
            },
        )
    }
    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除缓存？") },
            text = { Text("将删除“${item.title} · ${item.episodeName}”及已缓存的部分。") },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = null
                    onDelete(item.id)
                }) { Text("删除") }
            },
        )
    }
}

private fun cacheStatus(item: CachedEpisode): String {
    val size = when {
        item.bytesDownloaded >= 1024L * 1024 * 1024 ->
            "%.1f GB".format(item.bytesDownloaded / (1024f * 1024 * 1024))
        item.bytesDownloaded >= 1024L * 1024 ->
            "%.1f MB".format(item.bytesDownloaded / (1024f * 1024))
        else -> "${item.bytesDownloaded / 1024} KB"
    }
    return when (item.state) {
        Download.STATE_COMPLETED -> "已缓存 · $size"
        Download.STATE_DOWNLOADING -> {
            val progress = item.percent.takeIf { it >= 0f }?.let { " · ${it.toInt()}%" }.orEmpty()
            val speed = when {
                item.speedBytesPerSecond >= 1024L * 1024 ->
                    " · %.1f MB/s".format(item.speedBytesPerSecond / (1024f * 1024))
                item.speedBytesPerSecond >= 1024L ->
                    " · ${item.speedBytesPerSecond / 1024} KB/s"
                else -> ""
            }
            val threads = item.connections.takeIf { it > 0 }?.let { " · ${it}线程" }.orEmpty()
            "缓存中$progress · $size$threads$speed"
        }
        Download.STATE_QUEUED -> "等待缓存 · $size"
        Download.STATE_STOPPED -> "已暂停 · $size"
        Download.STATE_FAILED -> "缓存失败 · 已保留 $size"
        Download.STATE_REMOVING -> "正在删除"
        Download.STATE_RESTARTING -> "正在重新开始"
        else -> size
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    theme: ThemeMode,
    dynamicColor: Boolean,
    customThemeColor: Int,
    listState: LazyListState,
    modifier: Modifier,
    onTheme: (ThemeMode) -> Unit,
    onDynamicColor: (Boolean) -> Unit,
    onCustomThemeColor: (Int) -> Unit,
) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val updater = remember { AppUpdater(context.applicationContext) }
    var colorDialog by remember { mutableStateOf(false) }
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
        Box(Modifier.fillMaxSize().padding(padding)) {
        LazyColumn(
            Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
                .align(Alignment.TopCenter),
            state = listState,
        ) {
            item { SettingsHeader("外观") }
            item {
                ThemeModeSetting(theme = theme, onTheme = onTheme)
            }
            item {
                ListItem(
                    headlineContent = { Text("莫奈取色") },
                    supportingContent = { Text("根据系统壁纸自动生成主题配色") },
                    leadingContent = { Icon(Icons.Default.Palette, null) },
                    trailingContent = {
                        Switch(
                            checked = dynamicColor,
                            onCheckedChange = onDynamicColor,
                        )
                    },
                    modifier = Modifier.clickable { onDynamicColor(!dynamicColor) },
                )
            }
            item {
                AnimatedVisibility(visible = !dynamicColor) {
                    ListItem(
                        headlineContent = { Text("主题色") },
                        supportingContent = { Text("关闭莫奈取色时使用") },
                        leadingContent = {
                            Box(
                                Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(customThemeColor))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                            )
                        },
                        trailingContent = { Text("›", style = MaterialTheme.typography.titleLarge) },
                        modifier = Modifier.clickable { colorDialog = true },
                    )
                }
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
                    if (checkingUpdate) "正在检查更新…" else "当前版本 ${BuildConfig.VERSION_NAME}",
                    clickable = !checkingUpdate,
                ) {
                    checkingUpdate = true
                    scope.launch {
                        val result = runCatching { updater.check() }
                        checkingUpdate = false
                        result
                            .onSuccess { info ->
                                if (updater.isNewer(info.version)) availableUpdate = info
                                else snackbar.showSnackbar("已是最新版本 ${BuildConfig.VERSION_NAME}")
                            }
                            .onFailure { snackbar.showSnackbar(it.message ?: "检查更新失败") }
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
                SettingsRow(Icons.Default.Info, "版本", BuildConfig.VERSION_NAME, clickable = false) { }
            }
        }
        }
    }

    if (colorDialog) ThemeColorDialog(
        initialColor = Color(customThemeColor),
        onDismiss = { colorDialog = false },
        onConfirm = {
            onCustomThemeColor(it.toArgb())
            colorDialog = false
        },
    )

    availableUpdate?.let { info ->
        AlertDialog(
            onDismissRequest = { availableUpdate = null },
            title = { Text("发现新版本 ${info.version}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(info.name.ifBlank { "GitHub Release ${info.version}" })
                    if (info.notes.isNotBlank()) {
                        Text("更新内容", style = MaterialTheme.typography.titleSmall)
                        Text(info.notes)
                    }
                }
            },
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
private fun ThemeModeSetting(theme: ThemeMode, onTheme: (ThemeMode) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text("APP 风格") },
            supportingContent = { Text("设置应用的明暗外观") },
            leadingContent = {
                Icon(
                    when (theme) {
                        ThemeMode.DARK -> Icons.Default.DarkMode
                        ThemeMode.LIGHT -> Icons.Default.LightMode
                        ThemeMode.SYSTEM -> Icons.Default.Settings
                    },
                    null,
                )
            },
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 56.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(
                ThemeMode.SYSTEM to "自动",
                ThemeMode.LIGHT to "亮色",
                ThemeMode.DARK to "暗色",
            ).forEach { (mode, label) ->
                val selected = theme == mode
                OutlinedButton(
                    onClick = { onTheme(mode) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        },
                        contentColor = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    ),
                ) {
                    Text(label)
                }
            }
        }
    }
}

@Composable
private fun ThemeColorDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (Color) -> Unit,
) {
    var red by remember(initialColor) { mutableFloatStateOf(initialColor.red) }
    var green by remember(initialColor) { mutableFloatStateOf(initialColor.green) }
    var blue by remember(initialColor) { mutableFloatStateOf(initialColor.blue) }
    val color = Color(red, green, blue)
    val presets = listOf(
        Color(0xFF216DFF),
        Color(0xFF6750A4),
        Color(0xFF008577),
        Color(0xFF3F7D20),
        Color(0xFFC2410C),
        Color(0xFFB3261E),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择主题色") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(color)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                )
                ThemeColorSlider("红", red, Color.Red) { red = it }
                ThemeColorSlider("绿", green, Color.Green) { green = it }
                ThemeColorSlider("蓝", blue, Color.Blue) { blue = it }
                Text(
                    "#${color.toArgb().toUInt().toString(16).takeLast(6).uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("预设颜色", style = MaterialTheme.typography.labelMedium)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    presets.forEach { preset ->
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(preset)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable {
                                    red = preset.red
                                    green = preset.green
                                    blue = preset.blue
                                },
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(color) }) { Text("确定") }
        },
    )
}

@Composable
private fun ThemeColorSlider(
    label: String,
    value: Float,
    trackColor: Color,
    onValueChange: (Float) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(24.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
        )
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(trackColor),
        )
        Text(
            "${(value * 255).roundToInt()}",
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall,
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
private fun IKanTheme(
    mode: ThemeMode,
    useDynamicColor: Boolean,
    customThemeColor: Int,
    paletteRevision: Int,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val systemDark = (LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val dark = mode == ThemeMode.DARK || (mode == ThemeMode.SYSTEM && systemDark)
    val baseColors = if (useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Reading the revision is intentional: it invalidates this calculation after returning
        // from the wallpaper/theme picker, including on systems that keep the Activity alive.
        paletteRevision
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        dynamicColorScheme(
            seedColor = Color(customThemeColor),
            isDark = dark,
            isAmoled = false,
        )
    }
    val colors: ColorScheme = if (useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Navigation and filter chips use secondaryContainer by default. Map those prominent
        // selected states to the wallpaper's primary family so Monet is visible at a glance.
        baseColors.copy(
            secondaryContainer = baseColors.primaryContainer,
            onSecondaryContainer = baseColors.onPrimaryContainer,
        )
    } else {
        baseColors
    }
    SideEffect {
        val activity = context as? ComponentActivity ?: return@SideEffect
        WindowCompat.getInsetsController(activity.window, view).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
    MaterialTheme(colorScheme = colors) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.background,
            content = content,
        )
    }
}
