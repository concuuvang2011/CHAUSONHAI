package vn.chausonhai.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.*;

public class MainActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> fileCb;
    private GeolocationPermissions.Callback geoCb;
    private String geoOrigin;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setGeolocationEnabled(true);
        s.setAllowFileAccess(true);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback cb) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    cb.invoke(origin, true, false);
                } else {
                    geoCb = cb; geoOrigin = origin;
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
            }
            @Override public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (fileCb != null) fileCb.onReceiveValue(null);
                fileCb = cb;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(Intent.createChooser(i, "Chọn file KML/KMZ"), 2);
                return true;
            }
        });
        web.loadUrl("file:///android_asset/index.html");
    }

    @Override public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        if (code == 1 && geoCb != null) {
            boolean ok = res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED;
            geoCb.invoke(geoOrigin, ok, false);
            geoCb = null;
        }
    }

    @Override protected void onActivityResult(int code, int result, Intent data) {
        if (code == 2 && fileCb != null) {
            Uri[] r = (result == RESULT_OK && data != null && data.getData() != null) ? new Uri[]{data.getData()} : null;
            fileCb.onReceiveValue(r);
            fileCb = null;
        }
    }

    @Override public void onBackPressed() { if (web.canGoBack()) web.goBack(); else super.onBackPressed(); }
}
