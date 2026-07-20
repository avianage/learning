package com.ioc.coupling;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class IOCExample {
    public static void main(String[] args){

        ApplicationContext context = new ClassPathXmlApplicationContext("applicationIocLooseCoupling.xml");

        // UserDataProvider databaseProvider = new UserDatabaseProvider();
        // UserManager userManagerWithDb = new UserManager(databaseProvider);
        UserManager userManagerWithDb = (UserManager) context.getBean("userManagerWithUserDataProvider");
        System.out.println(userManagerWithDb.getUserInfo());

        // UserDataProvider webServiceProvide = new WebServiceDataProvider();
        // UserManager userManagerWithWS = new UserManager(webServiceProvide);
        UserManager userManagerWithNewDB = (UserManager) context.getBean("userManagerWithWebServiceDataProvider");
        System.out.println(userManagerWithNewDB.getUserInfo());

        // UserDataProvider webDatabaseProvide = new NewDataProvider();
        // UserManager userManagerWithNewDB = new UserManager(webDatabaseProvide);
        UserManager userManagerWithWS = (UserManager) context.getBean("userManagerWithNewDataProvider");
        System.out.println(userManagerWithWS.getUserInfo());

    }
}
