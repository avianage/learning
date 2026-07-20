package com.avianage.corejava.wrapperclass;

import java.util.ArrayList;
import java.util.List;

public class AutoBoxing {
    // Autoboxing and Unboxing
    // Manually creating wrapper objects gets tedious.
    // Java 5 introduced autoboxing and unboxing to handle this automatically.
    static void main(String[] args) {
        // Autoboxing (Automatic):
        int id1 = 123;
        Integer wrappedId = id1;     // autoboxed - compiler inserts Integer.valueOf(id);

        double salary3 = 75000.0;
        Double wrappedSalary = salary3; // autoboxed;

        List<Integer> ids = new ArrayList<>();
        ids.add(101);   // autoboxed
        ids.add(102);   // autoboxed

        // Unboxing (Automatic)
        Integer wrappedSalary1 = Integer.valueOf(82000);
        int salary = wrappedSalary1; // unboxed - compiler inserts wrappedSalary.intValue()

        Integer a = 100;
        Integer b = 200;
        int sum  = a + b;   // unboxing happens during arithmatic;

        // Example
        List<Double> salaries = new ArrayList<>();
        salaries.add(75000.0);    // double → Double (autoboxing)
        salaries.add(82000.0);
        salaries.add(55000.0);

        double total = 0;
        for (double s : salaries) {    // Double → double (unboxing) in each iteration
            total += s;
        }
        System.out.println("Total: " + total);   // 212000.0

        // Null problem with Unboxing
        // Wrapper Objects can be Null
        // But unboxing a null wrapper throws NullPointerException

        Integer salary1 = null;  // valid - wrapper objects can be null
        int s = salary1;         // NullPointerException at runtime - cannot unbox null

        // Better Practise

        // Integer bonus = getBonus(); // might return Null
        // if (bonus != null){
        //    int b = bonus;
        // }

        // Integer Cache - A Cache with ==
        Integer m = 100;
        Integer n = 100;
        System.out.println(m == n);         // true - same cached object

        Integer x = 200;
        Integer y = 200;
        System.out.println(x == y);         // false - different objects, cache range exceeded
        System.out.println(x.equals(y));    // true - always use equals() for wrapper comparison

    }
}
