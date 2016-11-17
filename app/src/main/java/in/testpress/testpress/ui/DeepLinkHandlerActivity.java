package in.testpress.testpress.ui;

import android.content.Intent;
import android.view.MenuItem;

import in.testpress.testpress.core.Constants;

public class DeepLinkHandlerActivity extends TestpressFragmentActivity {

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            if (getIntent().getBooleanExtra(Constants.IS_DEEP_LINK, false)) {
                Intent intent = new Intent(DeepLinkHandlerActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                onBackPressed();
            }
            return true;
        }
        return false;
    }
}
