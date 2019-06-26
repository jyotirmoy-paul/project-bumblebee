package paul.cipherresfeber.sarwar.models;

public class UserInfo {

    public String name;
    public String email;
    public String phoneNumber;
    public String address;

    public String isProfileCompleted;

    public UserInfo(){

    }

    public UserInfo(String name, String email, String phoneNumber, String address, String isProfileCompleted){
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.isProfileCompleted = isProfileCompleted;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

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
