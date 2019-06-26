package paul.cipherresfeber.sarwar.models;

import java.io.Serializable;

public class DonationData implements Serializable {

    public String donorName;
    public String donationCategory;
    public String donationKey;
    public String donorContactNumber;
    public String donationItemDescription;
    public String donationWorth;
    public String donationOtherDetails;
    public String donationMainPhotoUrl;
    public LatLong latLong;
    public String donationUserAddress;
    public String dateTime;
    public String userRegTokenKey;
    public String status;
    public String donorUID;

    public DonationData(){

    }

    public DonationData(String donorName, String donationCategory, String donationKey, String donorContactNumber, String donationItemDescription, String donationWorth,
                        String donationOtherDetails, String donationMainPhotoUrl, LatLong latLong, String donationUserAddress, String dateTime, String userRegTokenKey,
                        String status, String donorUID){
        this.donorName = donorName;
        this.donationCategory = donationCategory;
        this.donationKey = donationKey;
        this.donorContactNumber = donorContactNumber;
        this.donationItemDescription = donationItemDescription;
        this.donationWorth = donationWorth;
        this.donationOtherDetails = donationOtherDetails;
        this.donationMainPhotoUrl = donationMainPhotoUrl;
        this.latLong = latLong;
        this.donationUserAddress = donationUserAddress;
        this.dateTime = dateTime;
        this.userRegTokenKey = userRegTokenKey;
        this.status = status;
        this.donorUID = donorUID;
    }

    public String getDonationCategory() {
        return donationCategory;
    }

    public LatLong getLatLong() {
        return latLong;
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


