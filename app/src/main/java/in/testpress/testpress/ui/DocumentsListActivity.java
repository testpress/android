package in.testpress.testpress.ui;

import android.os.Bundle;

import com.github.kevinsawicki.wishlist.Toaster;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import in.testpress.testpress.R;
import in.testpress.testpress.authenticator.LoginActivity;
import in.testpress.testpress.events.CustomErrorEvent;
import in.testpress.testpress.events.UnAuthorizedErrorEvent;
import in.testpress.testpress.util.CommonUtils;

public class DocumentsListActivity extends BaseAuthenticatedActivity {

    @Inject
    Bus bus;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout_material);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getStringExtra("title") != "") {
            getSupportActionBar().setTitle(getIntent().getStringExtra("title"));
        }

        if (CommonUtils.isUserAuthenticated(this)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DocumentsListFragment())
                    .commitAllowingStateLoss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }


    @Subscribe
    public void onCustomErrorEvent(CustomErrorEvent customErrorEvent) {
        CommonUtils.showAlert(this, "Parallel Login Restriction", customErrorEvent.getDetail());
    }


}
