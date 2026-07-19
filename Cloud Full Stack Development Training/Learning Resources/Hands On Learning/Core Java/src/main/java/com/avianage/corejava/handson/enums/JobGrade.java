package com.avianage.corejava.handson.enums;

public enum JobGrade {
    // Enums with abstract methods

    JUNIOR {
        @Override
        public double applyBonus(double salary) { return salary * 0.05; }

        @Override
        public String describe() { return "Junior (0-2 yrs)"; }
    },

    MID {
        @Override
        public double applyBonus(double salary) { return salary * 0.10; }

        @Override
        public String describe() { return "Mid-level (2-5 yrs)"; }
    },

    SENIOR {
        @Override
        public double applyBonus(double salary) { return salary * 0.20; }

        @Override
        public String describe() { return "Senior (5+ yrs)"; }
    };

    public abstract double applyBonus(double salary);
    public abstract String describe();
}