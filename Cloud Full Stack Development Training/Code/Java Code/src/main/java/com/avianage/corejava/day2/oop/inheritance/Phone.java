package com.avianage.corejava.day2.oop.inheritance;

class BasicPhone {

    public void call() {
        System.out.println("calling...");
    }

    public void sms() {
        System.out.println("texting...");
    }
}

class FeaturePhone extends BasicPhone {

    public void music() {
        System.out.println("playing...");
    }

}

class SmartPhone extends FeaturePhone {

    @Override
    public void music() {
        System.out.println("playing dolby...");
    }

    public void camera() {
        System.out.println("clicking...");
    }

}