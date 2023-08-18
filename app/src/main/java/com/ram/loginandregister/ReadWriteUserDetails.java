package com.ram.loginandregister;

public class ReadWriteUserDetails {
    public  String  dob, gender, mobile ;

    //constructor
    public ReadWriteUserDetails(){

    }

    public ReadWriteUserDetails( String textDOB, String textGender, String textMobile) {

        this.dob = textDOB;
        this.gender = textGender;
        this.mobile = textMobile;
    }
}
