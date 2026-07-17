package com.avianage.corejava.handson.dtypes;

public class Literals {
    static void main(String[] args) {
        // Integer Literals:
        int a = 42;         // decimal (base 10)
        int b = 0b101010;   // binary (base 2)  -> prefix 0b
        int c = 052;        // octal (base 8)   -> prefix 0
        int d = 0x2A;       // hexadecimal (base 16) -> prefix 0x

        long bigNum = 9_000_000_000L;

        // Floating Point Literals
        double d1 = 3.14;   // default is double
        double d2 = 3.14d;  // explicit double suffix
        float f1 = 3.14f;   // Must have f suffix - without it, 3.14 is a double and wont fit float;
        double sci = 1.5e10;// scientific notation

        // Character Literals
        char c1 = 'A';     // char literal - single quotes
        char c2 = 65;      // same as 'A' stores Unicode
        char c3 = '\n';    // escape seq - newline
        char c4 = '\t';    // escape seq - tab
        char c5 = '\'';    // escape seq - single quote;
        char c6 = '\\';    // escape seq - backslash;

        // String Literals
        String s = "Sonu";                      // double quotes
        String empty = "";                      // empty string - not null
        String multiword = " Senior Engineer";  // spaces fine inside quotes

        // Boolean Literals
        boolean isActive = true;
        boolean isDeleted = false;



    }
}
