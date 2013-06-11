package com.apphance.flow.plugins.ios.apphance.tasks

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant

public interface AddIOSApphanceTaskFactory {

    AddIOSApphanceTask create(AbstractIOSVariant abstractIOSVariant)

}