package com.apphance.ameba.ios.plugins.fonemonkey

import com.apphance.ameba.AbstractShowSetupTask;

class ShowFoneMonkeySetupTask extends AbstractShowSetupTask {
    ShowFoneMonkeySetupTask() {
        super(IOSFoneMonkeyProperty.class)
        this.dependsOn(project.showIOSSetup)
    }
}

