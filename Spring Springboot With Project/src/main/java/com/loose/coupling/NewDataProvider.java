package com.loose.coupling;

public class NewDataProvider implements UserDataProvider{

    @Override
    public String getUserDetails(){
        return "New DB in Action";
    }
}
