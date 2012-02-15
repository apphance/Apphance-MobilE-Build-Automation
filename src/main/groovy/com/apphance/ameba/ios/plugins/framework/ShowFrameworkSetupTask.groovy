package com.apphance.ameba.ios.plugins.framework

import com.apphance.ameba.AbstractShowSetupTask;

class ShowFrameworkSetupTask extends AbstractShowSetupTask {
    ShowFrameworkSetupTask() {
        super(IOSFrameworkProperty.class)
        this.dependsOn(project.showIOSSetup)
    }
}

