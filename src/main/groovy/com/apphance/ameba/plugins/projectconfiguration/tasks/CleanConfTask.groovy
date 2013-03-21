package com.apphance.ameba.plugins.projectconfiguration.tasks

import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration

class CleanConfTask {

    private ProjectConfiguration conf

    CleanConfTask(ProjectConfiguration conf) {
        this.conf = conf
    }

    void clean() {
        conf.buildDirectory.deleteDir()
        conf.tmpDirectory.deleteDir()
        conf.logDirectory.deleteDir()
        conf.buildDirectory.mkdirs()
        conf.logDirectory.mkdirs()
        conf.tmpDirectory.mkdirs()
    }
}
