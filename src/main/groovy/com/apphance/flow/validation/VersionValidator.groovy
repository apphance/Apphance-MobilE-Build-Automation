package com.apphance.flow.validation

import org.gradle.api.GradleException

import java.util.regex.Pattern

import static java.util.ResourceBundle.getBundle
import static java.util.regex.Pattern.compile
import static org.apache.commons.lang.StringUtils.isNotEmpty

class VersionValidator {

    public final static Pattern WHITESPACE_PATTERN = compile('\\s+')

    protected bundle = getBundle('validation')

    void validateVersionString(String versionString) {
        if (!hasNoWhiteSpace(versionString))
            throw new GradleException(bundle.getString('exception.version.string'))
    }

    void validateVersionCode(String versionCode) {
        if (!isNumber(versionCode))
            throw new GradleException(bundle.getString('exception.version.code'))
    }

    boolean isNumber(String number) {
        isNotEmpty(number) && number.matches('[0-9]+')
    }

    boolean hasNoWhiteSpace(String s) {
        isNotEmpty(s) && !WHITESPACE_PATTERN.matcher(s).find()
    }
}
