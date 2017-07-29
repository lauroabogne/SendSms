package com.blogspot.justsimpleinfo.sendsms;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,IncomingSmsReceiver.OnSmsReceivedListener {

    final static String SENT_MESSAGE_TAG ="Message Sent";
    final static String DELIVERED_MESSAGE_TAG = "Message Delivered";

    final static String SEND_SMS = Manifest.permission.SEND_SMS;
    final static String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    final static String RECEIVE_SMS = "android.provider.Telephony.SMS_RECEIVED";

    final static int SEND_SMS_REQUEST_CODE = 1968;

    List<SimInformation> mSimInformations = new ArrayList<>();

    EditText mNumberEditText;
    ListView mMessageListView;
    EditText mMessageEditTet;
    Button mSendMessageButton;


    final BroadcastReceiver mSendMessageReceiver = new SendMessageReceiver();
    final BroadcastReceiver mMessageDeliveredReceiver = new MessageDeliveredReceiver();
    PendingIntent mSendIntent;
    PendingIntent mDeliveryIntent;

    String mMessage;
    String mDestination;


    IncomingSmsReceiver mIncomingSmsReceiver;

    List<Message> messages = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNumberEditText = (EditText) this.findViewById(R.id.cell_number_edittext);
        mMessageEditTet = (EditText) this.findViewById(R.id.message_edittext);
        mMessageListView = (ListView) this.findViewById(R.id.messages_listview);
        mSendMessageButton = (Button) this.findViewById(R.id.send_message_button);
        mSendMessageButton.setEnabled(false);
        mSendMessageButton.setOnClickListener(this);


        CustomAdapter customAdapter = new CustomAdapter(this,messages);
        mMessageListView.setAdapter(customAdapter);


        /**
         * request permission
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkPermission(Manifest.permission.SEND_SMS) && checkPermission(Manifest.permission.READ_PHONE_STATE) && checkPermission(Manifest.permission.RECEIVE_SMS) ) {

                mSendMessageButton.setEnabled(true);

            } else {

                ActivityCompat.requestPermissions(this, new String[]{SEND_SMS,READ_PHONE_STATE,RECEIVE_SMS},SEND_SMS_REQUEST_CODE);
            }

        }else{

            mSendMessageButton.setEnabled(true);
        }




        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1){
            /**
             * get carrier
             */
            mSimInformations = this.getCarrierInformation();

        }else{
            /**
             *
             */
            mSimInformations = this.getCarrierInformation1();

        }



        mSendIntent = PendingIntent.getBroadcast(this,0,new Intent(SENT_MESSAGE_TAG),0);
        mDeliveryIntent = PendingIntent.getBroadcast(this,0,new Intent(DELIVERED_MESSAGE_TAG),0);



        /*IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        *//* filter.setPriority(999); This is optional. *//*
        IncomingSmsReceiver receiver = new IncomingSmsReceiver();
        registerReceiver(receiver, filter);*/

        mIncomingSmsReceiver = new IncomingSmsReceiver();
        mIncomingSmsReceiver.setmOnSmsReceivedListener(this);

    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case SEND_SMS_REQUEST_CODE:
                if(grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    mSendMessageButton.setEnabled(true);
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.registerReceiver(mSendMessageReceiver,new IntentFilter(SENT_MESSAGE_TAG));
        this.registerReceiver(mMessageDeliveredReceiver,new IntentFilter(DELIVERED_MESSAGE_TAG));
        this.registerReceiver(mIncomingSmsReceiver,new IntentFilter(RECEIVE_SMS));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSendMessageReceiver);
        unregisterReceiver(mMessageDeliveredReceiver);
        unregisterReceiver(mIncomingSmsReceiver);

    }

    @Override
    public void onClick(View v) {


        mMessage = mMessageEditTet.getText().toString().trim();
        mDestination  = mNumberEditText.getText().toString();

        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP_MR1){
            /**
             * use default sim
             * dual sim not supported
             */


            sendSms1(mDestination,mMessage);

        }else{
            /**
             * dual sim supported
             *
             */

            new PopupOfCarrierSelection(this).show();
        }



    }

    private void delete(){
        try {


           /* Uri uriSms = Uri.parse("content://sms/inbox");
            Cursor c = this.getContentResolver().query(
                    uriSms,
                    new String[] { "_id", "thread_id", "address", "person",
                            "date", "body" }, "read=0", null, null);*/

            Uri uri = Uri.parse("content://sms/inbox");
            Cursor c= getContentResolver().query(uri, new String[] { "_id", "thread_id", "address", "person",
                    "date", "body" }, null ,null,null);

            Toast.makeText(this,"working "+c.getCount(),Toast.LENGTH_SHORT).show();
            if (c != null && c.moveToFirst()) {
                do {
                    long id = c.getLong(0);
                    long threadId = c.getLong(1);
                    String address = c.getString(2);
                    String body = c.getString(5);
                    String date = c.getString(3);
                    Log.e("log>>>",
                            "0--->" + c.getString(0) + "1---->" + c.getString(1)
                                    + "2---->" + c.getString(2) + "3--->"
                                    + c.getString(3) + "4----->" + c.getString(4)
                                    + "5---->" + c.getString(5));
                    Log.e("log>>>", "date" + c.getString(0));

                    ContentValues values = new ContentValues();
                    values.put("read", true);
                    getContentResolver().update(Uri.parse("content://sms/"),
                            values, "_id=" + id, null);


                        getContentResolver().delete(
                                Uri.parse("content://sms/" + id), "date=?",
                                new String[] { c.getString(4) });
                        Log.e("log>>>", "Delete success.........");

                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e("log>>>", e.toString());
        }
    }

    private boolean checkPermission(String permission) {

        int checkedPermission = ContextCompat.checkSelfPermission(this, permission);

        return checkedPermission == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    protected void sendSms(int subscriptionId,String destinationAddress, String message){


        Message messageObj = new Message();
        messageObj.isMyMessage = true;
        messageObj.mOrgin = "ME";
        messageObj.text = message;
        MainActivity.this.onSmsReceived(messageObj);



        SmsManager sm = SmsManager.getSmsManagerForSubscriptionId(subscriptionId);

        ArrayList<String> messageList = SmsManager.getDefault().divideMessage(message);

        if(messageList.size() > 1 ){

            sm.sendMultipartTextMessage(destinationAddress, null, messageList, null, null);

        }else{

            sm.sendTextMessage(destinationAddress,null,message,mSendIntent,mDeliveryIntent);
        }





    }

    protected  void sendSms1(String destinationAddress, String message){
        SmsManager sm = SmsManager.getDefault();
        ArrayList<String> messageList = SmsManager.getDefault().divideMessage(message);

        if(messageList.size() > 0 ){


            sm.sendMultipartTextMessage(destinationAddress, null, messageList, null, null);

        }else{

            sm.sendTextMessage(destinationAddress,null,message,null,null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private List<SimInformation> getCarrierInformation(){


        List<SimInformation> simInformations = new ArrayList<>();

        SubscriptionManager mSubscriptionManager = SubscriptionManager.from(this);
        List<SubscriptionInfo> mSubInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();

        for (int i = 0; i < mSubInfoList.size(); i++) {


            SimInformation simInformation = new SimInformation();
            simInformation.setCarrierName(mSubInfoList.get(i).getCarrierName().toString());
            simInformation.setSubscriptionId(mSubInfoList.get(i).getSubscriptionId());
            simInformation.setNumber(mSubInfoList.get(i).getNumber());

            simInformations.add(i,simInformation);



        }



        return simInformations;


    }

    private List<SimInformation> getCarrierInformation1(){
        List<SimInformation> simInformations = new ArrayList<>();
        TelephonyManager manager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        SimInformation simInformation = new SimInformation();
        simInformation.setCarrierName(manager.getNetworkOperatorName());
        simInformation.setSubscriptionId(0);
        simInformation.setNumber(manager.getLine1Number());


        simInformations.add(simInformation);
        return simInformations;
    }



    @Override
    public void onSmsReceived(Message message) {

        messages.add(message);
        ArrayAdapter adapter = (ArrayAdapter) mMessageListView.getAdapter();
        adapter.notifyDataSetChanged();
        Toast.makeText(this,"Test",Toast.LENGTH_SHORT).show();

        Log.e("sender",message.mOrgin);
        Log.e("message",message.text);

    }

    /**
     *
     */
    class SendMessageReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {


            String action = intent.getAction();
            switch (this.getResultCode()){
                case Activity.RESULT_OK:




                    Toast.makeText(context,"send"+intent.getAction(),Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context,"RESULT_ERROR_GENERIC_FAILURE",Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context,"RESULT_ERROR_NO_SERVICE",Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context,"RESULT_ERROR_NULL_PDU",Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context,"RESULT_ERROR_RADIO_OFF",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    /**
     *
     */
    class MessageDeliveredReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            switch (this.getResultCode()){
                case Activity.RESULT_OK:

                    Toast.makeText(context,"Message Delivered",Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(context,"Message NOT  Delivered",Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    }

    /**
     *
     */
    class CustomAdapter extends ArrayAdapter {

         CustomAdapter(Context context, List objects) {
            super(context, R.layout.message_layout, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout linearLayout = (LinearLayout) convertView;
            Message message = (Message) this.getItem(position);
            if(linearLayout == null){

                linearLayout = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.message_layout,parent,false);
            }

            TextView textView = (TextView) linearLayout.findViewById(R.id.message_textview);
            textView.setText(message.text);
            TextView icon = (TextView) linearLayout.findViewById(R.id.message_icon);

            boolean isMyMessage = message.isMyMessage;

            if(isMyMessage){


                icon.setText("ME");
                icon.setBackgroundColor(Color.YELLOW);
            }else{
                icon.setText("OTH");
                icon.setBackgroundColor(Color.GREEN);
            }

            return linearLayout;
        }
    }


}
