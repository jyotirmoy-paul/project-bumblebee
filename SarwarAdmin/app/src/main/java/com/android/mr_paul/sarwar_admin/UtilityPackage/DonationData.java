package com.android.mr_paul.sarwar_admin.UtilityPackage;


public class DonationData {

    public String donorName;
    public String donationCategory;
    public String donationKey;
    public String donorContactNumber;
    public String donationItemDescription;
    public String donationWorth;
    public String donationOtherDetails;
    public String donationMainPhotoUrl;
    public String donationUserAddress;
    public String dateTime;
    public String userRegTokenKey;
    public String status;

    public DonationData(){

    }


    public String getDonationCategory() {
        return donationCategory;
    }


    public String getDonationItemDescription() {
        return donationItemDescription;
    }

    public String getDonationKey() {
        return donationKey;
    }

    public String getDonationMainPhotoUrl() {
        return donationMainPhotoUrl;
    }

    public String getDonationOtherDetails() {
        return donationOtherDetails;
    }

    public String getDonorName() {
        return donorName;
    }

    public String getDonationWorth() {
        return donationWorth;
    }

    public String getDonorContactNumber() {
        return donorContactNumber;
    }

    public String getDonationUserAddress() {
        return donationUserAddress;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getUserRegTokenKey() {
        return userRegTokenKey;
    }

    public String getStatus() {
        return status;
    }
}
