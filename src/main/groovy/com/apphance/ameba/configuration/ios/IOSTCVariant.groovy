package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.configuration.reader.PropertyPersister

class IOSTCVariant extends AbstractIOSVariant {

    IOSTCVariant(String name, IOSConfiguration conf, ApphanceConfiguration apphanceConf, PropertyPersister persister) {
        super(name, conf, apphanceConf, persister)
    }

    @Override
    List<String> buildCmd() {
        //TODO
        null
    }
}
