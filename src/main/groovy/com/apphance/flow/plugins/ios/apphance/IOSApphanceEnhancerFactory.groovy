package com.apphance.flow.plugins.ios.apphance

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant

interface IOSApphanceEnhancerFactory {

    IOSApphanceEnhancer create(AbstractIOSVariant variant)
}