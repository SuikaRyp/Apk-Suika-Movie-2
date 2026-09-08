package com.suikamovie.app;

import android.os.Bundle;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Plugin native custom (rotasi layar paksa) WAJIB didaftarin SEBELUM
        // super.onCreate(), sesuai pola registrasi plugin Capacitor.
        registerPlugin(OrientationLockPlugin.class);
        super.onCreate(savedInstanceState);
        setupImmersiveNavBar();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setupImmersiveNavBar();
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
    private void setupImmersiveNavBar() {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

            // false = ikon status bar warna putih (light content)
            controller.setAppearanceLightStatusBars(false);
        }
    }
}
