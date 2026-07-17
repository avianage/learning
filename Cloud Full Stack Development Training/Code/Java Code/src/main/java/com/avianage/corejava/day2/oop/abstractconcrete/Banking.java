package com.avianage.corejava.day2.oop.abstractconcrete;

public class Banking {
}

interface StateGovt {

}

interface GovOfIndia {
    void checkNationality();
}

abstract class Rbi {
    abstract void doKyc();

    public abstract void payInterest();
}

class HdfcBank extends Rbi implements GovtOfIndia, StateGovt {

    @Override
    public void doKyc() {
        System.out.println("Aadhaar KYC");
    }

    @Override
    public void payInterest() {
        System.out.println("Paying 4% interest");
    }

    @Override
    public void checkNationality() {
        System.out.println("Is Indian?");
    }
}

class IciciBank extends Rbi implements GovtOfIndia {

    @Override
    public void doKyc() {
        System.out.println("PAN Card KYC");
    }

    @Override
    public void payInterest() {
        System.out.println("Paying 5% interest");
    }

    @Override
    public void checkNationality() {
        System.out.println("Is Foreign?");
    }
}

