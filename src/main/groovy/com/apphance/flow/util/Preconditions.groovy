package com.apphance.flow.util

import org.gradle.api.GradleException

class Preconditions {

    def throwIfConditionTrue(condition, String msg) {
        validate(!condition) {
            def constructor = GradleException.getConstructor(String)
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
