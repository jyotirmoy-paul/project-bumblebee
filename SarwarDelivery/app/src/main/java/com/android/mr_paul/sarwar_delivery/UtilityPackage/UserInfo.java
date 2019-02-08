package com.android.mr_paul.sarwar_delivery.UtilityPackage;

public class UserInfo {

    public String name;
    public String phoneNumber;
    public String aadharCardLink;
    public String isProfileCompleted;
    public String creationDate;
    public String isVerified;

    public UserInfo(){

    }


    public UserInfo(String name, String phoneNumber, String aadharCardLink, String isProfileCompleted, String creationDate, String isVerified){
        this.aadharCardLink = aadharCardLink;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.isProfileCompleted = isProfileCompleted;
        this.creationDate = creationDate;
        this.isVerified = isVerified;
    }

    public String getAadharCardLink(){return aadharCardLink;}

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getIsProfileCompleted() {
        return isProfileCompleted;
    }

}
