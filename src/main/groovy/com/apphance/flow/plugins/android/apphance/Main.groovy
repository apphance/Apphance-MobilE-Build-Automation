package com.apphance.flow.plugins.android.apphance

import com.apphance.flow.plugins.android.apphance.tasks.AddApphanceToAndroid

import static com.apphance.flow.configuration.apphance.ApphanceMode.PROD
import static com.apphance.flow.configuration.apphance.ApphanceMode.QA

class Main {

    public static void main(String[] args) {
        println "Standalone apphance adding tool"
        if (args.size() != 7) usage("Bad number of argument. Got: ${args.size()}, should be: 7")
        String mode = args[0]
        if (!(mode in ['prod', 'pre'])) usage('Incorrect mode. Should be one of: pre, prod')

        def apphanceKey = args[1]

        def version = args[2]
        if (!(version ==~ /\d+(\.(\d+))*/)) usage("Incorrect apphance version. Shound match: " + /\d+(\.(\d+))*/)

        Boolean reportOnShakeEnabled = args[3] as Boolean
        Boolean uTestEnabled = args[4] as Boolean
        String defaultUser = args[5]
        Boolean screenshotFromGallery = args[6] as Boolean

        def foundManifest = false
        File currentDir = new File('.')
        currentDir.traverse { if (it.name == 'AndroidManifest.xml') foundManifest = true }
        if (!foundManifest) usage("It is not an Android project. Run this application inside root directory of your app")

        println "Adding apphance in mode: $mode, key: $apphanceKey"

        def apphance = new AddApphanceToAndroid(currentDir, apphanceKey, mode == 'prod' ? PROD : QA, version, reportOnShakeEnabled, uTestEnabled,
                defaultUser, screenshotFromGallery)
        apphance.addApphance()
    }

    def static usage(String message = null) {
        if (message) println "ERROR: $message"
        println "Usage: java -jar path-to-flow.jar [prod|pre] " +
                "<ApphanceKey> <ApphanceVersion> <ReportOnShakeEnabled> <UTestEnabled> <DefaultUser> <ScreenshotFromGallery>"
        System.exit(1)
    }
}
