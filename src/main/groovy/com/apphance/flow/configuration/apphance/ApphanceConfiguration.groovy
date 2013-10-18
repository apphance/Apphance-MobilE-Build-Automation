package com.apphance.flow.configuration.apphance

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.properties.BooleanProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.google.inject.Singleton

import javax.inject.Inject

import static com.apphance.flow.configuration.properties.BooleanProperty.POSSIBLE_BOOLEAN
import static org.apache.commons.lang.StringUtils.isEmpty

/**
 * This configuration keeps values used while uploading prepared artifacts to <a href="http://apphance.com">apphance.com
 * </a> service.
 */
@Singleton
class ApphanceConfiguration extends AbstractConfiguration {

    String configurationName = 'Apphance Configuration'
    private boolean enabledInternal = false

    @Inject ProjectConfiguration conf

    @Override
    boolean isEnabled() {
        enabledInternal && conf.isEnabled()
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    def user = new StringProperty(
            name: 'apphance.user',
            message: 'Apphance user (used for uploading artifacts to apphance server)',
            doc: { docBundle.getString('apphance.user') }

    )

    def pass = new StringProperty(
            name: 'apphance.pass',
            message: 'Apphance pass (used for uploading artifacts to apphance server)',
            doc: { docBundle.getString('apphance.pass') }
    )
}
