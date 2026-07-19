package com.avianage.corejava.handson.accessmodifiers;

public class Packages {
    /*
    A package is a namespace — a way to organize related classes and avoid name conflicts.

    Without packages, if two libraries both define a class called Employee, there is no
    way to distinguish them. With packages:

    com.hrms.bean.Employee     // one library's Employee
    com.payroll.model.Employee // another library's Employee

    Both can coexist and be imported explicitly.
     */

    /*
    Package Naming Conventions

    Packages use all lowercase, reverse domain name convention:
    com.ems.bean         → domain: ems.com, sub-package: bean
    com.ems.service      → service classes
    com.ems.util         → utility/helper classes
    com.ems.exception    → custom exceptions
    org.example.project  → for non-commercial or open-source projects

     */

    // Built In Packages
    /*
    Package 	            Key Classes
    java.lang 	            String, Math, System, Object, Integer, Thread — auto-imported
    java.util 	            ArrayList, HashMap, Scanner, Arrays, Collections, Optional
    java.util.stream 	    Stream, Collectors
    java.util.function 	    Function, Predicate, Consumer, Supplier
    java.io 	            File, FileReader, BufferedReader, ObjectInputStream
    java.nio.file 	        Path, Paths, Files
    java.util.concurrent 	ExecutorService, Future, CompletableFuture

    java.lang is the only package that is automatically imported — everything else needs an
    explicit import.
     */

    // Sub Packages
    /*
    Subpackages are simply packages with a longer name that share a prefix. They are not hierarchically nested in terms of access — a class in com.ems.bean does not automatically have access to classes in com.ems.bean.specialist:

    package com.ems.bean;
    // No automatic access to com.ems.bean.specialist — must import explicitly


     */

}
