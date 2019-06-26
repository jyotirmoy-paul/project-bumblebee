package com.android.mr_paul.sarwar.models;

public class DonorData {

    public String name;
    public long number;
    public String uid;

    public DonorData(){

    }

    public DonorData(String name, long number, String uid){
        this.name = name;
        this.number = number;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public long getNumber() {
        return number;
    }

    public String getUid() {
        return uid;
    }
}
