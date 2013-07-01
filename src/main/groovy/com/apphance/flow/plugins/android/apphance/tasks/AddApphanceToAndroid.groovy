package com.apphance.flow.plugins.android.apphance.tasks

import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.apphance.ApphanceMode
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import com.apphance.flow.util.FlowUtils
import com.thoughtworks.qdox.JavaDocBuilder
import com.thoughtworks.qdox.model.JavaClass
import groovy.transform.PackageScope
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.GradleException
import org.gradle.api.logging.Logging

import static android.Manifest.permission.*
import static com.apphance.flow.configuration.apphance.ApphanceLibType.PRE_PROD
import static com.apphance.flow.configuration.apphance.ApphanceLibType.libForMode
import static com.google.common.base.Preconditions.checkArgument
import static com.google.common.base.Preconditions.checkNotNull

@Mixin([FlowUtils, AndroidManifestHelper])
class AddApphanceToAndroid {

    String apphanceVersion
    public final String ARTIFACTORY_URL
    def logger = Logging.getLogger(this.class)

    final File variantDir
    final String apphanceAppKey
    final ApphanceMode apphanceMode

    private static final String IMPORT_APPHANCE = 'com.apphance.android.Apphance'
    private static final String ON_START = 'onStart'
    private static final String ON_STOP = 'onStop'

    AddApphanceToAndroid() {
    }

    AddApphanceToAndroid(File variantDir, String apphanceAppKey, ApphanceMode apphanceMode, String libVersion) {
        apphanceVersion = libVersion ?: '1.9-RC1'
        ARTIFACTORY_URL = "https://dev.polidea.pl/artifactory/libs-releases-local/com/apphance/android.pre-production/${apphanceVersion}/android" +
                ".pre-production-${apphanceVersion}.zip"
        this.variantDir = variantDir
        this.apphanceAppKey = apphanceAppKey
        this.apphanceMode = apphanceMode

        checkArgument variantDir.exists()
        checkNotNull apphanceAppKey
        checkNotNull apphanceMode
        checkNotNull apphanceVersion
    }

    AddApphanceToAndroid(AndroidVariantConfiguration androidVariantConf) {
        this(androidVariantConf.tmpDir, androidVariantConf.apphanceAppKey.value, androidVariantConf.apphanceMode.value,
                androidVariantConf.apphanceLibVersion.value)
    }

    public void addApphance() {
        logger.info "Adding apphance to ${variantDir?.absolutePath}"
        if (checkIfApphancePresent()) throw new GradleException("Apphance was already added")

        addStartNewSessionToAllMainActivities()
        addApphanceImportsAndStartStopMethodsInAllActivities()
        addProblemActivityToManifest()
        addPermissions()
        addApphanceLib()
        addApphanceLibraryReferenceToProjectProperties()
    }

    @PackageScope
    boolean checkIfApphancePresent() {
        def startNewSession = { File it -> it.name.endsWith('.java') && it.text.contains('Apphance.startNewSession') }
        def apphanceLib = { File it -> it.name ==~ /.*apphance-library.*/ }

        allFiles(dir: variantDir, where: { startNewSession(it) || apphanceLib(it) }) || isApphanceActivityPresent(variantDir)
    }

    @PackageScope
    void addProblemActivityToManifest() {
        if (libForMode(apphanceMode) == PRE_PROD) {
            withManifest(variantDir) { GPathResult manifest ->
                manifest.application.appendNode {
                    activity 'android:name': 'com.apphance.android.ui.ProblemActivity',
                            'android:configChanges': 'orientation',
                            'android:launchMode': 'singleInstance',
                            'android:process': 'com.utest.apphance.reporteditor'
                }
            }
        } else {
            logger.info("Problem activity not added in apphance production mode")
        }
    }

    @PackageScope
    void addPermissions() {
        withManifest(variantDir) { GPathResult manifest ->
            addPermissionsToManifest manifest, INTERNET, ACCESS_WIFI_STATE, ACCESS_NETWORK_STATE, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, BLUETOOTH

            if (libForMode(apphanceMode) == PRE_PROD) {
                addPermissionsToManifest manifest, READ_PHONE_STATE, GET_TASKS
            }
        }
    }

    @PackageScope
    void addStartNewSessionToAllMainActivities() {
        Collection<String> classes = getMainActivitiesFromProject(variantDir)
        List<File> sourceFiles = getSourcesOf(variantDir, classes)

        sourceFiles.each {
            logger.info "Adding startNewSession invocation to ${it.name}"

            addApphanceInit(it, apphanceAppKey, apphanceMode)
        }
    }

    @PackageScope
    JavaClass getActivity(File activityFile) {
        JavaDocBuilder builder = new JavaDocBuilder()
        builder.addSource(activityFile)
        JavaClass activity = builder.getClasses().find { 'public' in it.modifiers }
        assert activity
        activity
    }

    @PackageScope
    void addApphanceImportsAndStartStopMethodsInAllActivities() {
        Set<String> activityNames = getActivities(getManifest(variantDir))
        List<File> activityFiles = getSourcesOf(variantDir, activityNames)
        activityFiles.each {
            convertLogToApphance(it)
            addApphanceImportTo(it)
            addStartStopInvocations(it)
        }
    }

    @PackageScope
    void addStartStopInvocations(File file) {
        logger.info "Adding onStart and onStop invocation to ${file.name}"

        [ON_START, ON_STOP].each { String methodName ->
            if (!isMethodPresent(file, methodName)) {
                String methodBody = """
                    |    @Override
                    |    protected void $methodName() {
                    |        Apphance.$methodName(this);
                    |        super.$methodName();
                    |    }""".stripMargin()
                addMethodToClassFile(methodBody, file)
            } else {
                addLineOfCodeToMethod(file, methodName, "    Apphance.$methodName(this);")
            }
        }
    }

    @PackageScope
    void addApphanceImportTo(File file) {
        logger.info "Adding Apphance import to ${file.name}"

        JavaClass activity = getActivity(file)
        if (!(IMPORT_APPHANCE in activity.getSource().imports)) {
            List<String> lines = file.text.readLines()
            int firstImportLine = lines.findIndexOf { it.trim().startsWith('import') }
            assert firstImportLine >= 0
            lines.add(firstImportLine, "import $IMPORT_APPHANCE;")

            file.setText lines.join('\n')
        }
    }

    @PackageScope
    void convertLogToApphance(File file) {
        logger.info "Adding Apphance logger to ${file.name}"

        file.setText file.text.replaceAll(/\s*import\s*android.util.Log\s*;/, '\nimport com.apphance.android.Log;')
    }

    @PackageScope
    def addApphanceLib() {
        logger.info('Downloading apphance')
        unzip downloadToTempFile(ARTIFACTORY_URL), new File("$variantDir.absolutePath/libs")
    }

    @PackageScope
    void addApphanceLibraryReferenceToProjectProperties() {
        File projectProperties = new File(variantDir, 'project.properties')
        assert projectProperties.exists()
        def libSize = projectProperties.readLines().findAll { it.trim().startsWith('android.library.reference.') }.size()
        projectProperties << "android.library.reference.${libSize + 1}=libs/apphance-library-${apphanceVersion}"
    }

    def addApphanceInit(File mainFile, String apphanceAppKey, ApphanceMode apphanceMode) {
        logger.debug("Adding apphance init to file: $mainFile.absolutePath")
        String startSession = """    Apphance.startNewSession(this, "$apphanceAppKey", ${mapApphanceMode(apphanceMode)});"""
        if (isMethodPresent(mainFile, 'onCreate')) {
            addLineOfCodeToMethod(mainFile, 'onCreate', startSession)
        } else {
            String body = "    public void onCreate(final Bundle savedInstanceState) {\n    super.onCreate(savedInstanceState);\n    $startSession\n}\n"
            addMethodToClassFile(body, mainFile)
        }
    }

    @PackageScope
    boolean isMethodPresent(File file, String methodName) {
        file.readLines().any { it.matches(".*void.*$methodName\\(.*") }
    }

    private String mapApphanceMode(ApphanceMode apphanceMode) {
        (apphanceMode == ApphanceMode.QA) ? 'Apphance.Mode.QA' : 'Apphance.Mode.Silent'
    }

    private void addLineOfCodeToMethod(File file, String methodName, String lineOfCode) {
        File temp = tempFile

        boolean lineAdded = false
        boolean searchingForOpeningBrace = false
        temp.withWriter { out ->
            file.eachLine { line ->
                if (line.matches(".*void\\s$methodName\\(.*") && !lineAdded) {
                    searchingForOpeningBrace = true
                }
                if (!lineAdded && searchingForOpeningBrace && line.matches('.*\\{.*')) {
                    out.println(line.replaceAll('\\{', "{\n$lineOfCode"))
                    lineAdded = true
                } else {
                    out.println(line)
                }
            }
        }
        if (!lineAdded) {
            logger.warn("Could not find $methodName(). Apphance not added.")
        }

        file.delete()
        file << temp.text
        temp.delete()
    }

    private void addMethodToClassFile(String methodBody, File file) {
        File temp = tempFile

        boolean onCreateAdded = false
        boolean searchingForOpeningBrace = false
        temp.withWriter { out ->
            file.eachLine { line ->
                out.println(line)

                if (line.matches('.*class.*extends.*') && !onCreateAdded) {
                    searchingForOpeningBrace = true
                }

                if (!onCreateAdded && searchingForOpeningBrace && line.matches('.*\\{.*')) {
                    out.println(methodBody)
                    onCreateAdded = true
                }
            }
        }

        if (!onCreateAdded) {
            logger.warn("Method was not added")
        }

        file.delete()
        file << temp.text
        temp.delete()
    }
}
