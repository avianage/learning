package com.avianage.corejava.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface AuditLog {
    String action()  default "UNKNOWN";
    String module()  default "GENERAL";
    boolean enabled() default true;
}
