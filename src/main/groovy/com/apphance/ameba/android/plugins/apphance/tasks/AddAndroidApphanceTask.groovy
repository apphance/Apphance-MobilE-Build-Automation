package com.apphance.ameba.android.plugins.apphance.tasks

import com.apphance.ameba.android.AndroidManifestHelper
import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.apphance.ApphancePluginCommons
import org.gradle.api.GradleException
import org.gradle.api.Project

import static com.apphance.ameba.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration
import static com.apphance.ameba.apphance.ApphanceProperty.*
import static com.apphance.ameba.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.FILES
import static org.gradle.api.logging.Logging.getLogger

@Mixin(ApphancePluginCommons)

//TODO to be tested
//TODO to be refactored when apphance artifact uploading is back in progress
class AddAndroidApphanceTask {

    def l = getLogger(getClass())

    private static final JAR_PATTERN = ~/.*android\.(pre\-)?production\-(\d+\.)+\d+\.jar/
    private static String EVENT_LOG_WIDGET_PACKAGE = 'com.apphance.android.eventlog.widget'
    private static String EVENT_LOG_ACTIVITY_PACKAGE = 'com.apphance.android.eventlog.activity'

    private Project project
    private AntBuilder ant
    private AndroidProjectConfiguration androidConf
    private AndroidManifestHelper manifestHelper = new AndroidManifestHelper()

    AddAndroidApphanceTask(Project project) {
        this.project = project
        this.ant = project.ant
        this.androidConf = getAndroidProjectConfiguration(project)
    }

    public void addApphance(String variant) {
        File variantDir = getVariantDir(project, variant)
        if (!checkIfApphancePresent(variantDir)) {
            l.debug("Apphance not found in project: $variantDir.absolutePath")
            File mainFile
            boolean isActivity
            (mainFile, isActivity) = getMainApplicationFile(variantDir)
            if (mainFile) {
                new ApphanceLogsConversionTask(ant).convertLogsToApphance(variantDir)
                if (logEvents()) {
                    replaceViewsWithApphance(variantDir)
                }
                addApphanceInit(variantDir, mainFile, isActivity)
                copyApphanceJar(variantDir)
                addApphanceToManifest(variantDir)
            }
        } else {
            l.debug("Apphance found in project: $variantDir.absolutePath")
        }
    }

    private File getVariantDir(Project project, String variant) {
        variant != null ? androidConf.tmpDirs[variant] : project.rootDir
    }

    private boolean checkIfApphancePresent(File directory) {
        boolean found = false
        //TODO jakaś sprytniejsza metoda?
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

    private boolean logEvents() {
        project[APPHANCE_LOG_EVENTS.propertyName].toString().toBoolean()
    }

    private void replaceViewsWithApphance(File directory) {
        l.debug("Replacing android views with apphance loggable versions for: $directory.absolutePath")
        replaceViewWithApphance(directory, 'Button')
        replaceViewWithApphance(directory, 'CheckBox')
        replaceViewWithApphance(directory, 'EditText')
        replaceViewWithApphance(directory, 'ImageButton')
        replaceViewWithApphance(directory, 'ListView')
        replaceViewWithApphance(directory, 'RadioGroup')
        replaceViewWithApphance(directory, 'SeekBar')
        replaceViewWithApphance(directory, 'Spinner')
        replaceViewWithApphance(directory, 'TextView')

        replaceActivityWithApphance(directory, 'Activity')
        replaceActivityWithApphance(directory, 'ActivityGroup')
    }

    private void replaceViewWithApphance(File directory, String viewName) {
        replaceViewExtendsWithApphance(directory, viewName);
        replaceTagResourcesOpeningTag(directory, viewName, EVENT_LOG_WIDGET_PACKAGE + "." + viewName)
        replaceTagResourcesClosingTag(directory, viewName, EVENT_LOG_WIDGET_PACKAGE + "." + viewName)
    }

    private void replaceViewExtendsWithApphance(File directory, String viewName) {
        String newClassName = EVENT_LOG_WIDGET_PACKAGE + '.' + viewName
        l.debug("Replacing extends with Apphance for $viewName to $newClassName")
        ant.replace(casesensitive: 'true', token: "extends $viewName ",
                value: "extends $newClassName ", summary: true) {
            fileset(dir: new File(directory, 'src')) { include(name: '**/*.java') }
        }
    }

    private void replaceTagResourcesOpeningTag(File directory, String tagName, String replacement) {
        l.debug("Replacing tag resources with Apphance for $tagName to $replacement")
        ant.replace(casesensitive: 'true', token: "<$tagName ",
                value: "<$replacement ", summary: true) {
            fileset(dir: new File(directory, 'res/layout')) { include(name: '**/*.xml') }
        }
        ant.replaceregexp(flags: 'gm') {
            regexp(pattern: "<$tagName(\\s*)")
            substitution(expression: "<$replacement\\1")
            fileset(dir: new File(directory, 'res/layout')) { include(name: '**/*.xml') }
        }
        ant.replace(casesensitive: 'true', token: "<$tagName>",
                value: "<$replacement>", summary: true) {
            fileset(dir: new File(directory, 'res/layout')) { include(name: '**/*.xml') }
        }
    }

    private void replaceTagResourcesClosingTag(File directory, String tagName, String replacement) {
        l.debug("Replacing tag resources with Apphance for $tagName to $replacement")
        ant.replace(casesensitive: 'true', token: "</$tagName ",
                value: "</$replacement ", summary: true) {
            fileset(dir: new File(directory, 'res/layout')) { include(name: '**/*.xml') }
        }
        ant.replaceregexp(flags: 'gm') {
            regexp(pattern: "</$tagName(\\s*)")
            substitution(expression: "</$replacement\\1")
            fileset(dir: new File(directory, 'res/layout')) { include(name: '**/*.xml') }
        }
        ant.replace(casesensitive: 'true', token: "</$tagName>",
                value: "</$replacement>", summary: true) {
            fileset(dir: new File(directory, 'res/layout')) { include(name: '**/*.xml') }
        }
    }

    private void replaceActivityWithApphance(File directory, String activityName) {
        String newClassName = EVENT_LOG_ACTIVITY_PACKAGE + "." + activityName
        l.debug("Replacing extends with Apphance for: $activityName to $newClassName")
        ant.replace(casesensitive: 'true', token: "extends $activityName ",
                value: "extends $newClassName ", summary: true) {
            fileset(dir: new File(directory, 'src')) { include(name: '**/*.java') }
        }
    }

    private def addApphanceInit(File directory, File mainFile, boolean isActivity) {
        l.debug("Adding apphance init to file: $mainFile.absolutePath")
        File newMainClassFile = new File(directory, 'newMainClassFile.java')
        String startSession = """Apphance.startNewSession(this, "${appKey()}", ${apphanceMode()});"""
        if (logEvents()) {
            startSession = startSession + 'com.apphance.android.eventlog.EventLog.setInvertedIdMap(this);'
        }
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

    private String appKey() {
        project[APPLICATION_KEY.propertyName] as String
    }

    private String apphanceMode() {
        ((project.hasProperty(APPHANCE_MODE.propertyName) && project[APPHANCE_MODE.propertyName].equals('QA'))) ?
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
