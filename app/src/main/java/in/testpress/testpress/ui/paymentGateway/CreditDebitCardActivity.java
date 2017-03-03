package in.testpress.testpress.ui.paymentGateway;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.PaymentPostParams;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.testpress.R;
import in.testpress.testpress.ui.TestpressFragmentActivity;
import in.testpress.testpress.ui.TextWatcherAdapter;
import in.testpress.testpress.util.InternetConnectivityChecker;

import static in.testpress.testpress.ui.paymentGateway.PaymentModeActivity.ACTION_BACK_PRESSED;

public class CreditDebitCardActivity extends TestpressFragmentActivity {

    @InjectView(R.id.card_number) EditText cardNumberEditText;
    @InjectView(R.id.name_on_card) EditText cardNameEditText;
    @InjectView(R.id.card_cvv) EditText cardCvvEditText;
    @InjectView(R.id.expiry_month) EditText cardExpiryMonthEditText;
    @InjectView(R.id.expiry_year) EditText cardExpiryYearEditText;
    @InjectView(R.id.fill_all_details) TextView fillAllDetailsText;
    @InjectView(R.id.make_payment) Button payButton;
    final TextWatcher watcher = validationTextWatcher();
    PayuHashes mPayuHashes;
    PaymentParams mPaymentParams;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mPayuHashes = bundle.getParcelable(PayuConstants.PAYU_HASHES);
            mPaymentParams = bundle.getParcelable(PayuConstants.PAYMENT_PARAMS);
        }
        setContentView(R.layout.credit_debit_activity);
        ButterKnife.inject(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((TextView) findViewById(R.id.amount)).setText("Amount: ₹ " + mPaymentParams.getAmount());
        ((TextView) findViewById(R.id.transaction_id)).setText("Transaction id: " + mPaymentParams.getTxnId());
        cardNumberEditText.addTextChangedListener(watcher);
        cardNameEditText.addTextChangedListener(watcher);
        cardCvvEditText.addTextChangedListener(watcher);
        cardExpiryMonthEditText.addTextChangedListener(watcher);
        cardExpiryYearEditText.addTextChangedListener(watcher);
    }


    private TextWatcher validationTextWatcher() {
        return new TextWatcherAdapter() {
            public void afterTextChanged(final Editable EditTextBox) {
                updateUIWithValidation();
            }
        };
    }

    private void updateUIWithValidation() {
        final boolean populated = populated(cardNumberEditText) && populated(cardNameEditText) && populated(cardCvvEditText) && populated(cardExpiryMonthEditText) && populated(cardExpiryYearEditText);
        if(populated) {
            fillAllDetailsText.setVisibility(View.GONE);
            payButton.setEnabled(true);
        } else {
            payButton.setEnabled(false);
            fillAllDetailsText.setVisibility(View.VISIBLE);
        }
    }

    private boolean populated(final EditText editText) {
        return editText.getText().toString().trim().length() > 0;
    }

    @OnClick(R.id.make_payment) void pay() {
        InternetConnectivityChecker internetConnectivityChecker = new InternetConnectivityChecker(CreditDebitCardActivity.this);
        if (internetConnectivityChecker.isConnected()) {
            mPaymentParams.setHash(mPayuHashes.getPaymentHash());
            String cardNumber = String.valueOf(cardNumberEditText.getText());
            String cardName = cardNameEditText.getText().toString();
            String expiryMonth = cardExpiryMonthEditText.getText().toString();
            String expiryYear = "20" + cardExpiryYearEditText.getText().toString();
            String cvv = cardCvvEditText.getText().toString();
            mPaymentParams.setCardNumber(cardNumber);
            mPaymentParams.setCardName(cardName);
            mPaymentParams.setNameOnCard(cardName);
            mPaymentParams.setExpiryMonth(expiryMonth);
            mPaymentParams.setExpiryYear(expiryYear);
            mPaymentParams.setCvv(cvv);
            PostData postData = new PaymentPostParams(mPaymentParams, PayuConstants.CC).getPaymentPostParams();
            if (postData.getCode() == PayuErrors.NO_ERROR) {
                // launch webview
                PayuConfig payuConfig = new PayuConfig();
                payuConfig.setData(postData.getResult());
                Intent intent = new Intent(CreditDebitCardActivity.this, PaymentsActivity.class);
                intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
            } else {
                Toast.makeText(CreditDebitCardActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
            }
        } else {
            internetConnectivityChecker.showAlert();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            setResult(resultCode, data);
            finish();
        }
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent();
        intent.setAction(ACTION_BACK_PRESSED);
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
