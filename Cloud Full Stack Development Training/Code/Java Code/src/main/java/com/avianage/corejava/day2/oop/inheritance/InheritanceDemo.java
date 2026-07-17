package com.avianage.corejava.day2.oop.inheritance;

public class InheritanceDemo {

    public static void main(String[] args) {

        BasicPhone phone1 = new BasicPhone();
        phone1.call();
        phone1.sms();

        FeaturePhone phone2 = new FeaturePhone();
        phone2.call();
        phone2.sms();
        phone2.music();

        SmartPhone phone3 = new SmartPhone();
        phone3.call();
        phone3.sms();
        phone3.music();
        phone3.camera();

        BasicPhone phone4 = new SmartPhone();
        phone4.call();
        phone4.sms();
//		phone4.music(); // CE
//		phone4.camera(); // CE
//		advantages ?


    }

}
