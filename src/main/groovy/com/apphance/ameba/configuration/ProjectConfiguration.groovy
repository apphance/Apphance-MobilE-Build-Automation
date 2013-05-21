package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.StringProperty

interface ProjectConfiguration extends Configuration {

    String getVersionCode()

    String getExtVersionCode()

    String getVersionString()

    String getExtVersionString()

    String getFullVersionString()

    String getProjectVersionedName()

    StringProperty getProjectName()

    File getTmpDir()

    File getBuildDir()

    File getLogDir()

    File getRootDir()

    Collection<String> getSourceExcludes()
}