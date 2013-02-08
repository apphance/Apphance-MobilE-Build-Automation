package com.apphance.ameba.util

import org.gradle.api.GradleException

class Preconditions {

    def throwIfCondition(Boolean condition, Class<? extends RuntimeException> c = GradleException.class, String msg) {
        if (condition) {
            def constructor = c.getConstructor(String.class)
            def re = (RuntimeException) constructor.newInstance(msg);
            throw re;
        }
    }
}
