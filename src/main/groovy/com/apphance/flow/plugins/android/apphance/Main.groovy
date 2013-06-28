package com.apphance.flow.plugins.android.apphance

import com.apphance.flow.plugins.android.apphance.tasks.AddApphanceToAndroid

import static com.apphance.flow.configuration.apphance.ApphanceMode.PROD
import static com.apphance.flow.configuration.apphance.ApphanceMode.QA

class Main {

    public static void main(String[] args) {
        println "Standalone apphance adding tool"
        if (args.size() != 2) usage("Bad number of argument. Got: ${args.size()}, should be: 2")
        String mode = args[0]
        if (!(mode in ['prod', 'pre'])) usage('Incorrect mode. Should be one of: pre, prod')

        def apphanceKey = args[1]
        File currentDir = new File('.')
        File manifest = new File(currentDir, 'AndroidManifest.xml')
        if (!manifest.exists()) {
            usage("It is not an Android project. Run this application inside root directory of your app")
        }

        println "Adding apphance in mode: $mode, key: $apphanceKey"

        def apphance = new AddApphanceToAndroid(currentDir, apphanceKey, mode == 'prod' ? PROD : QA, '1.9-RC1')
        apphance.addApphance()
    }

    def static usage(String message = null) {
        if (message) println "ERROR: $message"
        println "Usage: java -jar path-to-flow.jar [prod|pre] <APPHANCE_KEY> "
        System.exit(1)
    }
}
