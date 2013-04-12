package com.apphance.ameba.plugins.android.apphance.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
import static org.gradle.api.logging.Logging.getLogger

class AndroidLogsConversionTask extends DefaultTask {

    private l = getLogger(getClass())

    static String taskName = 'convertLogsToAndroid'
    String group = AMEBA_APPHANCE_SERVICE
    String description = 'Converts all logs to android from apphance logs for the source project'

    @TaskAction
    public void convertLogsToAndroid() {
        File inDir = project.rootDir
        l.debug("Replacing apphance logs with android in: $inDir.absolutePath")
        project.ant.replace(
                casesensitive: 'true',
                token: 'import com.apphance.android.Log;',
                value: 'import android.util.Log;',
                summary: true) {
            fileset(dir: new File(inDir, 'src')) {
                include(name: '**/*.java')
            }
        }
    }
}
