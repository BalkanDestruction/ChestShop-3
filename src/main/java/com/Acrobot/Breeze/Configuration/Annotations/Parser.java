package com.Acrobot.Breeze.Configuration.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for a parser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parser {
    /**
     * This option's comment
     *
     * @return Comment
     */
    String value();
}
