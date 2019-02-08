package com.android.mr_paul.sarwar_admin.UtilityPackage;

public class AvailableDonationDetailClass {

    public String donorName;
    public String donorContactNumber;

    public String deliveryAgentName;
    public String deliveryAgentNumber;

    public String donationConfirm;
    public double distanceInKm;
    public String donationCategory;
    public String deliveryAgentUID;
    public String donationKey;

    public String agentToken;
    public String donationMainPhoto;
    public String donationStatus;

    public String donorUID;

    // empty constructor for firebase
    public AvailableDonationDetailClass(){

    }

    public String getDonationCategory() {
        return donationCategory;
    }

    public String getDonorContactNumber() {
        return donorContactNumber;
    }

    public String getDonorName() {
        return donorName;
    }

    public String getDonationKey() {
        return donationKey;
    }

    public double getDistanceInKm() {
        return distanceInKm;
    }

    public String getDeliveryAgentName() {
        return deliveryAgentName;
    }

    public String getDonationConfirm() {
        return donationConfirm;
    }

    public String getDeliveryAgentNumber() {
        return deliveryAgentNumber;
    }

    public String getDeliveryAgentUID() {
        return deliveryAgentUID;
    }

    public String getAgentToken() {
        return agentToken;
    }

    public String getDonationMainPhoto() {
        return donationMainPhoto;
    }

    public String getDonationStatus() {
        return donationStatus;
    }

    public String getDonorUID(){
        return donorUID;
    }
}
