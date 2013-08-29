package com.apphance.flow.validation

import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.configuration.properties.StringProperty
import org.gradle.api.GradleException

import java.util.regex.Pattern

import static java.text.MessageFormat.format
import static java.util.ResourceBundle.getBundle
import static org.apache.commons.lang.StringUtils.isBlank

class ReleaseValidator {

    public static final MAIL_PATTERN_WITH_NAME = /.* *<{0,1}[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[A-Za-z]{2,4}>{0,1}/
    public static final MAIL_PATTERN = /[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[A-Za-z]{2,4}/
    public static final WHITESPACE = Pattern.compile('\\s+')

    private final bundle = getBundle('validation')

    void validateMailServer(String mailServer) {
        if (isBlank(mailServer) || WHITESPACE.matcher(mailServer).find())
            throw new GradleException(bundle.getString('exception.release.mail.server'))
    }

    void validateMailPort(String mailPort) {
        if (isBlank(mailPort) || !mailPort.matches('[0-9]+'))
            throw new GradleException(bundle.getString('exception.release.mail.port'))
    }

    void validateMail(StringProperty mail) {
        if (!mail.validator(mail.value))
            throw new GradleException(format(bundle.getString('exception.release.mail'), mail.name, mail.value))
    }

    void validateMailList(ListStringProperty emails) {
        def value = emails.value
        if (!value || !value.every { it?.trim() ==~ MAIL_PATTERN })
            throw new GradleException(format(bundle.getString('exception.release.mail.list'), emails.name, emails.value))
    }
}
