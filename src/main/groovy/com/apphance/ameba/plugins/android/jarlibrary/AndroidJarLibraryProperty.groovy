package com.apphance.ameba.plugins.android.jarlibrary

/**
 * Property for Android jar library.
 *
 */
public enum AndroidJarLibraryProperty {

    RES_PREFIX('android.jarLibrary.resPrefix', 'Internal directory name used to embed resources in the jar', ''),

    public static final String DESCRIPTION = 'Android jar library properties'
    final String propertyName
    final String description
    final String defaultValue

    AndroidJarLibraryProperty(String propertyName, String description, String defaultValue = null) {
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}
