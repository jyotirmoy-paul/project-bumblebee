package com.android.mr_paul.sarwar_admin.UtilityPackage;

public class DeliveryAgentProfile {

    public String name;
    public String phoneNumber;
    public String aadharCardLink;
    public String isVerified;
    public String deliveryAgentUID;
    public String creationDate;

    public DeliveryAgentProfile(){

    }

    public DeliveryAgentProfile(String name, String phoneNumber, String aadharCardLink, String isVerified, String deliveryAgentUID, String creationDate){
        this.aadharCardLink = aadharCardLink;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.isVerified = isVerified;
        this.deliveryAgentUID = deliveryAgentUID;
        this.creationDate = creationDate;
    }

    public String getAadharCardLink(){return aadharCardLink;}

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getIsVerified(){return isVerified;}

    public String getDeliveryAgentUID(){return deliveryAgentUID;}

    public String getCreationDate(){return creationDate;}

}
