package com.apphance.ameba.configuration

import java.lang.annotation.Retention
import java.lang.annotation.Target

import static java.lang.annotation.ElementType.FIELD
import static java.lang.annotation.RetentionPolicy.RUNTIME

@Retention(RUNTIME)
@Target(FIELD)
@interface AmebaProp {
    String name()

    String message()

    Class defaultValue()

    //Class validator()

    //Class possibleValues()
}