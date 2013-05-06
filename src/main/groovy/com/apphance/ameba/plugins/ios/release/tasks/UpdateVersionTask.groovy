package com.apphance.ameba.plugins.ios.release.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.plugins.release.tasks.AbstractUpdateVersionTask

import javax.inject.Inject

class UpdateVersionTask extends AbstractUpdateVersionTask {

    @Inject
    IOSPlistProcessor plistProcessor

    @Override
    void updateDescriptor(String versionCode, String versionString) {
        plistProcessor.incrementPlistVersion((conf as IOSConfiguration).plist.value, versionCode, versionString)
    }
}
