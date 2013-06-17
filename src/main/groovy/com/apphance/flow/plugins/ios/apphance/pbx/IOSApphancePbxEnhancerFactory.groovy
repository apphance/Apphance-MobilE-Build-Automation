package com.apphance.flow.plugins.ios.apphance.pbx

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant

interface IOSApphancePbxEnhancerFactory {

    IOSApphancePbxEnhancer create(AbstractIOSVariant variant)
}