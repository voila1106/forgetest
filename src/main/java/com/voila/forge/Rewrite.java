package com.voila.forge;

import java.lang.annotation.*;

/** Re-write whole method */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Rewrite {
}
