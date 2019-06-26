package paul.cipherresfeber.sarwaradmin.models;

public class DeliveryAgentUserInfo {

    public String name;
    public String phoneNumber;
    public String aadharCardLink;
    public String isProfileCompleted;
    public String creationDate;

    public DeliveryAgentUserInfo(){

    }

    public DeliveryAgentUserInfo(String name, String phoneNumber, String aadharCardLink, String isProfileCompleted, String creationDate){
        this.aadharCardLink = aadharCardLink;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.isProfileCompleted = isProfileCompleted;
        this.creationDate = creationDate;
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

    public String getCreationDate(){return creationDate;}

}
