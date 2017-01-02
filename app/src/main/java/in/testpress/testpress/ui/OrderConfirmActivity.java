package in.testpress.testpress.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Payu.PayuConstants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.TestpressExam;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.R.id;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Order;
import in.testpress.testpress.models.OrderItem;
import in.testpress.testpress.models.ProductDetails;
import in.testpress.testpress.ui.paymentGateway.PaymentModeActivity;
import in.testpress.testpress.util.InternetConnectivityChecker;
import in.testpress.testpress.util.SafeAsyncTask;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

public class OrderConfirmActivity extends TestpressFragmentActivity {

    @Inject TestpressService testpressService;
    @Inject TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;
    @InjectView(id.address) EditText address;
    @InjectView(id.zip) EditText zip;
    @InjectView(id.landmark) EditText landmark;
    @InjectView(id.phone) EditText phone;
    @InjectView(id.fill_all_details) TextView fillAllDetailsText;
    @InjectView(id.continueButton) Button continueButton;
    @InjectView(id.pb_loading) ProgressBar progressBar;
    @InjectView(id.shippingDetails) RelativeLayout shippingDetails;
    final TextWatcher watcher = validationTextWatcher();
    PaymentParams paymentParams;
    PayuConfig payuConfig;
    ProductDetails productDetails;
    Order order;
    OrderItem orderItem = new OrderItem();

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Injector.inject(this);
        setContentView(R.layout.shipping_address);
        ButterKnife.inject(this);
        progressBar.setVisibility(View.VISIBLE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        shippingDetails.setVisibility(View.GONE);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_IN);
        productDetails = getIntent().getParcelableExtra("productDetails");
        orderItem.setProduct(Constants.Http.URL_BASE + "/api/v2/products/" + productDetails.getSlug() + "/"); //now using v2 instead v2.1
        orderItem.setQuantity(1);
        orderItem.setPrice(productDetails.getPrice());
        final List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        new SafeAsyncTask<Order>() {

            @Override
            public Order call() throws Exception {
                AccountManager manager = AccountManager.get(OrderConfirmActivity.this);
                Account[] accounts = manager.getAccountsByType(Constants.Auth.TESTPRESS_ACCOUNT_TYPE);
                if(accounts.length != 0) {
                    return serviceProvider.getService(OrderConfirmActivity.this).order(Constants.Http.URL_USERS + accounts[0].name + "/", orderItems);
                } else {
                    serviceProvider.logout(OrderConfirmActivity.this, testpressService,
                            serviceProvider, logoutService);
                    throw new Exception("No Account exist");
                }
            }

            @Override
            protected void onSuccess(Order createdOrder) {
                order = createdOrder;
                if(createdOrder.getStatus().equals("Completed")) {
                    setResult(RESULT_OK);
                    if(productDetails.getExams().size() != 0) {
                        //noinspection ConstantConditions
                        TestpressExam.show(OrderConfirmActivity.this,
                                TestpressSdk.getTestpressSession(OrderConfirmActivity.this));
                    } else {
                        Intent intent = new Intent(OrderConfirmActivity.this, PaymentSuccessActivity.class);
                        intent.putExtra("order", order);
                        intent.putExtras(getIntent().getExtras());
                        startActivity(intent);
                    }
                    finish();
                } else if(productDetails.getRequiresShipping()) {
                    landmark.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        public boolean onEditorAction(final TextView v, final int actionId,
                                                      final KeyEvent event) {
                            if (actionId == IME_ACTION_DONE && continueButton.isEnabled()) {
                                continuePayment();
                                return true;
                            }
                            return false;
                        }
                    });
                    phone.setText(order.getPhone());
                    address.addTextChangedListener(watcher);
                    zip.addTextChangedListener(watcher);
                    phone.addTextChangedListener(watcher);
                    shippingDetails.setVisibility(View.VISIBLE);
                } else {
                    confirmOrder();
                }
            }

            @Override
            protected void onException(Exception e) {
                finish();
            }

            @Override
            protected void onFinally() {
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

    private TextWatcher validationTextWatcher() {
        return new TextWatcherAdapter() {
            public void afterTextChanged(final Editable EditTextBox) {
                updateUIWithValidation();
            }
        };
    }

    private void updateUIWithValidation() {
        final boolean populated = populated(address) && populated(zip) && populated(phone);
        if(populated) {
            fillAllDetailsText.setVisibility(View.GONE);
            continueButton.setEnabled(true);
        } else {
            fillAllDetailsText.setVisibility(View.VISIBLE);
            continueButton.setEnabled(false);
        }
    }

    private boolean populated(final EditText editText) {
        return editText.getText().toString().trim().length() > 0;
    }

    @OnClick(id.continueButton) public void continuePayment() {
        if(validate()) {
            InternetConnectivityChecker internetConnectivityChecker = new InternetConnectivityChecker(this);
            if (internetConnectivityChecker.isConnected()) {
                confirmOrder();
            } else {
                internetConnectivityChecker.showAlert();
            }
        }
    }

    private boolean validate() {
        //Phone number verification
        if (phone.getText().toString().trim().length() != 10) {
            phone.setError("This field may contain only 10 digit valid Mobile Numbers");
            phone.requestFocus();
            return false;
        }
        //pin code verification
        if (zip.getText().toString().trim().length() != 6) {
            zip.setError("Enter 6 digit valid pin number");
            zip.requestFocus();
            return false;
        }
        return true;
    }

    private void confirmOrder() {
        progressBar.setVisibility(View.VISIBLE);
        shippingDetails.setVisibility(View.GONE);
        new SafeAsyncTask<Order>() {

            @Override
            public Order call() throws Exception {
                return serviceProvider.getService(OrderConfirmActivity.this).orderConfirm(Constants.Http.URL_ORDERS_FRAG + order.getId() + "/confirm/", address.getText().toString(), zip.getText().toString(), phone.getText().toString(), landmark.getText().toString(), order.getUser(), order.getOrderItems());
            }

            @Override
            protected void onSuccess(Order order) {
                Intent intent = new Intent(OrderConfirmActivity.this, PaymentModeActivity.class);
                paymentParams = new PaymentParams();
                paymentParams.setKey(order.getApikey());
                paymentParams.setTxnId(order.getOrderId());
                paymentParams.setAmount(order.getAmount());
                paymentParams.setProductInfo("Testpress");
                paymentParams.setFirstName(order.getName());
                paymentParams.setEmail(order.getEmail());
                paymentParams.setUdf1("");
                paymentParams.setUdf2("");
                paymentParams.setUdf3("");
                paymentParams.setUdf4("");
                paymentParams.setUdf5("");
                paymentParams.setSurl("https://testpress.in/payments/response/payu/");
                paymentParams.setFurl("https://testpress.in/payments/response/payu/");
                payuConfig = new PayuConfig();
                payuConfig.setEnvironment(PayuConstants.PRODUCTION_ENV);
                PayuHashes payuHashes = new PayuHashes();
                payuHashes.setPaymentHash(order.getChecksum());
                payuHashes.setPaymentRelatedDetailsForMobileSdkHash(order.getMobileSdkHash());
                intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                intent.putExtra(PayuConstants.PAYMENT_PARAMS, paymentParams);
                intent.putExtra(PayuConstants.PAYU_HASHES, payuHashes);
                startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
            }

            @Override
            protected void onException(Exception e) {
                finish();
            }

            @Override
            protected void onFinally() {
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                Intent intent = new Intent(OrderConfirmActivity.this, PaymentSuccessActivity.class);
                intent.putExtra("order", order);
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
            }
            if (getIntent().getBooleanExtra("isDeepLink", false)) {
                Intent intent = new Intent(OrderConfirmActivity.this, ProductDetailsActivity.class);
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
            } else {
                setResult(resultCode, data);
            }
            finish();
        }
    }

    @Override
    public void onBackPressed(){
        new MaterialDialog.Builder(this)
                .title("Do you really want to cancel the order ?")
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .positiveColorRes(R.color.primary)
                .negativeColorRes(R.color.primary)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        dialog.dismiss();
                        if (getIntent().getBooleanExtra("isDeepLink", false)) {
                            Intent intent = new Intent(OrderConfirmActivity.this, ProductDetailsActivity.class);
                            intent.putExtras(getIntent().getExtras());
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra("result", "Transaction canceled due to back pressed!");
                            setResult(RESULT_CANCELED, intent);
                        }
                        finish();
                    }
                })
                .show();
    }
}
