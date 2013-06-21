package com.apphance.flow.plugins.android.apphance.tasks

import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.apphance.ApphanceMode
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import com.apphance.flow.util.FlowUtils
import com.thoughtworks.qdox.JavaDocBuilder
import com.thoughtworks.qdox.model.JavaClass
import com.thoughtworks.qdox.model.JavaField
import com.thoughtworks.qdox.model.JavaMethod
import com.thoughtworks.qdox.model.Type
import groovy.transform.PackageScope
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.GradleException
import org.gradle.api.logging.Logging

import static android.Manifest.permission.*
import static com.google.common.base.Preconditions.checkArgument
import static com.google.common.base.Preconditions.checkNotNull
import static com.thoughtworks.qdox.model.Type.VOID

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

    AddApphanceToAndroid(AndroidVariantConfiguration androidVariantConf) {
        variantDir = androidVariantConf.tmpDir
        apphanceAppKey = androidVariantConf.apphanceAppKey.value
        apphanceVersion = androidVariantConf.apphanceLibVersion.value
        apphanceMode = androidVariantConf.apphanceMode.value
        ARTIFACTORY_URL = "https://dev.polidea.pl/artifactory/libs-releases-local/com/apphance/android.pre-production/1.9-RC1/android" +
                ".pre-production-${apphanceVersion}.zip"
        checkArgument variantDir.exists()
        checkNotNull apphanceAppKey
        checkNotNull apphanceMode
        checkNotNull apphanceVersion
    }

    public void addApphance() {
        logger.info "Adding apphance to ${variantDir?.absolutePath}"
        if (checkIfApphancePresent()) throw new GradleException("Apphance was already added")

        addStartNewSessionToAllMainActivities()
        addApphanceImportsAndStartStopMethodsInAllActivities()
        addProblemActivityToManifest()
        addPermisions()
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
        withManifest(variantDir) { GPathResult manifest ->
            manifest.application.appendNode {
                activity 'android:name': 'com.apphance.android.ui.ProblemActivity',
                        'android:configChanges': 'orientation',
                        'android:launchMode': 'singleInstance',
                        'android:process': 'com.utest.apphance.reporteditor'
            }
        }
    }

    @PackageScope
    void addPermisions() {
        withManifest(variantDir) { GPathResult manifest ->
            addPermissionsToManifest manifest, INTERNET, READ_PHONE_STATE, GET_TASKS, ACCESS_WIFI_STATE, ACCESS_NETWORK_STATE, ACCESS_COARSE_LOCATION,
                    ACCESS_FINE_LOCATION, BLUETOOTH
        }
    }

    @PackageScope
    void addStartNewSessionToAllMainActivities() {
        Collection<String> classes = getMainActivitiesFromProject(variantDir)
        List<File> sourceFiles = getSourcesOf(variantDir, classes)

        sourceFiles.each {
            logger.info "Adding startNewSession invocation to ${it.name}"
            JavaClass activity = getActivity(it)

            // public static final String APP_KEY = "apphanceAppKey";
            JavaField appKey = new JavaField(new Type('String'), 'APP_KEY')
            appKey.setModifiers 'public', 'static', 'final'
            appKey.setInitializationExpression "\"$apphanceAppKey\""
            activity.addField appKey

            // Apphance.startNewSession(this, APP_KEY, Mode.QA);
            def onCreate = activity.getMethodBySignature('onCreate', [new Type('android.os.Bundle')] as Type[])
            assert onCreate
            onCreate.setSourceCode(onCreate.sourceCode + '\nApphance.startNewSession(this, APP_KEY, Apphance.Mode.QA);\n')

            it.setText activity.source.toString()
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

        JavaClass activity = getActivity(file)
        [ON_START, ON_STOP].each { String methodName ->
            def method = activity.getMethodBySignature(methodName)
            String apphanceInvocation = "Apphance.${methodName}(this);\n"
            if (method) {
                method.sourceCode += apphanceInvocation
            } else {
                method = new JavaMethod(VOID, methodName)
                method.sourceCode = "super.$methodName();\n" + apphanceInvocation
                method.setModifiers(['protected'] as String[])
                activity.addMethod method
            }
        }
        file.setText activity.source.toString()
    }

    @PackageScope
    void addApphanceImportTo(File file) {
        logger.info "Adding Apphance import to ${file.name}"

        JavaClass activity = getActivity(file)
        if (!(IMPORT_APPHANCE in activity.getSource().imports)) {
            activity.getSource().addImport IMPORT_APPHANCE
        }
        file.setText activity.source.toString()
    }

    @PackageScope
    void convertLogToApphance(File file) {
        logger.info "Adding Apphance logger to ${file.name}"

        file.setText file.text.replaceAll(/\s*import\s*android.util.Log\s*;/, '\nimport com.apphance.android.Log;')
    }

    @PackageScope
    def addApphanceLib() {
        unzip downloadToTempFile(ARTIFACTORY_URL), new File("$variantDir.absolutePath/libs")
    }

    @PackageScope
    void addApphanceLibraryReferenceToProjectProperties() {
        File projectProperties = new File(variantDir, 'project.properties')
        assert projectProperties.exists()
        def libSize = projectProperties.readLines().findAll {it.trim().startsWith('android.library.reference.')}.size()
        projectProperties << "android.library.reference.${libSize + 1}=libs/apphance-library-${apphanceVersion}"
    }
}
