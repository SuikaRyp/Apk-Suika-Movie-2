package com.suikamovie.app;

import android.os.Bundle;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
     * Sembunyikan navigation bar begitu app dibuka (immersive sticky).
     * Nav bar cuma nongol sebentar kalau user swipe dari tepi layar
     * (atas ke bawah / bawah ke atas), lalu otomatis ilang lagi lewat
     * animasi swipe-nya sendiri — ini perilaku bawaan Android
     * BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE. Status bar tetap tampil
     * seperti biasa, cuma ikon/teksnya dipaksa putih (light content)
     * biar kebaca jelas di atas header gelap aplikasi.
     */
    private void setupImmersiveNavBar() {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.navigationBars());
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

            // false = ikon status bar warna putih (light content)
            controller.setAppearanceLightStatusBars(false);
        }
    }
}
