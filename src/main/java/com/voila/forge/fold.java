package com.voila.forge;

import java.lang.annotation.*;

/** make no sense, just make IDE can fold annotations to hide long sentence */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface fold {
}
