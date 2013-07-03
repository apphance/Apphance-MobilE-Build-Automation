package com.apphance.flow.util

import org.gradle.api.GradleException

class Preconditions {

    def throwIfConditionTrue(condition, String msg, String output = '') {
        validate(!condition) {
            def constructor = GradleException.getConstructor(String)
            def re = (RuntimeException) constructor.newInstance(msg);
            re.metaClass.output = output
            throw re;
        }
    }

    def validate(boolean condition, Closure orElse) {
        if (!condition) {
            orElse()
        }
    }
}
