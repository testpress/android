package in.testpress.testpress.ui.paymentGateway;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.payu.india.Interfaces.PaymentRelatedDetailsListener;
import com.payu.india.Model.MerchantWebService;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.MerchantWebServicePostParams;
import com.payu.india.Tasks.GetPaymentRelatedDetailsTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.testpress.R;
import in.testpress.testpress.ui.TestpressFragmentActivity;
import in.testpress.testpress.util.InternetConnectivityChecker;

public class PaymentModeActivity extends TestpressFragmentActivity implements PaymentRelatedDetailsListener{

    @InjectView(R.id.paymentMethodLayout) LinearLayout paymentMethodLayout;
    @InjectView(R.id.pb_loading) ProgressBar progressBar;
    @InjectView(R.id.amount) TextView amount;
    PayuConfig payuConfig;
    PayuResponse mPayuResponse;
    PaymentParams mPaymentParams;
    PayuHashes mPayUHashes;
    boolean cancelled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_modes);
        ButterKnife.inject(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle bundle = getIntent().getExtras();
        payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
        payuConfig = null != payuConfig ? payuConfig : new PayuConfig();
        mPaymentParams = bundle.getParcelable(PayuConstants.PAYMENT_PARAMS);
        mPayUHashes = bundle.getParcelable(PayuConstants.PAYU_HASHES);
        MerchantWebService merchantWebService = new MerchantWebService();
        merchantWebService.setKey(mPaymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK);
        merchantWebService.setVar1("default");
        merchantWebService.setHash(mPayUHashes.getPaymentRelatedDetailsForMobileSdkHash());
        // fetching for the first time.
        if(null == savedInstanceState){ // dont fetch the data if its been called from payment activity.
            PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();
            if(postData.getCode() == PayuErrors.NO_ERROR){
                // ok we got the post params, let make an api call to payu to fetch the payment related details
                payuConfig.setData(postData.getResult());
                paymentMethodLayout.setVisibility(View.GONE);
                progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_IN);
                GetPaymentRelatedDetailsTask paymentRelatedDetailsForMobileSdkTask = new GetPaymentRelatedDetailsTask(this);
                paymentRelatedDetailsForMobileSdkTask.execute(payuConfig);
            } else {
                Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                setResult(resultCode, data);
                finish();
            } else {
                cancelled = true;
            }
        }
    }

    @Override
    public void onPaymentRelatedDetailsResponse(PayuResponse payuResponse) {
        mPayuResponse = payuResponse;
        amount.setText("₹ " + mPaymentParams.getAmount());
        paymentMethodLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    public void onClick(View v) {
        InternetConnectivityChecker internetConnectivityChecker = new InternetConnectivityChecker(this);
        if (internetConnectivityChecker.isConnected()) {
            Intent intent;
            switch (v.getId()) {
                case R.id.creditCard:
                    intent = new Intent(PaymentModeActivity.this, CreditDebitCardActivity.class);
                    break;
                case R.id.debitCard:
                    intent = new Intent(PaymentModeActivity.this, CreditDebitCardActivity.class);
                    break;
                case R.id.netBanking:
                    intent = new Intent(PaymentModeActivity.this, NetBankingActivity.class);
                    intent.putParcelableArrayListExtra(PayuConstants.NETBANKING, mPayuResponse.getNetBanks());
                    break;
                default:
                    intent = new Intent(PaymentModeActivity.this, PaymentModeActivity.class);
                    break;
            }
            intent.putExtra(PayuConstants.PAYU_HASHES, mPayUHashes);
            intent.putExtra(PayuConstants.PAYMENT_PARAMS, mPaymentParams);
            startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
        } else {
            internetConnectivityChecker.showAlert();
        }
    }

    @Override
    public void onBackPressed(){
        if(cancelled) { //if already cancelled in previous activity simply go back
            finishActivity();
        } else {
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
                            finishActivity();
                        }
                    })
                    .show();
        }
    }

    void finishActivity() {
        Intent intent = new Intent();
        intent.putExtra("result", "Transaction canceled due to back pressed!");
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}

