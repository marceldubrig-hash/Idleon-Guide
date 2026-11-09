package de.ubrig.idleonatlas;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

public class MainActivity extends Activity {
    private static final String ORIGIN = "https://appassets.androidplatform.net";
    private static final String START = ORIGIN + "/assets/site/index.html";
    private static final int IMPORT = 101, EXPORT = 102;
    WebView webView;
    private ValueCallback<Uri[]> fileCallback;

    @Override @SuppressLint("SetJavaScriptEnabled")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(245,244,237));
        setContentView(root);
        // Explicit insets work on target-35 edge-to-edge, rotation and fold resizing.
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            } else {
                view.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            }
            return insets;
        });
        if (Build.VERSION.SDK_INT >= 30) getWindow().setDecorFitsSystemWindows(false);
        webView = new WebView(this);
        webView.setId(View.generateViewId());
        webView.setBackgroundColor(Color.rgb(245,244,237));
        root.addView(webView,new FrameLayout.LayoutParams(-1,-1));
        WebSettings settings=webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true); // Only URIs selected in the Android document picker.
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setSupportMultipleWindows(false);
        WebView.setWebContentsDebuggingEnabled(false);
        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
        webView.setWebViewClient(new WebViewClientCompat(){
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                WebResourceResponse response=loader.shouldInterceptRequest(request.getUrl());
                if(response!=null)return response;
                // No remote content or scripts can run inside the trusted app origin.
                return new WebResourceResponse("text/plain","UTF-8",403,"Blocked",Collections.emptyMap(),new ByteArrayInputStream(new byte[0]));
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri=request.getUrl();
                if(isLocal(uri))return false;
                if(request.isForMainFrame() && ("https".equals(uri.getScheme())||"http".equals(uri.getScheme())))openExternal(uri);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onShowFileChooser(WebView view,ValueCallback<Uri[]> callback,FileChooserParams params){
                if(fileCallback!=null)fileCallback.onReceiveValue(null);
                fileCallback=callback;
                Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("application/json");
                try{startActivityForResult(intent,IMPORT);}catch(ActivityNotFoundException e){fileCallback.onReceiveValue(null);fileCallback=null;notice("Keine Dateiauswahl verfügbar.");}
                return true;
            }
        });
        if(WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            WebViewCompat.addWebMessageListener(webView,"nativeBackup",Collections.singleton(ORIGIN),
                    (view,message,origin,mainFrame,reply)->{
                        if(mainFrame&&ORIGIN.equals(origin.toString()))runOnUiThread(()->exportBackup(message.getData()));
                    });
        }
        webView.setDownloadListener((url,userAgent,disposition,mimetype,length)->{
            Uri uri=Uri.parse(url);
            if(isLocal(uri))notice("Diese Rohdaten findest du auch im GitHub-Repo unter public/data.");
            else if("https".equals(uri.getScheme()))openExternal(uri);
            else notice("Bitte nutze den Backup-Export unter Mein Abenteuer.");
        });
        if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::navigateBack);
        if(savedInstanceState==null||webView.restoreState(savedInstanceState)==null)webView.loadUrl(START);
        root.requestApplyInsets();
    }
    private static boolean isLocal(Uri uri){return "https".equals(uri.getScheme())&&"appassets.androidplatform.net".equals(uri.getHost())&&uri.getPath()!=null&&uri.getPath().startsWith("/assets/site/");}
    private void openExternal(Uri uri){try{startActivity(new Intent(Intent.ACTION_VIEW,uri));}catch(ActivityNotFoundException e){notice("Kein Browser zum Öffnen des Links gefunden.");}}
    private void exportBackup(String json){
        try {
            if(json==null||json.length()>1000000)throw new IllegalArgumentException();
            JSONObject backup=new JSONObject(json);
            if(backup.getInt("schema")!=1)throw new IllegalArgumentException();
            Files.write(new java.io.File(getCacheDir(),"pending-backup.json").toPath(),json.getBytes(StandardCharsets.UTF_8));
            startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("application/json").putExtra(Intent.EXTRA_TITLE,"idleon-atlas-fortschritt.json"),EXPORT);
        }catch(Exception e){notice("Backup konnte nicht vorbereitet werden.");}
    }
    @Override public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==IMPORT&&fileCallback!=null){fileCallback.onReceiveValue(resultCode==RESULT_OK&&data!=null&&data.getData()!=null?new Uri[]{data.getData()}:null);fileCallback=null;}
        if(requestCode==EXPORT&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){
            java.io.File pending=new java.io.File(getCacheDir(),"pending-backup.json");
            try(OutputStream out=getContentResolver().openOutputStream(data.getData())){
                if(out==null)throw new IllegalStateException();
                out.write(Files.readAllBytes(pending.toPath()));notice("Backup gespeichert.");
            }catch(Exception e){notice("Backup konnte nicht gespeichert werden.");}
            pending.delete();
        }
    }
    private void navigateBack(){if(webView.canGoBack())webView.goBack();else finish();}
    @Override @SuppressWarnings("deprecation") public void onBackPressed(){navigateBack();}
    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);webView.saveState(out);}
    private void notice(String text){Toast.makeText(this,text,Toast.LENGTH_LONG).show();}
    @Override protected void onDestroy(){if(fileCallback!=null)fileCallback.onReceiveValue(null);if(webView!=null){webView.stopLoading();webView.destroy();}super.onDestroy();}
}
