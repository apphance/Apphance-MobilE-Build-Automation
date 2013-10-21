package com.apphance.flow.plugins.android.apphance.tasks

import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.apphance.ApphanceMode
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import com.apphance.flow.util.FlowUtils
import com.thoughtworks.qdox.JavaDocBuilder
import com.thoughtworks.qdox.model.JavaClass
import groovy.io.FileType
import groovy.transform.PackageScope
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.logging.Logging

import static android.Manifest.permission.*
import static com.apphance.flow.configuration.apphance.ApphanceArtifactory.ANDROID_APPHANCE_REPO
import static com.apphance.flow.configuration.apphance.ApphanceLibType.PRE_PROD
import static com.apphance.flow.configuration.apphance.ApphanceLibType.libForMode
import static com.apphance.flow.configuration.apphance.ApphanceMode.*
import static com.google.common.base.Preconditions.checkArgument
import static com.google.common.base.Preconditions.checkNotNull
import static org.apache.commons.io.FileUtils.copyURLToFile

@Mixin([FlowUtils, AndroidManifestHelper])
class AddApphanceToAndroid {

    String apphanceVersion
    public final String APPHANCE_PROD_URL
    public final String APPHANCE_PREPROD_URL
    def logger = Logging.getLogger(this.class)

    final File variantDir
    final String apphanceAppKey
    final ApphanceMode apphanceMode
    final boolean shakeEnabled
    final boolean uTestEnabled
    final String defaultUser
    final boolean screenshotFromGallery

    private static final String IMPORT_APPHANCE = 'com.apphance.android.Apphance'
    static final String IMPORT_CONFIGURATION = 'com.apphance.android.common.Configuration'
    private static final String ON_START = 'onStart'
    private static final String ON_STOP = 'onStop'
    private static final String APPHANCE_LIB_WITH_CONFIGURATION_BUILDER = '1.9.6'

    def androidHelper = new AndroidManifestHelper()

    AddApphanceToAndroid() {
    }

    AddApphanceToAndroid(File variantDir, String apphanceAppKey, ApphanceMode apphanceMode, String libVersion, reportOnShakeEnabled = false,
                         boolean uTestEnabled, String defaultUser, boolean screenshotFromGallery) {
        apphanceVersion = libVersion ?: '1.9'
        APPHANCE_PROD_URL = "${ANDROID_APPHANCE_REPO}apphance-prod/${apphanceVersion}/apphance-prod-${apphanceVersion}.jar"
        APPHANCE_PREPROD_URL = "${ANDROID_APPHANCE_REPO}apphance-preprod/${apphanceVersion}/apphance-preprod-${apphanceVersion}.zip"
        this.variantDir = variantDir
        this.apphanceAppKey = apphanceAppKey
        this.apphanceMode = apphanceMode
        this.shakeEnabled = reportOnShakeEnabled
        this.uTestEnabled = uTestEnabled
        this.screenshotFromGallery = screenshotFromGallery
        this.defaultUser = defaultUser

        checkArgument variantDir.exists()
        checkNotNull apphanceAppKey
        checkNotNull apphanceMode
        checkNotNull apphanceVersion
    }

    AddApphanceToAndroid(AndroidVariantConfiguration androidVariantConf) {
        this(androidVariantConf.tmpDir, androidVariantConf.aphAppKey.value, androidVariantConf.aphMode.value,
                androidVariantConf.aphLib.value, androidVariantConf.aphReportOnShake.value, androidVariantConf.aphWithUTest.value,
                androidVariantConf.aphDefaultUser.value, androidVariantConf.aphWithScreenShotsFromGallery.value)
    }

    public void addApphance() {
        logger.info "Adding apphance to ${variantDir?.absolutePath}"
        if (checkIfApphancePresent()) {
            logger.warn "Apphance was already added. Skipping adding apphance task."
            return
        }

        addStartNewSessionToAllMainActivities()
        addApphanceImportsAndStartStopMethodsInAllActivities()
        convertLogToApphance()
        addProblemActivityToManifest()
        addPermissions()
        addApphanceLib()
    }

    @PackageScope
    boolean checkIfApphancePresent() {
        def startNewSession = { File it -> it.name.endsWith('.java') && it.text.contains('Apphance.startNewSession') }
        def apphanceLib = { File it -> it.name ==~ /(.*apphance-library.*|apphance-prod.*jar)/ }
        def apphanceFiles = allFiles(dir: new File(variantDir, 'src'), where: { startNewSession(it) })
        apphanceFiles += allFiles(dir: new File(variantDir, 'libs'), where: { apphanceLib(it) })
        def foundApphanceActivity = isApphanceActivityPresent(variantDir)

        if (apphanceFiles) logger.info "Apphance was already added. Found following files: ${apphanceFiles*.absolutePath}"
        if (foundApphanceActivity) logger.info "Found Apphance activity"

        apphanceFiles || foundApphanceActivity
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

        sourceFiles.each { File file ->
            logger.info "Adding startNewSession invocation to ${file.name}"
            addStartNewSession(file)
        }
    }

    @PackageScope
    void addStartNewSession(File file) {
        if (libVerLowerThan(APPHANCE_LIB_WITH_CONFIGURATION_BUILDER)) {
            addApphanceInit(file, apphanceAppKey, apphanceMode)
        } else {
            addApphanceInitVer196(file, apphanceAppKey, apphanceMode, shakeEnabled, uTestEnabled, defaultUser, screenshotFromGallery)
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
        List<File> existingActivityFiles = activityFiles.findAll { it.exists() }
        existingActivityFiles.each {
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
                addCodeToMethod(file, methodName, "    Apphance.$methodName(this);")
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
    void convertLogToApphance() {
        new File(variantDir, 'src').traverse(type: FileType.FILES, nameFilter: ~/.*\.java/) { convertLogToApphanceInFile(it) }
    }

    private void convertLogToApphanceInFile(File file) {
        logger.info "Adding Apphance logger to ${file.absolutePath}"
        file.setText file.text.replaceAll(/\s*import\s*android.util.Log\s*;/, '\nimport com.apphance.android.Log;')
    }

    @PackageScope
    def addApphanceLib() {
        logger.info "Downloading apphance in mode: $apphanceMode"
        if (apphanceMode in [QA, SILENT]) {
            downloadZipAndUnzip APPHANCE_PREPROD_URL, new File("$variantDir.absolutePath/libs")
            addApphanceLibraryReferenceToProjectProperties()
        } else if (apphanceMode == PROD) {
            download APPHANCE_PROD_URL, new File("$variantDir.absolutePath/libs/apphance-prod-${apphanceVersion}.jar")
        }
    }

    void download(String url, File destFile) {
        copyURLToFile url.toURL(), destFile
    }

    void downloadZipAndUnzip(String url, File destDir) {
        unzip downloadToTempFile(url), destDir
    }

    @PackageScope
    void addApphanceLibraryReferenceToProjectProperties() {
        androidHelper.addLibrary(new File(variantDir, 'project.properties'), "libs/apphance-library-${apphanceVersion}")
    }

    def addApphanceInit(File mainFile, String apphanceAppKey, ApphanceMode apphanceMode) {
        logger.debug "Adding apphance init to file: $mainFile.absolutePath"
        String startSession = """Apphance.startNewSession(this, "$apphanceAppKey", ${mapApphanceMode(apphanceMode)});"""
        String shakeEnablingLine = (shakeEnabled && (apphanceMode in [QA, SILENT])) ? "Apphance.setReportOnShakeEnabled(true);" : ''
        logger.info "Shake enabled: $shakeEnabled. Apphance mode: $apphanceMode. Shake enabling line: $shakeEnablingLine"
        if (isMethodPresent(mainFile, 'onCreate')) {
            addCodeToMethod(mainFile, 'onCreate', "\n        $startSession\n        $shakeEnablingLine")
        } else {
            String body = "    public void onCreate(final Bundle savedInstanceState) {\n" +
                    "    super.onCreate(savedInstanceState);\n" +
                    "    $startSession\n" +
                    "    $shakeEnablingLine}\n"
            addMethodToClassFile(body, mainFile)
        }
    }

    def addApphanceInitVer196(File mainFile, String appKey, ApphanceMode apphanceMode, boolean reportOnShakeEnabled, boolean uTestEnabled,
                              String defaultUser = null, boolean screenshotFromGallery = false) {
        logger.info "Adding apphance init to file: $mainFile.absolutePath"
        logger.info "Using new Configuration builder"

        String body = """
                |com.apphance.android.common.Configuration configuration = new com.apphance.android.common.Configuration.Builder(this)
                |   .withAPIKey("$appKey")
                |   .withMode(${mapApphanceMode(apphanceMode)})
                |   .withUTestEnabled($uTestEnabled)
                |   .withReportOnShakeEnabled($reportOnShakeEnabled)
                |   .withScreenshotFromGalleryEnabled($screenshotFromGallery)
                |   ${defaultUser ? ".withDefaultUser(\"$defaultUser\")" : ''}
                |   .build();
                |
                |Apphance.startNewSession(this, configuration);
                |""".stripMargin()

        if (isMethodPresent(mainFile, 'onCreate')) {
            addCodeToMethod(mainFile, 'onCreate', body)
        } else {
            String method = """
                |public void onCreate(Bundle savedInstanceState) {
                |    super.onCreate(savedInstanceState);
                |    $body
                |}
                |
                |""".stripMargin()
            addMethodToClassFile(method, mainFile)
        }
    }

    @PackageScope
    boolean isMethodPresent(File file, String methodName) {
        file.readLines().any { it.matches(".*void.*$methodName\\(.*") }
    }

    @PackageScope
    String mapApphanceMode(ApphanceMode apphanceMode) {
        (apphanceMode == QA) ? 'Apphance.Mode.QA' : 'Apphance.Mode.Silent'
    }

    private void addCodeToMethod(File file, String methodName, String code) {
        File temp = tempFile

        boolean lineAdded = false
        boolean searchingForOpeningBrace = false
        temp.withWriter { out ->
            file.eachLine { line ->
                if (line.matches(".*void\\s$methodName\\(.*") && !lineAdded) {
                    searchingForOpeningBrace = true
                }
                if (!lineAdded && searchingForOpeningBrace && line.matches('.*\\{.*')) {
                    out.println(line.replaceAll('\\{', "{\n$code"))
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

    Boolean libVerLowerThan(String compare) {
        if (!(apphanceVersion ==~ /\d+(\.(\d+))*/ && compare ==~ /\d+(\.(\d+))*/)) return false

        def current = apphanceVersion.split(/\./).collect { it as Integer }
        def comp = compare.split(/\./).collect { it as Integer }
        def max = [current.size(), comp.size()].max()

        comp += [0] * (max - comp.size())
        current += [0] * (max - current.size())

        def diffIndex = (0..max).find { current[it] != comp[it] } as Integer
        diffIndex != null && (current[diffIndex] < comp[diffIndex])
    }

}
