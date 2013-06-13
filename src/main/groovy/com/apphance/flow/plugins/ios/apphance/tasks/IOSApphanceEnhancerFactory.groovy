package com.apphance.flow.plugins.ios.apphance.tasks

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant

interface IOSApphanceEnhancerFactory {

    IOSApphanceEnhancer create(AbstractIOSVariant variant)
}