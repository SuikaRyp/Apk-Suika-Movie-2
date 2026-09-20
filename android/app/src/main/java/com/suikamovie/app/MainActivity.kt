package com.suikamovie.app

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.suikamovie.app.ui.SuikaMovieApp
import com.suikamovie.app.ui.theme.SuikaMovieTheme

/**
 * Entry point native SuikaMovie. Dulu app ini WebView (Capacitor) yang
 * muterin HTML/CSS/JS; sekarang full native pake Jetpack Compose - satu-
 * satunya WebView yang tersisa cuma di layar player video (lihat
 * ui/screens/detail/PlayerWebView.kt), soalnya server streaming pihak
 * ketiga (vidlink.pro dkk) cuma bisa dibuka lewat iframe/WebView, bukan
 * player video native biasa.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash screen native (pake drawable/splash.png yang udah ada),
        // WAJIB dipanggil SEBELUM super.onCreate().
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setupImmersiveNavBar()

        setContent {
            SuikaMovieTheme {
                CompositionLocalProvider(LocalMainActivity provides this) {
                    SuikaMovieApp()
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setupImmersiveNavBar()
        }
    }

    /**
     * Sembunyikan status bar & navigation bar begitu app dibuka
     * (immersive sticky, full screen). Kedua bar cuma nongol sebentar
     * kalau user swipe dari tepi layar (atas ke bawah / bawah ke atas),
     * lalu otomatis ilang lagi lewat animasi swipe-nya sendiri — ini
     * perilaku bawaan Android BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE.
     * Ikon/teks status bar dipaksa putih (light content) buat jaga-jaga
     * pas lagi kebuka sementara karena di-swipe.
     */
    private fun setupImmersiveNavBar() {
        val controller: WindowInsetsControllerCompat =
            WindowCompat.getInsetsController(window, window.decorView)

        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // false = ikon status bar warna putih (light content)
        controller.isAppearanceLightStatusBars = false
    }

    /**
     * Dipanggil dari layar player (DetailScreen) pas video di-fullscreen-in.
     * Ini pengganti langsung dari OrientationLockPlugin (Capacitor) yang
     * dulu dipanggil dari JS lewat window.Capacitor.Plugins.OrientationLock -
     * sekarang manggil API Android asli secara langsung, nggak perlu lapisan
     * plugin/bridge apa pun lagi.
     */
    fun lockLandscape() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    fun unlockOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}

/**
 * CompositionLocal buat ngasih akses ke instance MainActivity dari mana aja
 * di dalam tree Compose - dipakai khusus buat manggil lockLandscape() /
 * unlockOrientation() dari layar player, dan buat manggil requestFullscreen
 * gaya Android (hide/show system bars) pas mode fullscreen player aktif.
 */
val LocalMainActivity = staticCompositionLocalOf<MainActivity> {
    error("LocalMainActivity belum di-provide - pastikan dipanggil di dalam SuikaMovieApp()")
}
