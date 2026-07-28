package com.learning.loose.coupling;

public class UserDatabaseProvider implements UserDataProvider{
    @Override
    public String getUserDetails(){
        return  "User Details from DB";
    }
}
