package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.configuration.reader.PropertyPersister

class IOSTCVariant extends AbstractIOSVariant {

    IOSTCVariant(String name, IOSConfiguration conf, IOSReleaseConfiguration releaseConf, ApphanceConfiguration apphanceConf, PropertyPersister persister) {
        super(name, conf, releaseConf, apphanceConf, persister)
    }

    @Override
    File getPlist() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
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
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    List<String> buildCmd() {
        //TODO
        null
    }
}
