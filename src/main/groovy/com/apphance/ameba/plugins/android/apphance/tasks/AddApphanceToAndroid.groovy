package com.apphance.ameba.plugins.android.apphance.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceMode
import com.apphance.ameba.plugins.android.AndroidManifestHelper
import com.apphance.ameba.plugins.apphance.ApphancePluginCommons
import org.gradle.api.GradleException
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.FILES
import static org.gradle.api.logging.Logging.getLogger

//TODO to be tested
//TODO to be refactored when apphance artifact uploading is back in progress
@Mixin(ApphancePluginCommons)
class AddApphanceToAndroid {

    def l = getLogger(getClass())

    private static final JAR_PATTERN = ~/.*android\.(pre\-)?production\-(\d+\.)+\d+\.jar/

    @Inject
    private Project project
    @Inject
    private AndroidConfiguration androidConf
    @Inject
    private AndroidManifestHelper manifestHelper
    @Inject
    private AntBuilder ant

    public void addApphance(AndroidVariantConfiguration avc) {
        File variantDir = avc.tmpDir
        if (!checkIfApphancePresent(variantDir)) {
            l.debug("Apphance not found in project: $variantDir.absolutePath")
            File mainFile
            boolean isActivity
            (mainFile, isActivity) = getMainApplicationFile(variantDir)
            if (mainFile) {
                new ApphanceLogsConversionTask(ant).convertLogsToApphance(variantDir)
                addApphanceInit(variantDir, mainFile, isActivity, avc.apphanceAppKey.value, avc.apphanceMode.value)
                copyApphanceJar(variantDir)
                addApphanceToManifest(variantDir)
            }
        } else {
            l.debug("Apphance found in project: $variantDir.absolutePath")
        }
    }

    private File getVariantDir(String variant) {
        new File(androidConf.tmpDir.value, variant)
    }

    private boolean checkIfApphancePresent(File directory) {
        boolean found = false
        directory.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) { file ->
            if (file.name.endsWith('.java') && file.text.contains('Apphance.startNewSession')) {
                found = true
            }
        }
        if (!found) {
            File libsDir = new File(directory, 'libs/')
            if (libsDir.exists()) {
                libsDir.eachFileMatch(JAR_PATTERN) { found = true }
            }
        }
        if (!found) {
            found = manifestHelper.isApphanceActivityPresent(directory)
        }
        if (!found) {
            found = manifestHelper.isApphanceInstrumentationPresent(directory)
        }
        found
    }

    def getMainApplicationFile(File directory) {
        String mainApplicationFileName = manifestHelper.getApplicationName(directory)
        mainApplicationFileName = mainApplicationFileName.replace('.', '/')
        mainApplicationFileName = mainApplicationFileName + '.java'
        mainApplicationFileName = 'src/' + mainApplicationFileName
        File f = new File(directory, mainApplicationFileName)
        if (f.exists()) {
            return [f, false]
        } else {
            String mainActivityName = manifestHelper.getMainActivityName(directory)
            mainActivityName = mainActivityName.replace('.', '/')
            mainActivityName = mainActivityName + '.java'
            mainActivityName = 'src/' + mainActivityName
            if (!(new File(directory, mainActivityName)).exists()) {
                f = null
            } else {
                f = new File(directory, mainActivityName)
            }
            return [f, true]
        }
    }

    private def addApphanceInit(File directory, File mainFile, boolean isActivity, String apphanceAppKey, ApphanceMode apphanceMode) {
        l.debug("Adding apphance init to file: $mainFile.absolutePath")
        File newMainClassFile = new File(directory, 'newMainClassFile.java')
        String startSession = """Apphance.startNewSession(this, "$apphanceAppKey", ${mapApphanceMode(apphanceMode)});"""
        String importApphance = 'import com.apphance.android.Apphance;'
        boolean onCreatePresent = isOnCreatePresent(mainFile)
        if (onCreatePresent) {
            addApphanceInitToExistingOnCreate(startSession, importApphance, mainFile, newMainClassFile)
        } else {
            String onCreateMethod
            if (isActivity) {
                onCreateMethod = " public void onCreate(final Bundle savedInstanceState) { super.onCreate(savedInstanceState); $startSession } "
            } else {
                onCreateMethod = " public void onCreate() { super.onCreate(); $startSession } "
            }
            addApphanceInitIfOnCreateMissing(onCreateMethod, importApphance, mainFile, newMainClassFile)
        }
        mainFile.delete()
        mainFile << newMainClassFile.text
        newMainClassFile.delete()
    }

    private String mapApphanceMode(ApphanceMode apphanceMode) {
        (apphanceMode == ApphanceMode.QA) ?
            'Apphance.Mode.QA'
        :
            'Apphance.Mode.Silent'
    }

    private boolean isOnCreatePresent(File mainFile) {
        boolean present = false
        mainFile.eachLine { line ->
            if (line.matches('.*void.*onCreate\\(.*')) {
                present = true
            }
        }
        return present
    }

    private void addApphanceInitToExistingOnCreate(String startSession, String importApphance, File mainFile, File newMainClassFile) {
        boolean onCreateAdded = false
        boolean searchingForOpeningBrace = false
        newMainClassFile.withWriter { out ->
            mainFile.eachLine { line ->
                if (line.matches('.*void\\sonCreate\\(.*') && !onCreateAdded) {
                    searchingForOpeningBrace = true
                } else if (line.matches('package\\s*.*')) {
                    line = "$line $importApphance"
                }
                if (!onCreateAdded && searchingForOpeningBrace && line.matches('.*\\{.*')) {
                    out.println(line.replaceAll('\\{', "{ $startSession"))
                    onCreateAdded = true
                } else {
                    out.println(line)
                }
            }
        }
        if (!onCreateAdded) {
            l.warn('Could not find onCreate(). Apphance not added.')
        }
    }

    private void addApphanceInitIfOnCreateMissing(String onCreateMethod, String importApphance, File mainFile, File newMainClassFile) {
        boolean onCreateAdded = false
        newMainClassFile.delete()
        newMainClassFile.withWriter { out ->
            mainFile.eachLine { line ->
                if (line.matches('.*class.*extends.*\\{.*') && !onCreateAdded) {
                    out.println(line << onCreateMethod)
                    onCreateAdded = true
                } else if (line.matches('package\\s*.*')) {
                    out.println(line << importApphance)
                } else {
                    out.println(line)
                }
            }
        }
    }

    private copyApphanceJar(File directory) {

        def apphanceLibDependency = prepareApphanceLibDependency(project, 'com.apphance:android.pre-production:1.8+')

        def libsDir = new File(directory, 'libs')
        libsDir.mkdirs()

        libsDir.eachFileMatch(JAR_PATTERN) {
            l.debug("Removing old apphance jar: ${it.name}")
            it.delete()
        }
        l.debug("Copying apphance jar to: $libsDir")

        try {
            project.copy {
                from { project.configurations.apphance }
                into libsDir
            }
        } catch (e) {
            def msg = "Error while resolving dependency: '$apphanceLibDependency'"
            l.error("""$msg.
To solve the problem add correct dependency to gradle.properties file or add -Dapphance.lib=<apphance.lib> to invocation.
Dependency should be added in gradle style to 'apphance.lib' entry""")
            throw new GradleException(msg)
        }
    }

    private addApphanceToManifest(File directory) {
        l.debug('Adding apphance to manifest')
        manifestHelper.addApphance(directory)
    }
}
