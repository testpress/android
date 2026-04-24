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
    public static final String ARG_EMAIL = "email";
    public static final String ARG_PASS = "pass";

    public static CustomWebViewFragment newInstance(String email, String pass) {
        CustomWebViewFragment fragment = new CustomWebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_PASS, pass);
        fragment.setArguments(args);
        return fragment;
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
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        
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
                if (url.startsWith("upi://") || url.contains("upi://") || 
                    url.startsWith("gpay://") || url.startsWith("phonepe://") || 
                    url.startsWith("paytm://")) {
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
        
        String url = Constants.Http.EPRATIBHA_SSO_URL;
        if (getArguments() != null) {
            String email = getArguments().getString(ARG_EMAIL, "");
            String pass = getArguments().getString(ARG_PASS, "");
            if (!email.isEmpty()) {
                url += "?email=" + email + "&pass=" + pass;
            }
        }
        webView.loadUrl(url);
        
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