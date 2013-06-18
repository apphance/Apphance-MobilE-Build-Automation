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

    def logger = Logging.getLogger(this.class)

    final File variantDir
    final String apphanceAppKey
    final ApphanceMode apphanceMode

    private static final String ON_START = 'onStart'
    private static final String ON_STOP = 'onStop'

    AddApphanceToAndroid(AndroidVariantConfiguration androidVariantConf) {
        variantDir = androidVariantConf.variantDir.value
        apphanceAppKey = androidVariantConf.apphanceAppKey.value
        apphanceMode = androidVariantConf.apphanceMode.value
        checkArgument variantDir.exists()
        checkNotNull apphanceAppKey
        checkNotNull apphanceMode
    }

    public void addApphance() {
        if (checkIfApphancePresent()) throw new GradleException("Apphance was already added")

        addReportActivityToManifest()
        addPermisions()
        addStartNewSessionToAllMainActivities()
        addApphanceToOnStartAndOnStopMethodsInAllActivities()
    }

    @PackageScope
    boolean checkIfApphancePresent() {
        def startNewSession = { File it -> it.name.endsWith('.java') && it.text.contains('Apphance.startNewSession') }
        def apphanceLib = { File it -> it.name == 'apphance-library.jar' }

        allFiles(dir: variantDir, where: { startNewSession(it) || apphanceLib(it) }) || isApphanceActivityPresent(variantDir)
    }

    @PackageScope
    void addReportActivityToManifest() {
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
            onCreate.setSourceCode(onCreate.sourceCode + '\nApphance.startNewSession(this, APP_KEY, Mode.QA);\n')

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
    void addApphanceToOnStartAndOnStopMethodsInAllActivities() {
        Set<String> activityNames = getActivities(getManifest(variantDir))
        List<File> activityFiles = getSourcesOf(variantDir, activityNames)
        activityFiles.each { addStartStopInvocations(it) }
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
}
