package com.apphance.ameba.android.plugins.test

import com.apphance.ameba.AbstractShowSetupTask;

class ShowAndroidTestSetupTask extends AbstractShowSetupTask {
    ShowAndroidTestSetupTask() {
        super(AndroidTestProperty.class)
        this.dependsOn(project.showAndroidSetup)
    }
}