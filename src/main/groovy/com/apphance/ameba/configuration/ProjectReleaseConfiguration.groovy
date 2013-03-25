package com.apphance.ameba.configuration

import javax.inject.Inject


class ProjectReleaseConfiguration  {

    @Inject
    ProjectConfiguration projectConfiguration

    private boolean enabled = false//TODO from PropertyExtractor

    boolean isEnabled() { enabled && projectConfiguration.enabled }

    void registerConfiguration(ConfigurationSorter resolver) {
        resolver.add([
                dependsOn: projectConfiguration,
                properties: [
                        [:],
                        [:]
                ]]
        )

    }
}
