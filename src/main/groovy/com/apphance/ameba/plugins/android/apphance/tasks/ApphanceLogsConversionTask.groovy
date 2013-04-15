package com.apphance.ameba.plugins.android.apphance.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
import static org.gradle.api.logging.Logging.getLogger

class ApphanceLogsConversionTask extends DefaultTask {

    static String NAME = 'convertLogsToApphance'
    String group = AMEBA_APPHANCE_SERVICE
    String description = 'Converts all logs to apphance from android logs for the source project'

    private l = getLogger(getClass())

    @TaskAction
    public void convertLogsToApphance() {
        File inDir = project.rootDir
        l.debug("Replacing Android logs with Apphance in: $inDir.absolutePath")
        project.ant.replace(
                casesensitive: 'true',
                token: 'import android.util.Log;',
                value: 'import com.apphance.android.Log;',
                summary: true) {
            fileset(dir: new File(inDir, 'src')) {
                include(name: '**/*.java')
            }
        }
    }
}
