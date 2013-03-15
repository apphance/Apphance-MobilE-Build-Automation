package com.apphance.ameba.android.plugins.apphance.tasks

import static org.gradle.api.logging.Logging.getLogger

class ApphanceLogsConversionTask {

    private AntBuilder ant
    private l = getLogger(getClass())

    ApphanceLogsConversionTask(AntBuilder ant) {
        this.ant = ant
    }

    public void convertLogsToApphance(File inDir) {
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
