package com.android.mr_paul.sarwar_admin.UtilityPackage;

public class UserInfo {

    public String firebaseToken;
    public String name;
    public String phoneNumber;
    public String uid;

    public UserInfo(){

    }

    public UserInfo(String name, String phoneNumber, String firebaseToken, String uid){
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.firebaseToken = firebaseToken;
        this.uid = uid;
    }


}
