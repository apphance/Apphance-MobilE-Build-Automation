package com.apphance.ameba.ios.plugins.buildplugin

import com.apphance.ameba.AbstractShowSetupTask;

class ShowIOSSetupTask extends AbstractShowSetupTask {
    ShowIOSSetupTask() {
        super(IOSProjectProperty.class)
        this.dependsOn(project.showBaseSetup)
    }
}