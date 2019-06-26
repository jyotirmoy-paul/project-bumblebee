package paul.cipherresfeber.sarwar.models;

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
    public String donorToken;

    // empty constructor for firebase
    public AvailableDonationDetailClass(){

    }

    public AvailableDonationDetailClass(String donorName, String deliveryAgentName, double distanceInKm, String donationCategory, String deliveryAgentUID, String donationKey,
                                        String donorContactNumber, String deliveryAgentNumber, String donationConfirm, String agentToken,
                                        String donationMainPhoto, String donationStatus, String donorToken){

        this.donorName = donorName;
        this.deliveryAgentName = deliveryAgentName;
        this.distanceInKm = distanceInKm;
        this.donationCategory = donationCategory;
        this.deliveryAgentUID = deliveryAgentUID;
        this.donationKey = donationKey;
        this.donorContactNumber = donorContactNumber;
        this.deliveryAgentNumber = deliveryAgentNumber;
        this.donationConfirm = donationConfirm;
        this.agentToken = agentToken;
        this.donationMainPhoto = donationMainPhoto;
        this.donationStatus = donationStatus;
        this.donorToken = donorToken;

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

    public String getDonorToken(){return donorToken;}
}
