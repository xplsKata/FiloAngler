package com.example.filoangler;

public class User {
    private String Email;
    private String Username;
    private String Password;
    private String FirstName;
    private String LastName;
    private String Birthdate;
    private String CityAddress;
    private String ProvinceAddress;
    private String Class;

    //Constructor for login
    public User(String Email, String Password){
        this.Email = Email;
        this.Password = Password;
    }

    //Constructor for Register
    public User(String Email, String Password, String Username, String FirstName, String LastName, String Birthdate, String ProvinceAddress, String CityAddress, String Class){
        this.Email = Email;
        this.Password = Password;
        this.Username = Username;
        this.FirstName = FirstName;
        this.LastName = LastName;
        this.Birthdate = Birthdate;
        this.CityAddress = CityAddress;
        this.ProvinceAddress = ProvinceAddress;
        this.Class = Class;
    }

    public String GetEmail(){
        return Email;
    }

    public void SetEmail(String Email){
        this.Email = Email;
    }

    public String GetUsername(){
        return Username;
    }

    public void SetUsername(String Username){
        this.Username = Username;
    }

    public String GetPassword(){
        return Password;
    }

    public void SetPassword(String Password){
        this.Password = Password;
    }

    public String GetFirstName(){
        return FirstName;
    }

    public void SetFirstName(String FirstName){
        this.FirstName = FirstName;
    }

    public String GetLastname(){
        return LastName;
    }

    public void SetLastName(String LastName){
        this.LastName = LastName;
    }

    public String GetBirthdate(){
        return Birthdate;
    }

    public void SetBirthdate(String Birthdate){
        this.Birthdate = Birthdate;
    }

    public String GetCityAddress(){
        return CityAddress;
    }

    public void SetCityAddress(String CityAddress){
        this.CityAddress = CityAddress;
    }

    public String GetProvinceAddress(){
        return ProvinceAddress;
    }

    public void SetProvinceAddress(String ProvinceAddress){
        this.ProvinceAddress = ProvinceAddress;
    }

    public String GetClass(){
        return Class;
    }

    public void SetClass(String Class){
        this.Class = Class;
    }
}
