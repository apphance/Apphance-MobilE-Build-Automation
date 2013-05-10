package com.apphance.ameba.plugins.ios.parsers

import com.apphance.ameba.executor.IOSExecutor
import groovy.json.JsonSlurper

import javax.inject.Inject

class PbxJsonParser {

    @Inject
    IOSExecutor executor

    String plist(String confName, String blueprintIdentifier) {
        def json = parsedPBX()

        def target = json.objects.find { it.key == blueprintIdentifier }
        def buildConfigurationListKey = target.value.buildConfigurationList
        def buildConfigurationList = json.objects.find { it.key == buildConfigurationListKey }
        def buildConfigurations = buildConfigurationList.value.buildConfigurations
        def configurations = json.objects.findAll { it.key in buildConfigurations }
        def conf = configurations.find {
            it.value['name'] == confName
        }

        conf.value.buildSettings['INFOPLIST_FILE']
    }

    private Object parsedPBX() {
        new JsonSlurper().parseText(executor.pbxProjToJSON().join('\n'))
    }
}
