package com.apphance.flow.plugins.ios.apphance.source

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.plugins.ios.apphance.pbx.IOSApphancePbxEnhancer

interface IOSApphanceSourceEnhancerFactory {
    IOSApphanceSourceEnhancer create(AbstractIOSVariant variant, IOSApphancePbxEnhancer apphancePbxEnhancer)
}