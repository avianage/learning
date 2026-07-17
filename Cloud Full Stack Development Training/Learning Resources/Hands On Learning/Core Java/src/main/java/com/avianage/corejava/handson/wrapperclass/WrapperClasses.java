package com.avianage.corejava.handson.wrapperclass;

public class WrapperClasses {

    /*
    The Problem with Primitives:
    Primitives are fast and memory-efficient.
    But they have one limitation: they are not objects.

    This matters when you need to:
    - Store numbers in a List or any collection (collections only hold objects)
    - Represent the absence of a value — a primitive int cannot be null, but sometimes
      "no salary assigned yet" is a valid state
    - Use utility methods like converting a String to an int, finding min/max, checking
      if a value is within range

    Java's solution: Wrapper Classes — one object type for each primitive that wraps
    the primitive value inside an object.

    Primitive	Wrapper Class	Package
    byte	    Byte	        java.lang
    short	    Short	        java.lang
    int	        Integer	        java.lang
    long	    Long	        java.lang
    float	    Float	        java.lang
    double	    Double	        java.lang
    char	    Character	    java.lang
    boolean	    Boolean	        java.lang

    All wrapper classes are in java.lang — automatically available, no import needed.
     */

    static void main(String[] args) {
        // Creating Wrapper Objects
        Integer empId   = Integer.valueOf(101);
        Double  salary  = Double.valueOf(75000.0);
        Boolean active  = Boolean.valueOf(true);
        Character grade = Character.valueOf('A');

        // Parsing Strings
        // Reading numbers from user input, config files, or APIs
        String input = "82000";
        int    salary1 = Integer.parseInt(input);      // String → int

        String rate   = "3.14";
        double pi     = Double.parseDouble(rate);     // String → double

        String flag   = "true";
        boolean b     = Boolean.parseBoolean(flag);   // String → boolean (case-insensitive)

        String num    = "FF";
        int    hex    = Integer.parseInt(num, 16);    // parse hex string → 255

        // parseInt and parseDouble throw NumberFormatException
        // if the string is not a valid number:

        // int bad = Integer.parseInt("abc");   // throws NumberFormatException at runtime

        // Converting Wrapper to String
        int    salary2 = 75000;
        String s1 = Integer.toString(salary2);    // "75000"
        String s2 = String.valueOf(salary2);      // "75000" — works for any type
        String s3 = "" + salary2;                 // "75000" — concatenation trick (works but less clean)
    }

}