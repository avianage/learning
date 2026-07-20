package com.avianage.corejava.dtypes;

public class DataTypes {
    static void main(String[] args) {

        /*
        Java Types
        |---- Primitive: 8 built-in, holds actual value
        |---- Reference: points to object on the heap (covered in OOP modules)

        The 8 Primitive Types:
        Types   Size        Range                   Default         Typical Use
        byte	1 byte	-128 to 127	                    0	    Raw binary data, file I/O
        short	2 bytes	-32,768 to 32,767               0       Rarely used directly
        int	    4 bytes	-2,147,483,648 to 2,147,483,647	0	    Standard integer type
        long	8 bytes	-9.2 × 10¹⁸ to 9.2 × 10¹⁸	    0L	    Large numbers, timestamps
        float	4 bytes	~7 decimal digits precision	   0.0f	    Rarely used (use double)
        double	8 bytes	~15 decimal digits precision   0.0d	    Standard decimal type
        char	2 bytes	0 to 65,535 (Unicode)	     '\u0000'	Single character
        boolean	JVM-dependent	true or false	       false	Flags,  conditions

        */
        int employeeId = 123;           // whole number -> int
        long aadharNum = 123456789L;    // exceed int range -> Long
        double salary = 75000.50;       // decimal number -> Double
        boolean isActive = true;        // yes/no flag -> boolean
        char grade = 'A';               // single char -> char (single quotes)
        String name = "Aakash";         // text -> String (Reference type, not primitive)



    }
}
