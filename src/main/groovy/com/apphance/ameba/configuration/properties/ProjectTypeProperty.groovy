package com.apphance.ameba.configuration.properties

import com.apphance.ameba.detection.ProjectType

class ProjectTypeProperty extends AbstractProperty<ProjectType> {

    @Override
    void setValue(String value) {
        if (value != null) {
            this.@value = ProjectType.valueOf(value)
        }
    }
}
