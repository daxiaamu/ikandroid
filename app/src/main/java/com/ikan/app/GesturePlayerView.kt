package com.ikan.app

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import android.media.AudioManager
import android.provider.Settings
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.C
import androidx.media3.ui.PlayerView
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign

class GesturePlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : PlayerView(context, attrs, defStyleAttr) {
    var onGestureFeedback: ((String?) -> Unit)? = null
    var onControllerChromeProgress: ((Float) -> Unit)? = null
    var onFullscreenChromeInsetChanged: ((Float) -> Unit)? = null

    private enum class GestureMode { NONE, BRIGHTNESS, VOLUME, SEEK }

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private var mode = GestureMode.NONE
    private var downX = 0f
    private var downY = 0f
    private var startBrightness = 0.5f
    private var startVolume = 0
    private var startPosition = 0L
    private var targetPosition = 0L
    private var nativeProgressGesture = false
    private var onBackAction: (() -> Unit)? = null
    private var onCastAction: (() -> Unit)? = null
    private var onSpeedAction: (() -> Unit)? = null
    private var onAudioAction: (() -> Unit)? = null
    private var onCacheAction: (() -> Unit)? = null
    private var onPipAction: (() -> Unit)? = null
    private var onFullscreenAction: (() -> Unit)? = null
    private var actionTargetsVisible = false
    private var actionTargetsFullscreen = false
    private var audioActionVisible = false
    private var pendingAction: (() -> Unit)? = null
    private var pendingActionCancelled = false
    private var pendingActionDownX = 0f
    private var pendingActionDownY = 0f
    private val topBackHitTarget by lazy {
        actionHitTarget("返回") { onBackAction?.invoke() }
    }
    private val topActionsHitTarget by lazy {
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(actionHitTarget("缓存") { onCacheAction?.invoke() })
            addView(actionHitTarget("DLNA 投屏") { onCastAction?.invoke() })
            addView(actionHitTarget("音轨") { onAudioAction?.invoke() })
            addView(actionHitTarget("播放速度") { onSpeedAction?.invoke() })
        }
    }
    private var actionTargetsAttached = false
    private var lastControllerChromeProgress = -1f
    private val controllerChromeObserver = ViewTreeObserver.OnPreDrawListener {
        dispatchControllerChromeProgress()
        true
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            clearPendingAction()
            nativeProgressGesture = false
            val progressHit = isNativeProgressTouch(event.x, event.y)
            val action = resolveAction(event.x, event.y)
            if (progressHit) {
                nativeProgressGesture = true
                mode = GestureMode.NONE
                return super.dispatchTouchEvent(event)
            }
            action?.let {
                pendingAction = action
                pendingActionCancelled = false
                pendingActionDownX = event.x
                pendingActionDownY = event.y
                return true
            }
        } else {
            pendingAction?.let { action ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_MOVE -> {
                        if (maxOf(
                                abs(event.x - pendingActionDownX),
                                abs(event.y - pendingActionDownY),
                            ) > touchSlop
                        ) {
                            pendingActionCancelled = true
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        if (!pendingActionCancelled &&
                            resolveAction(event.x, event.y) === action
                        ) {
                            action()
                        }
                        clearPendingAction()
                    }

                    MotionEvent.ACTION_CANCEL -> clearPendingAction()
                }
                return true
            }
        }
        if (event.actionMasked != MotionEvent.ACTION_DOWN && nativeProgressGesture) {
            val handled = super.dispatchTouchEvent(event)
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                nativeProgressGesture = false
            }
            return handled
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mode = GestureMode.NONE
                downX = event.x
                downY = event.y
                startBrightness = currentBrightness()
                startVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                startPosition = player?.currentPosition?.coerceAtLeast(0L) ?: 0L
                targetPosition = startPosition
                super.dispatchTouchEvent(event)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - downX
                val dy = event.y - downY
                if (mode == GestureMode.NONE && maxOf(abs(dx), abs(dy)) > touchSlop) {
                    mode = if (abs(dx) > abs(dy)) {
                        GestureMode.SEEK
                    } else if (downX < width / 2f) {
                        GestureMode.BRIGHTNESS
                    } else {
                        GestureMode.VOLUME
                    }
                    val cancel = MotionEvent.obtain(event).apply { action = MotionEvent.ACTION_CANCEL }
                    super.dispatchTouchEvent(cancel)
                    cancel.recycle()
                    hideController()
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
                when (mode) {
                    GestureMode.BRIGHTNESS -> adjustBrightness(dy)
                    GestureMode.VOLUME -> adjustVolume(dy)
                    GestureMode.SEEK -> previewSeek(dx)
                    GestureMode.NONE -> Unit
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (mode == GestureMode.SEEK && event.actionMasked == MotionEvent.ACTION_UP) {
                    previewSeek(event.x - downX)
                    player?.seekTo(targetPosition)
                }
                val wasGesture = mode != GestureMode.NONE
                mode = GestureMode.NONE
                parent?.requestDisallowInterceptTouchEvent(false)
                if (wasGesture) {
                    return true
                }
                return super.dispatchTouchEvent(event)
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onDetachedFromWindow() {
        clearPendingAction()
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.removeOnPreDrawListener(controllerChromeObserver)
        }
        super.onDetachedFromWindow()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnPreDrawListener(controllerChromeObserver)
        post(::dispatchControllerChromeProgress)
    }

    fun configureActionHitTargets(
        visible: Boolean,
        fullscreen: Boolean,
        onBack: () -> Unit,
        onCast: () -> Unit,
        onSpeed: () -> Unit,
        onAudio: () -> Unit,
        showAudio: Boolean,
        onCache: () -> Unit,
        onPip: () -> Unit,
        onFullscreen: () -> Unit,
    ) {
        onBackAction = onBack
        onCastAction = onCast
        onSpeedAction = onSpeed
        onAudioAction = onAudio
        audioActionVisible = showAudio
        onCacheAction = onCache
        onPipAction = onPip
        onFullscreenAction = onFullscreen
        actionTargetsVisible = visible
        actionTargetsFullscreen = fullscreen
        if (!actionTargetsAttached) {
            actionTargetsAttached = true
            addView(
                topBackHitTarget,
                LayoutParams(dp(56), dp(56), Gravity.TOP or Gravity.START),
            )
            addView(
                topActionsHitTarget,
                LayoutParams(dp(48) * 3, dp(48), Gravity.TOP or Gravity.END),
            )
        }
        topActionsHitTarget.getChildAt(2).visibility =
            if (showAudio) View.VISIBLE else View.GONE
        updateTopActionLayout()
        post(::updateTopActionLayout)
        topBackHitTarget.visibility = if (visible && fullscreen) View.VISIBLE else View.GONE
        topActionsHitTarget.visibility = if (visible) View.VISIBLE else View.GONE
        findViewById<View>(R.id.player_pip)?.apply {
            visibility = if (visible) View.VISIBLE else View.GONE
            setOnClickListener { onPipAction?.invoke() }
        }
        findViewById<View>(androidx.media3.ui.R.id.exo_fullscreen)?.apply {
            visibility = if (visible) View.VISIBLE else View.GONE
        }
        // Media3 reports setFullscreenButtonState through the same callback that it uses
        // for user clicks. Detach the listener while synchronizing the icon state so a
        // recomposition cannot be mistaken for another fullscreen request.
        setFullscreenButtonClickListener(null)
        setFullscreenButtonState(fullscreen)
        setFullscreenButtonClickListener { onFullscreenAction?.invoke() }
        bringChildToFront(topBackHitTarget)
        bringChildToFront(topActionsHitTarget)
    }

    private fun actionHitTarget(description: String, action: () -> Unit): View =
        View(context).apply {
            contentDescription = description
            isClickable = true
            isFocusable = true
            alpha = 0.01f
            layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
            setOnClickListener { action() }
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

    private fun resolveAction(x: Float, y: Float): (() -> Unit)? {
        if (!actionTargetsVisible) return null
        val button = dp(48).toFloat()
        val backButton = dp(56).toFloat()
        val (startInset, endInset) = topActionInsets()
        val backHitStart = (startInset - dp(4)).coerceAtLeast(0f)
        val topHeight = if (actionTargetsFullscreen) backButton else button
        if (y <= topHeight) {
            if (
                actionTargetsFullscreen &&
                x >= backHitStart &&
                x <= backHitStart + backButton
            ) {
                return onBackAction
            }
            val right = width - endInset
            if (x in (right - button)..right) return onSpeedAction
            var slot = 1
            if (audioActionVisible) {
                if (x in (right - button * 2)..(right - button)) return onAudioAction
                slot++
            }
            if (x in (right - button * (slot + 1))..(right - button * slot)) {
                return onCastAction
            }
            slot++
            if (x in (right - button * (slot + 1))..(right - button * slot)) {
                return onCacheAction
            }
        }
        return null
    }

    private fun dispatchControllerChromeProgress() {
        val bottomBar = findViewById<View>(androidx.media3.ui.R.id.exo_bottom_bar)
        val progress = when {
            bottomBar == null || bottomBar.visibility != View.VISIBLE -> 0f
            bottomBar.height <= 0 -> 1f
            else -> (1f - bottomBar.translationY / bottomBar.height).coerceIn(0f, 1f)
        }
        if (abs(progress - lastControllerChromeProgress) < 0.005f) return
        lastControllerChromeProgress = progress
        onControllerChromeProgress?.invoke(progress)
    }

    private fun updateTopActionLayout() {
        val (startInset, endInset) = topActionInsets()
        (topBackHitTarget.layoutParams as? LayoutParams)?.let { params ->
            params.width = dp(56)
            params.height = dp(56)
            params.gravity = Gravity.TOP or Gravity.START
            params.marginStart = (startInset - dp(4)).coerceAtLeast(0f).roundToInt()
            topBackHitTarget.layoutParams = params
        }
        (topActionsHitTarget.layoutParams as? LayoutParams)?.let { params ->
            params.width = dp(48) * (if (audioActionVisible) 4 else 3)
            params.height = dp(48)
            params.gravity = Gravity.TOP or Gravity.END
            params.marginEnd = endInset.roundToInt()
            topActionsHitTarget.layoutParams = params
        }
        findViewById<View>(androidx.media3.ui.R.id.exo_bottom_bar)?.let { bottomBar ->
            val symmetricInset = if (actionTargetsFullscreen) {
                (startInset + dp(4)).roundToInt()
            } else {
                0
            }
            bottomBar.setPadding(
                symmetricInset,
                bottomBar.paddingTop,
                symmetricInset,
                bottomBar.paddingBottom,
            )
            if (actionTargetsFullscreen) {
                // The bottom buttons use 40 dp cells while the top buttons use 48 dp cells.
                // Sharing this exact content edge, minus their 4 dp half-width difference,
                // keeps the visual centers aligned on devices with asymmetric gesture insets.
                onFullscreenChromeInsetChanged?.invoke(symmetricInset - dp(4).toFloat())
            }
        }
    }

    private fun topActionInsets(): Pair<Float, Float> {
        if (!actionTargetsFullscreen) return 0f to 0f
        val spacing = dp(12).toFloat()
        val insets = ViewCompat.getRootWindowInsets(this)?.getInsets(
            WindowInsetsCompat.Type.displayCutout() or
                WindowInsetsCompat.Type.systemGestures(),
        )
        val safeInset = maxOf(insets?.left ?: 0, insets?.right ?: 0).toFloat()
        val symmetricInset = safeInset + spacing
        return symmetricInset to symmetricInset
    }

    private fun adjustBrightness(dy: Float) {
        val value = (startBrightness - dy / height.coerceAtLeast(1)).coerceIn(0.01f, 1f)
        context.findActivity()?.window?.let { window ->
            window.attributes = window.attributes.apply { screenBrightness = value }
        }
        onGestureFeedback?.invoke("亮度 ${(value * 100).roundToInt()}%")
    }

    private fun adjustVolume(dy: Float) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val delta = (-dy / height.coerceAtLeast(1) * maxVolume).roundToInt()
        val volume = (startVolume + delta).coerceIn(0, maxVolume)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
        onGestureFeedback?.invoke("音量 ${(volume * 100f / maxVolume).roundToInt()}%")
    }

    private fun previewSeek(dx: Float) {
        val duration = player?.duration?.takeUnless { it == C.TIME_UNSET || it <= 0 } ?: return
        val seekRange = (duration / 10L).coerceIn(120_000L, 600_000L).coerceAtMost(duration)
        val fraction = (dx / width.coerceAtLeast(1)).coerceIn(-1f, 1f)
        val curvedFraction = fraction.sign * abs(fraction).pow(1.35f)
        targetPosition = (startPosition + (curvedFraction * seekRange).toLong())
            .coerceIn(0L, duration)
        val delta = targetPosition - startPosition
        val direction = if (delta >= 0) "快进" else "快退"
        onGestureFeedback?.invoke(
            "$direction ${formatDuration(abs(delta))}\n${formatTime(targetPosition)} / ${formatTime(duration)}",
        )
    }

    private fun isNativeProgressTouch(x: Float, y: Float): Boolean {
        val progress = findViewById<View>(androidx.media3.ui.R.id.exo_progress) ?: return false
        if (progress.visibility != View.VISIBLE || !progress.isShown) return false
        val progressLocation = IntArray(2)
        val playerLocation = IntArray(2)
        progress.getLocationOnScreen(progressLocation)
        getLocationOnScreen(playerLocation)
        val rect = Rect(
            progressLocation[0] - playerLocation[0],
            progressLocation[1] - playerLocation[1],
            progressLocation[0] - playerLocation[0] + progress.width,
            progressLocation[1] - playerLocation[1] + progress.height,
        )
        return rect.contains(x.roundToInt(), y.roundToInt())
    }

    private fun clearPendingAction() {
        pendingAction = null
        pendingActionCancelled = false
    }

    private fun currentBrightness(): Float {
        val windowValue = context.findActivity()?.window?.attributes?.screenBrightness ?: -1f
        if (windowValue >= 0f) return windowValue
        return runCatching {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255f
        }.getOrDefault(0.5f).coerceIn(0.01f, 1f)
    }

    private fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1_000
        val hours = totalSeconds / 3_600
        val minutes = totalSeconds % 3_600 / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
        else "%02d:%02d".format(minutes, seconds)
    }

    private fun formatDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1_000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return if (minutes > 0) "${minutes}分${seconds.toString().padStart(2, '0')}秒"
        else "${seconds}秒"
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
