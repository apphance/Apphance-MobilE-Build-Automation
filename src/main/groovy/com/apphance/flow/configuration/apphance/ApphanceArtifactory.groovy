package com.apphance.flow.configuration.apphance

import groovy.json.JsonSlurper

import static com.apphance.flow.configuration.apphance.ApphanceLibType.libForMode
import static com.apphance.flow.configuration.apphance.ApphanceMode.DISABLED
import static com.google.common.base.Preconditions.checkArgument
import static org.apache.commons.lang.StringUtils.isNotBlank
import static org.apache.commons.lang.StringUtils.isNotEmpty

class ApphanceArtifactory {

    public static final String APPHANCE_ARTIFACTORY_URL = 'https://dev.polidea.pl/artifactory/libs-releases-local/'
    public static final String APPHANCE_ARTIFACTORY_REST_URL = 'https://dev.polidea.pl/artifactory/api/storage/libs-releases-local/'

    List<String> androidLibraries(ApphanceMode mode) {
        checkArgument(mode && mode != DISABLED, "Invalid apphance mode: $mode")
        androidLibs.call(mode)
    }

    @Lazy
    private Closure<List<String>> androidLibs = { ApphanceMode mode ->
        def response = readStreamFromUrl("$APPHANCE_ARTIFACTORY_REST_URL/com/apphance/android.${libForMode(mode).groupName}")
        getParsedVersions(response)
    }.memoize()

    List<String> iOSLibraries(ApphanceMode mode, String arch) {
        checkArgument(mode && mode != DISABLED, "Invalid apphance mode: $mode")
        checkArgument(isNotBlank(arch), "Invalid arch: $arch")
        iosLibs.call(mode, arch)
    }

    @Lazy
    private Closure<List<String>> iosLibs = { ApphanceMode mode, String arch ->
        def response = readStreamFromUrl("$APPHANCE_ARTIFACTORY_REST_URL/com/apphance/ios.${libForMode(mode).groupName}.$arch")
        getParsedVersions(response)
    }.memoize()

    List<String> iOSArchs(ApphanceMode mode) {
        checkArgument(mode && mode != DISABLED, "Invalid apphance mode: $mode")
        archs.call(mode)
    }

    @Lazy
    private Closure<List<String>> archs = { ApphanceMode mode ->
        def response = readStreamFromUrl("$APPHANCE_ARTIFACTORY_REST_URL/com/apphance")
        if (isNotEmpty(response)) {
            def json = new JsonSlurper().parseText(response)

            return json.children.findAll {
                it.uri.startsWith("/ios.${libForMode(mode).groupName}")
            }*.uri.collect {
                it.split('\\.')[2]
            }*.trim().unique().sort()
        }
        []
    }.memoize()

    private String readStreamFromUrl(String url) {
        try { return url.toURL().openStream().readLines().join('\n') } catch (e) { return '' }
    }

    private List<String> getParsedVersions(String response) {
        if (isNotEmpty(response)) {
            def json = new JsonSlurper().parseText(response)
            return json.children*.uri.collect { it.replace('/', '') }
        }
        []
    }
}
