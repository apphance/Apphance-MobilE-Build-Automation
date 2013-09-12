package com.apphance.flow.configuration.ios.variants

import com.google.inject.assistedinject.Assisted

import javax.inject.Inject

class IOSWorkspaceVariant extends AbstractIOSVariant {

    @Inject
    IOSWorkspaceVariant(@Assisted String name) {
        super(name)
    }
}
