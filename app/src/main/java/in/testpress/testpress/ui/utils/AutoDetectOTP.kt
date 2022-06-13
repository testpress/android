package `in`.testpress.testpress.ui.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class AutoDetectOTP(val context: Context) {
    private var smsCallback: SmsCallback? = null
    private var broadcastReceiver: BroadcastReceiver? = null
    private val appCompatActivity: AppCompatActivity = context as AppCompatActivity
    private var intentFilter: IntentFilter? = null

    fun startSmsRetriever(smsCallback: SmsCallback) {
        registerReceiver()
        this.smsCallback = smsCallback
        val client = SmsRetriever.getClient(context)

        val task = client.startSmsRetriever()
        task.addOnSuccessListener { aVoid ->
            if(aVoid != null){
                smsCallback.connectionSuccess()
            }
        }

        task.addOnFailureListener { smsCallback.connectionFailed() }
    }

    private fun registerReceiver() {
        intentFilter = IntentFilter()
        intentFilter!!.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                    val extras = intent.extras
                    val status = extras!!.get(SmsRetriever.EXTRA_STATUS) as Status?
                    when (status?.statusCode) {
                        CommonStatusCodes.SUCCESS -> {
                            val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String?
                            smsCallback?.smsCallback(message.toString())
                            stopSmsReceiver()
                        }
                        CommonStatusCodes.TIMEOUT -> smsCallback?.connectionFailed()
                    }
                }
            }
        }
        appCompatActivity.application.registerReceiver(broadcastReceiver, intentFilter)
    }

    fun stopSmsReceiver() {
        try {
            appCompatActivity.applicationContext.unregisterReceiver(broadcastReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    interface SmsCallback {
        fun connectionFailed()
        fun connectionSuccess()
        fun smsCallback(sms: String)
    }
}