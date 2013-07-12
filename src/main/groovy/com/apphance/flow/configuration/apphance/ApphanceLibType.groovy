package com.apphance.flow.configuration.apphance

import org.gradle.api.GradleException

import static com.apphance.flow.configuration.apphance.ApphanceMode.QA
import static com.apphance.flow.configuration.apphance.ApphanceMode.SILENT

enum ApphanceLibType {

    PROD('production'), PRE_PROD('pre-production')

    private String groupName

    ApphanceLibType(String groupName) {
        this.groupName = groupName
    }

    String getGroupName() {
        groupName
    }

    static ApphanceLibType libForMode(ApphanceMode mode) {
        switch (mode) {
            case [QA, SILENT]:
                return PRE_PROD
            case ApphanceMode.PROD:
                return PROD
            default:
                throw new GradleException("Invalid apphance mode: $mode")
        }
    }
}