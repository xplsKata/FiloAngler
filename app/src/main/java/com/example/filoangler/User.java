package com.example.filoangler;

public class User {
    private String Email;
    private String Password;

    public User(String Email, String Password){
        this.Email = Email;
        this.Password = Password;
    }

    public String GetEmail(){
        return Email;
    }

    public void SetEmail(String Email){
        this.Email = Email;
    }

    public String GetPassword(){
        return Password;
    }

    public void SetPassword(String Password){
        this.Password = Password;
    }
}
