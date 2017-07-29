package com.blogspot.justsimpleinfo.sendsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

/**
 * Created by Lauro-PC on 7/19/2017.
 */

public class IncomingSmsReceiver extends BroadcastReceiver {

    OnSmsReceivedListener mOnSmsReceivedListener = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();

        if(bundle != null){

            Object[] pdus = (Object[]) bundle.get("pdus");



            for(int x = 0 ; x < pdus.length; x++){

                SmsMessage currentSMS;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    String format = bundle.getString("format");
                    currentSMS = SmsMessage.createFromPdu((byte[]) pdus[x], format);

                } else {

                    currentSMS = SmsMessage.createFromPdu((byte[]) pdus[x]);
                }

                Message message = new Message();
                message.isMyMessage = false;
                message.mOrgin = currentSMS.getOriginatingAddress();
                message.text = currentSMS.getDisplayMessageBody();

                if(mOnSmsReceivedListener != null){

                    mOnSmsReceivedListener.onSmsReceived(message);
                }
            }
        }


    }

    public void setmOnSmsReceivedListener(Context context){

        mOnSmsReceivedListener = (OnSmsReceivedListener) context;

    }

    public interface OnSmsReceivedListener {
        public  void onSmsReceived(Message message);
    }
}
