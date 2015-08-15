package in.testpress.testpress.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import in.testpress.testpress.authenticator.CodeVerificationActivity.Timer;

public class SmsReceivingEvent extends BroadcastReceiver {
    public String code;
    private Timer timer;

    public SmsReceivingEvent(Timer timer) {
                this.timer = timer;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[0]);
                String senderNum = currentMessage.getDisplayOriginatingAddress();
                if (senderNum.matches(".*TSTPRS")) { //check whether TSTPRS present in senderAddress
                    String smsContent = currentMessage.getDisplayMessageBody();
                    //get the code from smsContent
                    code = smsContent.replaceAll(".*(?<=Thank you for registering at Testpress.in. Your authorization code is )([^\n]*)(?=.).*", "$1");
                    timer.cancel();
                    timer.onFinish();
                }
            } // bundle is null
        } catch (Exception e) {
            timer.cancel();
            timer.onFinish();
        }
    }
}

