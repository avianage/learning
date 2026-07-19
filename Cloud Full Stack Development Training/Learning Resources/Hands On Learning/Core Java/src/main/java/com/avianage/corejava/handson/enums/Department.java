package com.avianage.corejava.handson.enums;

public enum Department {
    // Enum with fields and methods

    ENGINEERING ("Engineering",  "Pune",    500000.0),
    HR          ("Human Res.",   "Mumbai",  200000.0),
    FINANCE     ("Finance",      "Delhi",   300000.0),
    OPERATIONS  ("Operations",   "Pune",    250000.0);

    // ── Fields ────────────────────────────────────────────────────────
    private final String displayName;
    private final String location;
    private final double budget;

    // ── Constructor ───────────────────────────────────────────────────
    Department(String displayName, String location, double budget) {
        this.displayName = displayName;
        this.location    = location;
        this.budget      = budget;
    }

    // ── Methods ───────────────────────────────────────────────────────
    public String getDisplayName() { return displayName; }
    public String getLocation()    { return location; }
    public double getBudget()      { return budget; }

    public boolean isInCity(String city) {
        return this.location.equalsIgnoreCase(city);
    }

    public String budgetCategory() {
        if (budget >= 400000) return "High";
        if (budget >= 250000) return "Medium";
        return "Low";
    }
}