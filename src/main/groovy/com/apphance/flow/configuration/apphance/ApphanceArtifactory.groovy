package com.apphance.flow.configuration.apphance

import static com.apphance.flow.configuration.apphance.ApphanceMode.DISABLED
import static com.google.common.base.Preconditions.checkArgument
import static org.gradle.api.logging.Logging.getLogger

class ApphanceArtifactory {

    private logger = getLogger(getClass())

    public static final String ANDROID_APPHANCE_REPO = 'http://repo1.maven.org/maven2/com/utest/'
    public static final String POLIDEA_ARTIFACTORY = 'https://dev.polidea.pl/artifactory'
    public static final String POLIDEA_REPO_NAME = 'libs-releases-local'
    public static final String IOS_APPHANCE_REPO = "$POLIDEA_ARTIFACTORY/$POLIDEA_REPO_NAME"

    List<String> androidLibraries(ApphanceMode mode) {
        checkArgument(mode && mode != DISABLED, "Invalid apphance mode: $mode")
        androidLibs.call(mode)
    }

    @Lazy
    private Closure<List<String>> androidLibs = { ApphanceMode mode ->
        try {
            def url = "${ANDROID_APPHANCE_REPO}apphance-$mode.repoSuffix/maven-metadata.xml".toURL()
            def metadata = new XmlSlurper().parseText(url.text)
            metadata.versioning.versions.version.collect { it.text() } as List<String>
        } catch (Exception exp) {
            logger.warn "error during parsing apphance lib versions from maven: $exp.message"
            []
        }
    }.memoize()

    List<String> iOSLibraries(ApphanceMode mode) {
        checkArgument(mode && mode != DISABLED, "Invalid apphance mode: $mode")
        iosLibs.call(mode)
    }

    @Lazy
    private Closure<List<String>> iosLibs = { ApphanceMode mode ->
        def url = "${POLIDEA_ARTIFACTORY}/${POLIDEA_REPO_NAME}/com/utest/apphance-${mode.repoSuffix}/maven-metadata.xml".toURL()
        new XmlSlurper().parseText(url.text).versioning.versions.version*.text().unique().sort() as List<String>
    }.memoize()
}
