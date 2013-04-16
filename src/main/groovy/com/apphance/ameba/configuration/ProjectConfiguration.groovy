package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.StringProperty

public interface ProjectConfiguration extends Configuration {

    String getFullVersionString()

    String getProjectVersionedName()

    StringProperty getProjectName()

    FileProperty getTmpDir()

    FileProperty getBuildDir()

    FileProperty getLogDir()

    Collection<String> getSourceExcludes()
}