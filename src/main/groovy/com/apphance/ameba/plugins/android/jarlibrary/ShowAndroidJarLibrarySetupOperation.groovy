package com.apphance.ameba.plugins.android.jarlibrary

import com.apphance.ameba.AbstractShowSetupOperation;

/**
 * Shows all android jar library properties.
 *
 */
class ShowAndroidJarLibrarySetupOperation extends AbstractShowSetupOperation {
    ShowAndroidJarLibrarySetupOperation() {
        super(AndroidJarLibraryProperty.class)
    }
}