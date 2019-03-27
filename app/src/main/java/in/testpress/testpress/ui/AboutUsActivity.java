package in.testpress.testpress.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.InstituteSettings;
import in.testpress.testpress.models.InstituteSettingsDao;
import in.testpress.testpress.util.Strings;
import in.testpress.util.WebViewUtils;

import static in.testpress.testpress.BuildConfig.BASE_URL;

public class AboutUsActivity extends BaseToolBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        WebView webView = findViewById(in.testpress.course.R.id.web_view);
        webView.loadData(getAboutUs(), "text/html", "UTF-8");
    }

    public String getAboutUs() {
        DaoSession daoSession = TestpressApplication.getDaoSession();

        InstituteSettingsDao instituteSettingsDao = daoSession.getInstituteSettingsDao();
        InstituteSettings instituteSettings = instituteSettingsDao.queryBuilder()
                .where(InstituteSettingsDao.Properties.BaseUrl.eq(BASE_URL))
                .list().get(0);

        return Strings.toString(instituteSettings.getAboutUs()) == "" ? "" : instituteSettings.getAboutUs();
    }

}
