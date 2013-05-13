package com.apphance.ameba.plugins.ios.parsers

import com.apphance.ameba.executor.IOSExecutor
import groovy.json.JsonSlurper

import javax.inject.Inject

class PlistParser {

    @Inject
    IOSExecutor executor

    String getVersionCode(File plist) {
        def json = parsedJson(plist)
        json.CFBundleVersion
    }

    String getVersionString(File plist) {
        def json = parsedJson(plist)
        json.CFBundleShortVersionString
    }

    private Object parsedJson(File plist) {
        def text = executor.plistToJSON(plist).join('\n')
        new JsonSlurper().parseText(text)
    }
}
