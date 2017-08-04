package in.testpress.testpress.ui;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.models.Order;
import in.testpress.testpress.models.ProductDetails;

public class PaymentSuccessActivity extends TestpressFragmentActivity {

    @Inject TestpressServiceProvider serviceProvider;
    @InjectView(R.id.orderId) TextView orderId;
    @InjectView(R.id.amount) TextView amount;
    @InjectView(R.id.bookMessage) TextView bookMessage;
    @InjectView(R.id.furtherDetails) TextView furtherDetails;
    @InjectView(R.id.paymentDetails) RelativeLayout paymentDetailsView;

    Intent intent;
    ProgressBar progressBar;
    Order order;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_success_layout);
        Injector.inject(this);
        ButterKnife.inject(this);
        order = getIntent().getParcelableExtra("order");
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_IN);
        orderId.setText("Order Id: " + order.getOrderId());
        amount.setText("Amount: ₹ " + order.getAmount());
        paymentDetailsView.setVisibility(View.GONE);
        ProductDetails productDetails = getIntent().getParcelableExtra("productDetails");
        if(!productDetails.getTypes().contains("Books")) {
            bookMessage.setVisibility(View.GONE);
        }
        furtherDetails.setText("Further Details check your mail\n("+order.getEmail()+")");
        progressBar.setVisibility(View.GONE);
        paymentDetailsView.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.continueButton) public void continuePurchase() {
        if(getIntent().getBooleanExtra(Constants.IS_DEEP_LINK, false)) {
            gotoProductListActivity();
        }
        finish();
    }

    @OnClick(R.id.home_button) public void gotoHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if(getIntent().getBooleanExtra(Constants.IS_DEEP_LINK, false)) {
            gotoProductListActivity();
            finish();
        } else {
            super.onBackPressed();
        }
    }

    private void gotoProductListActivity() {
        Intent intent = new Intent(this, ProductsListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.IS_DEEP_LINK, true);
        startActivity(intent);
    }
}
