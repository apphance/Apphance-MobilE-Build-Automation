package com.apphance.ameba.plugins.ios.release.tasks

import com.apphance.ameba.plugins.ios.parsers.PlistParser
import com.apphance.ameba.plugins.release.tasks.AbstractUpdateVersionTask

import javax.inject.Inject

class UpdateVersionTask extends AbstractUpdateVersionTask {

    @Inject
    PlistParser parser

    @Override
    void updateDescriptor(String versionCode, String versionString) {
        //TODO
        //plistProcessor.incrementPlistVersion((conf as IOSConfiguration).plist.value, versionCode, versionString)
    }
}
