package com.loose.coupling;

public class LooseCouplingExample {
    public static void main(String[] args){
        UserDataProvider databaseProvider = new UserDatabaseProvider();
        UserManager userManagerWithDb = new UserManager(databaseProvider);
        System.out.println(userManagerWithDb.getUserInfo());

        UserDataProvider webServiceProvide = new WebServiceDataProvider();
        UserManager userManagerWithWS = new UserManager(webServiceProvide);
        System.out.println(userManagerWithWS.getUserInfo());

        UserDataProvider webDatabaseProvide = new NewDataProvider();
        UserManager userManagerWithNewDB = new UserManager(webServiceProvide);
        System.out.println(userManagerWithNewDB.getUserInfo());

    }
}
