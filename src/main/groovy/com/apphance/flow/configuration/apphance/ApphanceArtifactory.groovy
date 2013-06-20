package com.apphance.flow.configuration.apphance

import groovy.json.JsonSlurper

import static com.apphance.flow.configuration.apphance.ApphanceMode.*
import static com.google.common.base.Preconditions.checkArgument
import static org.apache.commons.lang.StringUtils.isNotBlank
import static org.apache.commons.lang.StringUtils.isNotEmpty

class ApphanceArtifactory {

    public static final String APPHANCE_ARTIFACTORY_URL = 'https://dev.polidea.pl/artifactory/libs-releases-local/'
    public static final String APPHANCE_ARTIFACTORY_REST_URL = 'https://dev.polidea.pl/artifactory/api/storage/libs-releases-local/'

    List<String> androidLibraries(ApphanceMode mode) {
        checkArgument(mode && mode != DISABLED, "Invalid apphance mode: $mode")
        switch (mode) {
            case QA:
            case SILENT:
                return androidPreProdLibs
            case PROD:
                return androidProdLibs
        }
        []
    }

    @Lazy
    private List<String> androidPreProdLibs = {
        def response = readStreamFromUrl("$APPHANCE_ARTIFACTORY_REST_URL/com/apphance/android.pre-production")
        getParsedVersions(response)
    }()

    @Lazy
    private List<String> androidProdLibs = {
        def response = readStreamFromUrl("$APPHANCE_ARTIFACTORY_REST_URL/com/apphance/android.production")
        getParsedVersions(response)
    }()

    List<String> iOSLibraries(ApphanceMode mode, String arch) {
        checkArgument(mode && mode != DISABLED, "Invalid apphance mode: $mode")
        checkArgument(isNotBlank(arch), "Invalid arch: $arch")
        switch (mode) {
            case QA:
            case SILENT:
                return iOSPreProdLibs.call(arch)
            case PROD:
                return iOSProdLibs.call(arch)
        }
        []
    }

    @Lazy
    private Closure<List<String>> iOSPreProdLibs = { arch ->
        def response = readStreamFromUrl("$APPHANCE_ARTIFACTORY_REST_URL/com/apphance/ios.pre-production.$arch")
        getParsedVersions(response)
    }.memoize()

    @Lazy
    private Closure<List<String>> iOSProdLibs = { arch ->
        def response = readStreamFromUrl("$APPHANCE_ARTIFACTORY_REST_URL/com/apphance/ios.production.$arch")
        getParsedVersions(response)
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
