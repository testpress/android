package in.testpress.testpress.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import javax.inject.Inject;

import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.TestpressService;

public class MainMenuActivity extends TestpressFragmentActivity {

    @Inject protected TestpressService testpressService;
    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MainMenuFragment mainMenuFragment = new MainMenuFragment();
        Bundle bundle = getIntent().getExtras();
        mainMenuFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mainMenuFragment).commitAllowingStateLoss();
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
                                serviceProvider.logout(MainMenuActivity.this, testpressService,
                                        serviceProvider, logoutService);
                            }
                        })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
