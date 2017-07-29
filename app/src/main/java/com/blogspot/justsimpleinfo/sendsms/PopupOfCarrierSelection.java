package com.blogspot.justsimpleinfo.sendsms;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Lauro-PC on 7/15/2017.
 */

public class PopupOfCarrierSelection extends AlertDialog.Builder implements AdapterView.OnItemClickListener {


    ListView mCarrierListView;
    MainActivity mMainActivity;
    AlertDialog mAlertDialog;

    public PopupOfCarrierSelection(Context context) {
        super(context);


        mMainActivity = (MainActivity) context;
        this.setTitle("Select Carrier");

        mCarrierListView = new ListView(context);
        mCarrierListView.setAdapter(new CustomAdapter(context,mMainActivity.mSimInformations));
        mCarrierListView.setOnItemClickListener(this);
        this.setView(mCarrierListView);
    }

    @Override
    public AlertDialog show() {

        mAlertDialog = super.show();
        return mAlertDialog;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        SimInformation simInformation = (SimInformation) view.getTag();


        mMainActivity.sendSms(simInformation.getSubscriptionId(),mMainActivity.mDestination,mMainActivity.mMessage);
        mAlertDialog.dismiss();




    }

    /**
     *
     */
    class CustomAdapter extends ArrayAdapter {

        CustomAdapter(Context context,  List objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView textView = (TextView) convertView;
            SimInformation simInformation = (SimInformation) this.getItem(position);

            if(textView == null){

                textView = (TextView) LayoutInflater.from(this.getContext()).inflate(android.R.layout.simple_list_item_1,parent, false);
            }
            textView.setText(simInformation.getCarrierName());
            textView.setTag(simInformation);


            return  textView;
        }
    }

}
