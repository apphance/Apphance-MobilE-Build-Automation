package com.apphance.ameba.plugins.ios.apphance.tasks

import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant

public interface AddIOSApphanceTaskFactory {

    AddIOSApphanceTask create(AbstractIOSVariant abstractIOSVariant)

}