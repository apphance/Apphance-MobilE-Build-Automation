package com.apphance.flow.plugins.android.apphance.tasks

import static org.gradle.api.logging.Logging.getLogger

class ApphanceLogsConversionTask {

    static String NAME = 'convertLogsToApphance'

    private l = getLogger(getClass())

    private AntBuilder ant

    ApphanceLogsConversionTask(AntBuilder ant) {
        this.ant = ant
    }

    void convertLogsToApphance(File inDir) {
        l.debug("Replacing Android logs with Apphance in: $inDir.absolutePath")
        ant.replace(
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
