package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.configuration.reader.PropertyPersister
import com.apphance.ameba.plugins.ios.parsers.PbxJsonParser
import com.apphance.ameba.plugins.ios.parsers.PlistParser
import com.apphance.ameba.plugins.ios.parsers.XCSchemeParser

class IOSSchemeVariant extends AbstractIOSVariant {


    XCSchemeParser schemeParser

    IOSSchemeVariant(String name,
                     IOSConfiguration conf,
                     IOSReleaseConfiguration releaseConf,
                     ApphanceConfiguration apphanceConf,
                     XCSchemeParser schemeParser,
                     PbxJsonParser pbxJsonParser,
                     PlistParser plistParser,
                     PropertyPersister persister) {
        super(name, conf, releaseConf, apphanceConf, pbxJsonParser, plistParser, persister)
        this.schemeParser = schemeParser
    }

    @Override
    File getPlist() {
        String confName = schemeParser.configurationName(name)
        String blueprintId = schemeParser.blueprintIdentifier(name)
        new File(conf.rootDir, pbxJsonParser.plistForScheme(confName, blueprintId))
    }

    @Override
    String getBuildableName() {
        schemeParser.buildableName(name)
    }

    @Override
    List<String> buildCmd() {
        //TODO
        null
    }
}
