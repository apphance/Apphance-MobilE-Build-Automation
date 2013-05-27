package com.apphance.ameba.plugins.android.analysis.tasks

import com.apphance.ameba.configuration.android.AndroidAnalysisConfiguration
import org.gradle.api.Project
import org.gradle.api.logging.Logging

import javax.inject.Inject

class AndroidAnalysisResourceLocator {

    private l = Logging.getLogger(getClass())

    @Inject AndroidAnalysisConfiguration analysisConf

    URL getResourceUrl(Project project, String resourceName) {
        l.info("Reading resource $resourceName")

        URL configUrl = project.file('config/analysis').toURI().toURL()
        URL baseUrl = configUrl

        if (analysisConf.analysisConfigUrl.isSet()) {
            baseUrl = analysisConf.analysisConfigUrl.value
        }

        URL targetURL = new URL(baseUrl, resourceName)
        if (targetURL.getProtocol() != 'file') {
            l.info("Downloading file from $targetURL")
            try {
                targetURL.getContent() // just checking if we can read it
                return targetURL
            } catch (IOException e) {
                l.warn("Exception $e while reading from $targetURL. Falling back")
                targetURL = new URL(configUrl, resourceName)
            }
        }
        l.info("Reading resource from file $targetURL")
        if (!(new File(targetURL.toURI()).exists())) {
            def url = this.class.getResource(resourceName)
            l.info("Reading resource from internal $url as file $targetURL not found")
            return url
        }
        return targetURL
    }
}
