package com.apphance.ameba.android.plugins.apphance.tasks

import static org.gradle.api.logging.Logging.getLogger

//TODO to be tested
class AndroidLogsConverter {

    private AntBuilder ant
    private l = getLogger(getClass())

    AndroidLogsConverter(AntBuilder ant) {
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
