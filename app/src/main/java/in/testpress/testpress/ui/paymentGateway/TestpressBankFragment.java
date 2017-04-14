package in.testpress.testpress.ui.paymentGateway;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.view.View;

import com.payu.custombrowser.Bank;

import in.testpress.testpress.R;

public class TestpressBankFragment extends Bank {

    private BroadcastReceiver mReceiver = null;

    @Override
    public void registerBroadcast(BroadcastReceiver broadcastReceiver, IntentFilter filter) {
        mReceiver = broadcastReceiver;
        getActivity().registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void unregisterBroadcast(BroadcastReceiver broadcastReceiver) {
        if(mReceiver != null){
            getActivity().unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    @Override
    public void onHelpUnavailable() {
        getActivity().findViewById(R.id.parent).setVisibility(View.GONE);
        getActivity().findViewById(R.id.trans_overlay).setVisibility(View.GONE);
    }

    @Override
    public void onBankError() {
        getActivity().findViewById(R.id.parent).setVisibility(View.GONE);
        getActivity().findViewById(R.id.trans_overlay).setVisibility(View.GONE);
    }

    @Override
    public void onHelpAvailable() {
        getActivity().findViewById(R.id.parent).setVisibility(View.VISIBLE);
    }
}
