package com.avianage.corejava.dtypes;

public class Operators {
    static void main(String[] args) {
        int a = 17, b = 5;

        System.out.println(a + b);  // 22 addition
        System.out.println(a - b);  // 12 subtraction
        System.out.println(a * b);  // 85 multiplication
        System.out.println(a / b);  // 3 integer division - fractional part dropped
        System.out.println(a % b);  // 2 modulus (remainder)

        // Integer division gotcha
        int hours = 7;
        int days = hours / 8;   // 0, not 0.875 - both operands are int -> int result
        double d = hours / 8;   // still 0 - division happens first, then widening

        // Correct Way to get decimal values;
        double correct = (double) hours / 8;    // 0.875 - cast before dividing
        double also = hours / 8.0;              // 0.875 - 8.0 is double, promotes hours

        // Assignment Operators
        int salary2 = 50000;
        salary2 += 5000;      // salary = salary + 5000 -> 55000
        salary2 -= 2000;      // salary = salary - 2000 -> 53000
        salary2 *= 2;         // salary = salary * 2 -> 106000
        salary2 /= 4;         // salary = salary / 4 -> 26500
        salary2 %= 3;         // salary = salary % 3 -> 1

        // Increment and Decrement
        int count = 0;
        count++;            // post increment: use value first increment later
        ++count;            // pre increment: increment first, use value later
        count--;            // post decrement
        --count;            // pre decrement

        // Example
        int x = 5;
        int y = x++;    // y = 5, x = 6
        int z = ++x;    // z = 7, x = 7

        // Relational Operators
        int salary3 = 75000;
        int salary4 = 82000;

        System.out.println(salary3 == salary4);   // false  equal to
        System.out.println(salary3 != salary4);   // true   not equal to
        System.out.println(salary3 > salary4);   // false  greater than
        System.out.println(salary3 < salary4);   // true   less than
        System.out.println(salary3 >= salary4);   // false  greater than or equal
        System.out.println(salary3 <= salary4);   // true   less than or equal

        // Logical Operators
        boolean isActive1 = true;
        boolean isSenior1 = false;
        double  salary1     = 90000;

        // AND — both must be true
        System.out.println(isActive1 && isSenior1);           // false

        // OR — at least one must be true
        System.out.println(isActive1 || isSenior1);           // true

        // NOT — inverts the boolean
        System.out.println(!isActive1);                      // false

        // Practical use
        boolean getsBonus = isActive1 && salary1 > 80000;
        System.out.println(getsBonus);                      // true

        // Short-circuit evaluation:
        // && stops evaluating as soon as it finds false.
        // || stops as soon as it finds true.

        if (isActive1 && salary1 > 80000) {  }
        // if isActive is false, the salary check is never evaluated

        if (isActive1 || salary1 > 80000) {  }
        // if isActive is true, the salary check is never evaluated

        // Bitwise Operators
        int m = 5;   // binary: 0101
        int n = 3;   // binary: 0011

        System.out.println(m & n);    // 1   AND:  0101 & 0011 = 0001
        System.out.println(m | n);    // 7   OR:   0101 | 0011 = 0111
        System.out.println(m ^ n);    // 6   XOR:  0101 ^ 0011 = 0110
        System.out.println(~m);       // -6  NOT:  bitwise complement
        System.out.println(m << 1);   // 10  left shift:  0101 → 1010 (multiply by 2)
        System.out.println(m >> 1);   // 2   right shift: 0101 → 0010 (divide by 2)
        System.out.println(m >>> 1);  // 2   unsigned right shift (fills with 0, not sign bit)

        // Ternanry Operators
        // syntax: condition ? valueIfTrue : valueIfFalse
        double salary5 = 95000;
        String level  = salary5 > 80000 ? "Senior" : "Junior";
        System.out.println(level);   // Senior

        // equivalent if-else:
        String level1;
        if (salary5 > 80000) {
            level1 = "Senior";
        } else {
            level1 = "Junior";
        }

        // InstanceOf Operator
        Object obj = "Sonu";
        System.out.println(obj instanceof String);    // true
        System.out.println(obj instanceof Integer);   // false

    }
}
