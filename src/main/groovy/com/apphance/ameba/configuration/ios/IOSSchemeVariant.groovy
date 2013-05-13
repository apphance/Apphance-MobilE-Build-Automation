package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.configuration.reader.PropertyPersister
import com.apphance.ameba.plugins.ios.parsers.PbxJsonParser
import com.apphance.ameba.plugins.ios.parsers.XCSchemeParser

class IOSSchemeVariant extends AbstractIOSVariant {


    XCSchemeParser schemeParser
    PbxJsonParser pbxJsonParser

    IOSSchemeVariant(String name,
                     IOSConfiguration conf,
                     IOSReleaseConfiguration releaseConf,
                     ApphanceConfiguration apphanceConf,
                     XCSchemeParser schemeParser,
                     PbxJsonParser pbxJsonParser,
                     PropertyPersister persister) {
        super(name, conf, releaseConf, apphanceConf, persister)
        this.schemeParser = schemeParser
        this.pbxJsonParser = pbxJsonParser
    }

    @Override
    File getPlist() {
        String confName = schemeParser.configurationName(name)
        String blueprintId = schemeParser.blueprintIdentifier(name)
        new File(conf.rootDir, pbxJsonParser.plistForScheme(confName, blueprintId))
    }

    @Override
    String getVersionCode() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getVersionString() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
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
