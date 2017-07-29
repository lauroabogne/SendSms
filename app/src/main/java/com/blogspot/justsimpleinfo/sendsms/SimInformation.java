package com.blogspot.justsimpleinfo.sendsms;

/**
 * Created by Lauro-PC on 7/15/2017.
 */

public class SimInformation {

    private String carrierName;
    private String number;
    private int subscriptionId;

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(int subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
}
