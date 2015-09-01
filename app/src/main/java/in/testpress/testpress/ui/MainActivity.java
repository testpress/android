

package in.testpress.testpress.ui;
;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import in.testpress.testpress.BuildConfig;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Update;
import in.testpress.testpress.util.SafeAsyncTask;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import javax.inject.Inject;

/**
 * Initial activity for the application.
 *
 * If you need to remove the authentication from the application please see
 * {@link in.testpress.testpress.authenticator.ApiKeyProvider#getAuthKey(android.app.Activity)}
 */
public class MainActivity extends NavigationDrawer {

    @Inject protected TestpressService testpressService;

    protected void initScreen() {
        if (userHasAuthenticated) {
            final Intent intent = getIntent();
            Bundle data = intent.getExtras();
            CarouselFragment fragment = new CarouselFragment();
            fragment.setArguments(data);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commitAllowingStateLoss();
        }
    }

    private void checkUpdate() {
        new SafeAsyncTask<Update>() {
            @Override
            public Update call() throws Exception {
                return testpressService.checkUpdate("" + BuildConfig.VERSION_CODE);
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                checkAuth();
            }

            @Override
            protected void onSuccess(final Update update) throws Exception {
                if(update.getUpdateRequired()) {
                    new MaterialDialog.Builder(MainActivity.this)
                            .cancelable(true)
                            .content(update.getMessage())
                            .cancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialogInterface) {
                                    if (update.getForce()) {
                                        finish();
                                    } else {
                                        checkAuth();
                                    }
                                }
                            })
                            .neutralText("Update")
                            .buttonsGravity(GravityEnum.CENTER)
                            .neutralColorRes(R.color.primary)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onNeutral(MaterialDialog dialog) {
                                    dialog.cancel();
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "in.testpress.testpress")));
                                    //Should change "in.testpress.testpress" to "in.testpress.<App name>" for different apps
                                    finish();
                                }
                            })
                            .show();
                } else checkAuth();
            }
        }.execute();
    }

    @Override
    public void onInt(Bundle savedInstanceState) {
        super.onInt(savedInstanceState);
        Injector.inject(this);
        checkUpdate();
        with(this)
                .startingPosition(0) //Starting position in the list
                .addAllHelpItem(mHelpLiveo.getHelp())
                .backgroundList(R.color.native_background)
                .build();
    }

    @Override
    public void onItemClick(int position) {
        if(position == 0) {
            initScreen();
        } else {
            super.onItemClick(position);
        }
    }
}
