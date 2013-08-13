package com.apphance.flow.configuration.apphance

import groovy.json.JsonSlurper

import static com.apphance.flow.configuration.apphance.ApphanceMode.DISABLED
import static com.google.common.base.Preconditions.checkArgument
import static org.apache.commons.lang.StringUtils.isNotBlank
import static org.gradle.api.logging.Logging.getLogger

class ApphanceArtifactory {

    private logger = getLogger(getClass())

    public static final String ANDROID_APPHANCE_ARTIFACTORY = 'http://repo1.maven.org/maven2/com/utest/'
    public static final String POLIDEA_ARTIFACTORY = 'https://dev.polidea.pl/artifactory'
    public static final String POLIDEA_REPO_NAME = 'libs-releases-local'
    public static final String IOS_APPHANCE_ARTIFACTORY = "$POLIDEA_ARTIFACTORY/$POLIDEA_REPO_NAME"

    List<String> androidLibraries(ApphanceMode mode) {
        checkArgument(mode && mode != DISABLED, "Invalid apphance mode: $mode")
        androidLibs.call(mode)
    }

    @Lazy
    private Closure<List<String>> androidLibs = { ApphanceMode mode ->
        try {
            def url = "${ANDROID_APPHANCE_ARTIFACTORY}apphance-$mode.repoSuffix/maven-metadata.xml".toURL()
            def metadata = new XmlSlurper().parseText(url.text)
            metadata.versioning.versions.version.collect { it.text() } as List<String>
        } catch (Exception exp) {
            logger.warn "error during parsing apphance lib versions from maven: $exp.message"
            []
        }
    }.memoize()

    List<String> iOSLibraries(ApphanceMode mode, String arch) {
        checkArgument(mode && mode != DISABLED, "Invalid apphance mode: $mode")
        checkArgument(isNotBlank(arch), "Invalid arch: $arch")
        iosLibs.call(mode, arch)
    }

    @Lazy
    private Closure<List<String>> iosLibs = { ApphanceMode mode, String arch ->
        def url = "${POLIDEA_ARTIFACTORY}/api/search/artifact?name=apphance-$mode.repoSuffix*${arch}*.zip&repos=$POLIDEA_REPO_NAME".toURL()
        def response = new JsonSlurper().parseText(url.text) as Map
        response.results.uri.collect {
            def hyphenIndexes = it.findIndexValues { it2 -> it2 == '-' }
            it.substring((hyphenIndexes[-2] as int) + 1, hyphenIndexes[-1] as int)
        }.unique().sort() as List<String>
    }.memoize()

    List<String> iOSArchs(ApphanceMode mode) {
        checkArgument(mode && mode != DISABLED, "Invalid apphance mode: $mode")
        archs.call(mode)
    }

    @Lazy
    private Closure<List<String>> archs = { ApphanceMode mode ->
        def url = "${POLIDEA_ARTIFACTORY}/api/search/artifact?name=apphance-$mode.repoSuffix*.zip&repos=$POLIDEA_REPO_NAME".toURL()
        def response = new JsonSlurper().parseText(url.text) as Map
        response.results.uri.collect { it.substring(it.lastIndexOf('-') + 1, it.lastIndexOf('.')) }.unique() as List<String>
    }.memoize()

}
