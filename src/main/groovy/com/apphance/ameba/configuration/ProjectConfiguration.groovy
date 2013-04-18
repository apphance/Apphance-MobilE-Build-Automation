package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.StringProperty

public interface ProjectConfiguration extends Configuration {

    String getFullVersionString()

    String getProjectVersionedName()

    StringProperty getProjectName()

    File getTmpDir()

    File getBuildDir()

    File getLogDir()

    File getRootDir()

    Collection<String> getSourceExcludes()
}