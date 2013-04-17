package com.apphance.ameba.plugins.android.apphance.tasks

import static org.gradle.api.logging.Logging.getLogger

class AndroidLogsConversionTask {

    private l = getLogger(getClass())

    static String NAME = 'convertLogsToAndroid'

    private AntBuilder ant

    AndroidLogsConversionTask(AntBuilder project) {
        this.ant = project
    }

    void convertLogsToAndroid(File inDir) {
        l.debug("Replacing apphance logs with android in: $inDir.absolutePath")
        ant.replace(
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
