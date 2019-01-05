package in.testpress.testpress.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.testpress.testpress.authenticator.CodeVerificationActivity.Timer;
import in.testpress.testpress.util.Strings;

public class SmsReceivingEvent extends BroadcastReceiver {
    public String code;
    private Timer timer;

    public SmsReceivingEvent(Timer timer) {
        this.timer = timer;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            try {
                Bundle extras = intent.getExtras();
                Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

                switch (status.getStatusCode()) {
                    case CommonStatusCodes.SUCCESS:
                        // Get SMS message contents
                        String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                        // Extract one-time code from the message and complete verification
                        Pattern pattern = Pattern.compile("\\d{7}");
                        Matcher m = pattern.matcher(message);
                        if (m.find()) {
                            code = m.group(0);
                        }
                        timer.cancel();
                        timer.onFinish();

                    case CommonStatusCodes.TIMEOUT:
                        // Waiting for SMS timed out (5 minutes)
                        // Handle the error ...
                        break;
                }
            } catch (Exception e) {
                timer.cancel();
                timer.onFinish();
            }

        }
    }
}

//    // Retrieves a map of extended data from the intent.
//    final Bundle bundle = intent.getExtras();
//        try {
//        if (bundle != null) {
//            final Object[] pdusObj = (Object[]) bundle.get("pdus");
//            SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[0]);
//            String senderNum = currentMessage.getDisplayOriginatingAddress();
//            if (senderNum.matches(".*TSTPRS")) { //check whether TSTPRS present in senderAddress
//                String smsContent = currentMessage.getDisplayMessageBody();
//                //get the code from smsContent
//                code = smsContent.replaceAll(".*(?=.*)(?<=Your authorization code is )([^\n]*)(?=.).*", "$1");
//                timer.cancel();
//                timer.onFinish();
//            }
//        } // bundle is null
//    } catch (Exception e) {
//        timer.cancel();
//        timer.onFinish();
//    }


