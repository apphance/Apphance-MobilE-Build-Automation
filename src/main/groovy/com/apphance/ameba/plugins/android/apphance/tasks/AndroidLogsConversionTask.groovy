package com.apphance.ameba.plugins.android.apphance.tasks

import static org.gradle.api.logging.Logging.getLogger

class AndroidLogsConversionTask {

    private AntBuilder ant
    private l = getLogger(getClass())

    AndroidLogsConversionTask(AntBuilder ant) {
        this.ant = ant
    }

    public void convertLogsToAndroid(File inDir) {
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
