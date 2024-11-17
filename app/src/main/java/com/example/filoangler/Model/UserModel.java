package com.example.filoangler.Model;

public class UserModel {
    private String UserID;
    private String Email;
    private String Username;
    private String Password;
    private String FirstName;
    private String LastName;
    private String Bio;
    private String Birthdate;
    private String CityAddress;
    private String ProvinceAddress;
    private String AnglerStatus;
    private String ProfileIconURL;

    public UserModel(){

    }

    //Constructor for login
    public UserModel(String Email, String Password){
        this.Email = Email;
        this.Password = Password;
    }

    //Constructor for RegisterP2
    public UserModel(String Email, String Password, String Username,
                     String FirstName, String LastName, String Birthdate,
                     String ProvinceAddress, String CityAddress, String AnglerStatus){
        this.Email = Email;
        this.Password = Password;
        this.Username = Username;
        this.FirstName = FirstName;
        this.LastName = LastName;
        this.Birthdate = Birthdate;
        this.CityAddress = CityAddress;
        this.ProvinceAddress = ProvinceAddress;
        this.AnglerStatus = AnglerStatus;
    }

    //Constructor for UserModel, RegisterP3
    public UserModel(String Email, String Password, String Username,
                     String FirstName, String LastName, String Birthdate,
                     String ProvinceAddress, String CityAddress, String AnglerStatus, String Bio, String ProfileIconURL){
        this.Email = Email;
        this.Password = Password;
        this.Username = Username;
        this.FirstName = FirstName;
        this.LastName = LastName;
        this.Bio = Bio;
        this.Birthdate = Birthdate;
        this.CityAddress = CityAddress;
        this.ProvinceAddress = ProvinceAddress;
        this.AnglerStatus = AnglerStatus;
        this.ProfileIconURL = ProfileIconURL;
    }

    public String getUserID(){
        return UserID;
    }

    public void setUserID(String UserID){
        this.UserID = UserID;
    }

    public String getEmail(){
        return Email;
    }

    public void setEmail(String Email){
        this.Email = Email;
    }

    public String getUsername(){
        return Username;
    }

    public void setUsername(String Username){
        this.Username = Username;
    }

    public String getPassword(){
        return Password;
    }

    public void setPassword(String Password){
        this.Password = Password;
    }

    public String getFirstName(){
        return FirstName;
    }

    public void setFirstName(String FirstName){
        this.FirstName = FirstName;
    }

    public String getLastName(){
        return LastName;
    }

    public void setLastName(String LastName){
        this.LastName = LastName;
    }

    public String getBio(){
        return Bio;
    }

    public void setBio(String Bio){
        this.Bio = Bio;
    }

    public String getBirthdate(){
        return Birthdate;
    }

    public void setBirthdate(String Birthdate){
        this.Birthdate = Birthdate;
    }

    public String getCityAddress(){
        return CityAddress;
    }

    public void setCityAddress(String CityAddress){
        this.CityAddress = CityAddress;
    }

    public String getProvinceAddress(){
        return ProvinceAddress;
    }

    public void setProvinceAddress(String ProvinceAddress){
        this.ProvinceAddress = ProvinceAddress;
    }

    public String getAnglerStatus(){
        return AnglerStatus;
    }

    public void setAnglerStatus(String AnglerStatus){
        this.AnglerStatus = AnglerStatus;
    }

    public String getProfileIconURL(){
        return ProfileIconURL;
    }

    public void setProfileIconURL(String ProfileIconURL){
        this.ProfileIconURL = ProfileIconURL;
    }
}
