package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.configuration.reader.PropertyPersister
import com.apphance.ameba.plugins.ios.parsers.PbxJsonParser
import com.apphance.ameba.plugins.ios.parsers.PlistParser

class IOSTCVariant extends AbstractIOSVariant {

    IOSTCVariant(String name,
                 IOSConfiguration conf,
                 IOSReleaseConfiguration releaseConf,
                 ApphanceConfiguration apphanceConf,
                 PlistParser plistParser,
                 PbxJsonParser pbxJsonParser,
                 PropertyPersister persister) {
        super(name, conf, releaseConf, apphanceConf, pbxJsonParser, plistParser, persister)
    }

    @Override
    File getPlist() {

    }

    @Override
    String getBuildableName() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    List<String> buildCmd() {
        //TODO
        null
    }
}
