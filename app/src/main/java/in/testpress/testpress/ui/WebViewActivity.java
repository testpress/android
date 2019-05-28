package in.testpress.testpress.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.TestpressService;

public class WebViewActivity extends BaseToolBarActivity {

    private static final String TAG = WebViewActivity.class.getSimpleName();
    public static final String URL_TO_OPEN = "URL";
    public static final String ACTIVITY_TITLE = "TITLE";
    public static final String SHOW_LOGOUT = "SHOW_LOGOUT";
    private final static int FILE_CHOOSER_RESULT_CODE = 1;

    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected TestpressService testpressService;
    @Inject protected LogoutService logoutService;

    private ProgressBar pb_loading;
    WebView webView;
    private String mCapturedMessage;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessages;
    private String url;
    private boolean showLogout;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (Build.VERSION.SDK_INT >= 21) {
            Uri[] results = null;

            //Check if response is positive
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == FILE_CHOOSER_RESULT_CODE) {

                    if (null == mUploadMessages) {
                        return;
                    }
                    if (intent == null) {
                        //Capture Photo if no image available
                        if (mCapturedMessage != null) {
                            results = new Uri[]{Uri.parse(mCapturedMessage)};
                        }
                    } else {
                        String dataString = intent.getDataString();
                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }
            }
            mUploadMessages.onReceiveValue(results);
            mUploadMessages = null;
        } else {

            if (requestCode == FILE_CHOOSER_RESULT_CODE) {
                if (null == mUploadMessage) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        setContentView(R.layout.generic_webview_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pb_loading = this.findViewById(R.id.pb_loading);

        Intent intent = getIntent();

        if (intent.hasExtra(URL_TO_OPEN) && intent.getExtras().getString(URL_TO_OPEN) != "") {
            setUrl(intent.getExtras().getString(URL_TO_OPEN));
        } else {
            finish();
        }

        if (intent.hasExtra(SHOW_LOGOUT) && intent.getExtras().getString(SHOW_LOGOUT) != "" && Boolean.parseBoolean(intent.getExtras().getString(SHOW_LOGOUT)) == true) {
            showLogout = true;
        }

        if (intent.hasExtra(ACTIVITY_TITLE) && intent.getExtras().getString(ACTIVITY_TITLE) != "") {
            getSupportActionBar().setTitle(intent.getExtras().getString(ACTIVITY_TITLE));
        }


        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(WebViewActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }

        webView = (WebView) findViewById(R.id.web_view);

        if (webView == null) {
            finish();
        }

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);

        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.setMixedContentMode(0);
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT < 19) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        // webView.setWebViewClient(new Callback());
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                pb_loading.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(webView, url);
                pb_loading.setVisibility(View.GONE);
            }
        });

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeAllCookie();

        webView.loadUrl(url);
        webView.setWebChromeClient(new WebChromeClient() {

            //For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {

                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                WebViewActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILE_CHOOSER_RESULT_CODE);
            }

            // For Android 3.0+, above method not supported in some android 3+ versions, in such case we use this
            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {

                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                WebViewActivity.this.startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FILE_CHOOSER_RESULT_CODE);
            }

            //For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {

                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                WebViewActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), WebViewActivity.FILE_CHOOSER_RESULT_CODE);
            }

            //For Android 5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {

                if (mUploadMessages != null) {
                    mUploadMessages.onReceiveValue(null);
                }

                mUploadMessages = filePathCallback;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(WebViewActivity.this.getPackageManager()) != null) {

                    File photoFile = null;

                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCapturedMessage);
                    } catch (IOException ex) {
                        Log.e(TAG, "Image file creation failed", ex);
                    }
                    if (photoFile != null) {
                        mCapturedMessage = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*");
                Intent[] intentArray;

                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE);

                return true;
            }
        });
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // Create an image file
    private File createImageFile() throws IOException {

        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN) {

            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:

                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }

                    return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public class Callback extends WebViewClient {
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(getApplicationContext(), "Failed loading app!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (showLogout) {
            getMenuInflater().inflate(R.menu.logout, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirm_message)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                serviceProvider.logout(WebViewActivity.this, testpressService, serviceProvider, logoutService);
                            }
                        })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
