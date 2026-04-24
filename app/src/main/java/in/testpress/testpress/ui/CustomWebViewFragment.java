package in.testpress.testpress.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import in.testpress.testpress.core.Constants;

@SuppressLint("SetJavaScriptEnabled")
public class CustomWebViewFragment extends Fragment {

    private WebView webView;
    
    public static final String ARG_URL_TO_OPEN = "url_to_open";
    public static final String ARG_EMAIL = "email";
    public static final String ARG_PASS = "pass";

    public static CustomWebViewFragment newInstance(String url, String email, String pass) {
        CustomWebViewFragment fragment = new CustomWebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL_TO_OPEN, url);
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_PASS, pass);
        fragment.setArguments(args);
        return fragment;
    }

    public static CustomWebViewFragment newInstance(String email, String pass) {
        return newInstance(Constants.Http.EPRATIBHA_SSO_URL, email, pass);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable Bundle savedInstanceState) {
        webView = new WebView(requireContext());
        
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                return handleUpiUrl(url, view);
            }
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUpiUrl(url, view);
            }
            
            private boolean handleUpiUrl(String url, WebView view) {
                Uri uri = Uri.parse(url);
                String scheme = uri.getScheme();
                if ("upi".equals(scheme) || "gpay".equals(scheme) || 
                    "phonepe".equals(scheme) || "paytm".equals(scheme)) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        if (view.getContext().getPackageManager().resolveActivity(intent, 0) != null) {
                            view.getContext().startActivity(intent);
                        } else {
                            Toast.makeText(view.getContext(), "No UPI app found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(view.getContext(), "No UPI app found", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });
        
        String baseUrl = Constants.Http.EPRATIBHA_SSO_URL;
        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_URL_TO_OPEN)) {
                baseUrl = getArguments().getString(ARG_URL_TO_OPEN, Constants.Http.EPRATIBHA_SSO_URL);
            }
            String email = getArguments().getString(ARG_EMAIL, "");
            String pass = getArguments().getString(ARG_PASS, "");
            if (!email.isEmpty()) {
                Uri uri = Uri.parse(baseUrl)
                        .buildUpon()
                        .appendQueryParameter("email", email)
                        .appendQueryParameter("pass", pass)
                        .build();
                baseUrl = uri.toString();
            }
        }
        webView.loadUrl(baseUrl);
        
        return webView;
    }
    
    @Override
    public void onDestroyView() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroyView();
    }
}