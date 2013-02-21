package com.apphance.ameba.util

import org.gradle.api.GradleException

class Preconditions {

    def throwIfCondition(Boolean condition, Class<? extends RuntimeException> c = GradleException, String msg) {
        validate(!condition) {
            def constructor = c.getConstructor(String)
            def re = (RuntimeException) constructor.newInstance(msg);
            throw re;
        }
    }

    def validate(boolean condition, Closure orElse) {
        if (!condition) {
            orElse()
        }
    }
}
