package com.apphance.flow.plugins.release

import static org.gradle.api.logging.Logging.getLogger

class FlowArtifact {

    private logger = getLogger(getClass())

    String name
    URL url
    File location

    List<FlowArtifact> childArtifacts = []

    String getRelativeUrl(def baseUrl) {
        logger.info("Retrieving relative url from $url with base $baseUrl")
        String stringUrl = url.toString()
        String currentBaseUrl = getParentPath(baseUrl.toString())
        String prefix = ""
        while (currentBaseUrl != "") {
            if (stringUrl.startsWith(currentBaseUrl)) {
                def relativeUrl = prefix + stringUrl.substring(currentBaseUrl.length() + 1)
                logger.info("Relative url from $url with base $baseUrl : $relativeUrl")
                return relativeUrl
            } else {
                currentBaseUrl = getParentPath(currentBaseUrl);
                prefix = prefix + "../"
            }
        }
        logger.info("Return absolute url - no common url found : $url")
        url.toString()
    }

    private String getParentPath(String path) {
        if (!path || path.equals('/')) {
            return ''
        }
        int lastSlashPos = path.lastIndexOf('/')
        if (lastSlashPos >= 0) {
            return path.substring(0, lastSlashPos);
        }
        ''
    }

    @Override
    public String toString() {
        this.properties
    }
}
