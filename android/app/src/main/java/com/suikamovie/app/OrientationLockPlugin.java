package com.suikamovie.app;

import android.content.pm.ActivityInfo;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * Plugin native buat maksa rotasi layar Activity Android.
 *
 * Alasan plugin ini dibutuhin: JS Screen Orientation API
 * (screen.orientation.lock()) SERING NGGAK JALAN di Android System WebView
 * (WebView yang dipakai Capacitor), beda sama browser Chrome biasa. Jadi
 * walaupun web app kita udah bener manggil screen.orientation.lock("landscape")
 * pas video di-fullscreen-in, WebView-nya diem aja / reject silent, makanya
 * layar nggak pernah ke-rotate.
 *
 * Solusinya: rotasi dipaksa lewat native Android API
 * (Activity.setRequestedOrientation), yang jauh lebih reliable karena
 * langsung ngontrol si Activity-nya, bukan lewat WebView punya API browser.
 *
 * Dipanggil dari JS lewat: window.Capacitor.Plugins.OrientationLock
 */
@CapacitorPlugin(name = "OrientationLock")
public class OrientationLockPlugin extends Plugin {

    @PluginMethod
    public void lockLandscape(PluginCall call) {
        if (getActivity() == null) {
            call.reject("Activity tidak tersedia");
            return;
        }
        getActivity().runOnUiThread(() ->
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
        );
        call.resolve();
    }

    @PluginMethod
    public void unlock(PluginCall call) {
        if (getActivity() == null) {
            call.reject("Activity tidak tersedia");
            return;
        }
        getActivity().runOnUiThread(() ->
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        );
        call.resolve();
    }
}
