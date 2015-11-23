package in.testpress.testpress.ui.paymentGateway;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.payu.india.Model.PaymentDetails;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.PaymentPostParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.ButterKnife;
import butterknife.OnClick;
import in.testpress.testpress.R;
import in.testpress.testpress.ui.TestpressFragmentActivity;
import in.testpress.testpress.util.InternetConnectivityChecker;

public class NetBankingActivity extends TestpressFragmentActivity {

    private String bankcode;
    private ArrayList<PaymentDetails> netBankingList;
    private PaymentParams mPaymentParams;
    private PayuHashes payuHashes;
    private NetBankingAdapter payUNetBankingAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getParcelableArrayList(PayuConstants.NETBANKING) != null) {
            netBankingList = new ArrayList<PaymentDetails>();
            netBankingList = bundle.getParcelableArrayList(PayuConstants.NETBANKING);
            Collections.sort(netBankingList, new Comparator<PaymentDetails>() {
                @Override
                public int compare(PaymentDetails item1, PaymentDetails item2) {
                    return item1.getBankName().compareTo(item2.getBankName());
                }
            });
            payUNetBankingAdapter = new NetBankingAdapter(this, netBankingList);
        } else {
            Toast.makeText(NetBankingActivity.this, "Could not get netbanking list Data from the previous activity", Toast.LENGTH_LONG).show();
        }
        setContentView(R.layout.net_banking_activity);
        ButterKnife.inject(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Spinner spinnerNetbanking = (Spinner) findViewById(R.id.spinner_netbanking);
        spinnerNetbanking.setAdapter(payUNetBankingAdapter);
        spinnerNetbanking.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                bankcode = netBankingList.get(index).getBankCode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        mPaymentParams = bundle.getParcelable(PayuConstants.PAYMENT_PARAMS);
        payuHashes = bundle.getParcelable(PayuConstants.PAYU_HASHES);
        ((TextView) findViewById(R.id.amount)).setText("Amount: ₹ " + mPaymentParams.getAmount());
        ((TextView) findViewById(R.id.transaction_id)).setText("Transaction id: " + mPaymentParams.getTxnId());
    }

    @OnClick(R.id.button_pay_now) void onClick() {
        InternetConnectivityChecker internetConnectivityChecker = new InternetConnectivityChecker(this);
        if (internetConnectivityChecker.isConnected()) {
            PostData postData = new PostData();
            mPaymentParams.setHash(payuHashes.getPaymentHash());
            mPaymentParams.setBankCode(bankcode);
            postData = new PaymentPostParams(mPaymentParams, PayuConstants.NB).getPaymentPostParams();
            if (postData.getCode() == PayuErrors.NO_ERROR) {
                // launch webview
                PayuConfig payuConfig = new PayuConfig();
                payuConfig.setData(postData.getResult());
                Intent intent = new Intent(this, PaymentsActivity.class);
                intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
            } else {
                Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
            }
        } else {
            internetConnectivityChecker.showAlert();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                setResult(resultCode, data);
                finish();
            }
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
                        Intent intent = new Intent();
                        intent.putExtra("result", "Transaction canceled due to back pressed!");
                        setResult(RESULT_CANCELED, intent);
                        finish();
                    }
                })
                .show();
    }
}

class NetBankingAdapter extends BaseAdapter {
    Context mContext;
    ArrayList<PaymentDetails> mNetBankingList;

    public NetBankingAdapter(Context context, ArrayList<PaymentDetails> netBankingList) {
        mContext = context;
        mNetBankingList = netBankingList;
    }

    @Override
    public int getCount() {
        return mNetBankingList.size();
    }

    @Override
    public Object getItem(int i) {
        if(null != mNetBankingList) return mNetBankingList.get(i);
        else return 0;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NetbankingViewHolder netbankingViewHolder = null;
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.netbanking_list_item, null);
            netbankingViewHolder = new NetbankingViewHolder(convertView);
        } else {
            netbankingViewHolder = (NetbankingViewHolder) convertView.getTag();
        }
        convertView.setTag(netbankingViewHolder);
        PaymentDetails paymentDetails = mNetBankingList.get(position);
        netbankingViewHolder.netbankingTextView.setText(paymentDetails.getBankName());
        return convertView;
    }

    class NetbankingViewHolder {
        TextView netbankingTextView;

        NetbankingViewHolder(View view) {
            netbankingTextView = (TextView) view.findViewById(R.id.text_view_netbanking);
        }
    }
}