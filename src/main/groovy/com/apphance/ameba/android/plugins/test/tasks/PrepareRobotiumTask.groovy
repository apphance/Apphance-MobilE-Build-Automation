package com.apphance.ameba.android.plugins.test.tasks

import com.apphance.ameba.android.AndroidManifestHelper
import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.android.plugins.test.AndroidTestConfiguration
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import groovy.text.SimpleTemplateEngine
import org.gradle.api.Project

import static com.apphance.ameba.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration
import static com.apphance.ameba.android.plugins.test.AndroidTestConfigurationRetriever.getAndroidTestConfiguration
import static org.gradle.api.logging.Logging.getLogger

class PrepareRobotiumTask {

    private l = getLogger(getClass())

    private String robotiumPath = 'test/android'
    private Project project
    private CommandExecutor executor
    private AndroidProjectConfiguration androidConf
    private AndroidTestConfiguration androidTestConf
    private AndroidManifestHelper manifestHelper = new AndroidManifestHelper()

    PrepareRobotiumTask(Project project, CommandExecutor executor) {
        this.executor = executor
        this.project = project
        this.androidConf = getAndroidProjectConfiguration(project)
        this.androidTestConf = getAndroidTestConfiguration(project)
    }

    void prepareRobotium() {
        File path = new File(project.rootDir.path, robotiumPath)
        setUpAndroidRobotiumProject(path)
        replaceInstrumentationLibrary(path)
        addApphanceInstrumentation(path)
        addRobotiumLibrary(path)
        // TODO: copy template test activities
        copyTemplateTestActivity(path)
    }

    private void setUpAndroidRobotiumProject(File path) {
        String[] command
        if (path.exists()) {
            println "Robotium test directory exists, now I'm going to recreate the project (no source files are going to be touched)"
            command = [
                    androidTestConf.androidBinary,
                    '-v',
                    'update',
                    'test-project',
                    '-p',
                    '.',
                    '-m',
                    '../..'
            ]
        } else {
            println "No Robotium project detected, new one is going to be created"
            path.mkdirs()
            command = [
                    androidTestConf.androidBinary,
                    '-v',
                    'create',
                    'test-project',
                    '-p',
                    '.',
                    '-m',
                    '../..',
                    '-n',
                    'test'
            ]
        }
        executor.executeCommand(new Command(runDir: path, cmd: command))
    }

    private void replaceInstrumentationLibrary(File path) {
        println "Changing Android Manifest file: PolideaInstrumentationTestRunner will be in use"
        File manifest = new File(path.path, 'AndroidManifest.xml')
        String input = manifest.text.replace('android.test.InstrumentationTestRunner', 'pl.polidea.instrumentation.PolideaInstrumentationTestRunner');
        manifest.write(input)
    }

    private void addApphanceInstrumentation(File path) {
        println "Downloading PolideaInstrumentationTestRunner library"
        def libs = new File(path.path + '/libs/')
        libs.mkdirs()
        copyFromResources(libs, 'the-missing-android-xml-junit-test-runner-release-1.3_2.jar');
    }

    private void copyFromResources(File path, String fileName) {
        FileOutputStream output = new FileOutputStream(path.path + File.separator + fileName)
        InputStream stream = this.class.getResource(fileName + '_').openStream();

        byte[] buffer = new byte[256];

        while (true) {
            def bytesRead = stream.read(buffer)
            if (bytesRead == -1)
                break;
            output.write(buffer, 0, bytesRead)
        }
    }

    private void addRobotiumLibrary(File path) {
        println "Downloading Robotium library"
        def libs = new File(path.path + '/libs/')
        libs.mkdirs()
        project.configurations.robotium.each {
            downloadFile(it.toURI().toURL(), new File(path.path + File.separator + 'libs' + File.separator + it.name))
        }
    }

    private void downloadFile(URL url, File file) {
        l.info("Downloading file from ${url} to ${file}")
        def stream = new FileOutputStream(file)
        def out = new BufferedOutputStream(stream)
        out << url.openStream()
        out.close()
    }

    private void copyTemplateTestActivity(File path) {
        println "Coping template Robotium test Activity class"
        File srcDir = new File(path.path + '/src/' + androidConf.mainProjectPackage.replace('.', File.separator))
        srcDir.mkdirs()

        // Delete the default test class
        new File(srcDir.path + "/TestActivityTest.java").delete()

        File baseCase = new File(srcDir.path + File.separator + 'BaseTestCase.java')
        URL baseCaseTemplate = this.class.getResource("BaseTestCase.java_")
        File helloCase = new File(srcDir.path + File.separator + 'TestHello.java')
        URL helloCaseTemplate = this.class.getResource("TestHello.java_")


        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        def binding = [packageName: androidConf.mainProjectPackage, mainActivity: manifestHelper.getMainActivityName(project.rootDir)]
        def baseCaseResult = engine.createTemplate(baseCaseTemplate).make(binding)
        def helloCaseResult = engine.createTemplate(helloCaseTemplate).make(binding)

        baseCase.write(baseCaseResult.toString())
        helloCase.write(helloCaseResult.toString())

    }
}
