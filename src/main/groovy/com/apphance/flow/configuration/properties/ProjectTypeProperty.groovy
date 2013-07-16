package com.apphance.flow.configuration.properties

import com.apphance.flow.detection.project.ProjectType

class ProjectTypeProperty extends AbstractProperty<ProjectType> {

    @Override
    void setValue(String value) {
        if (value?.trim()) {
            this.@value = ProjectType.valueOf(value)
        }
    }
}
