package com.apphance.ameba.plugins.android.test.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.android.parsers.AndroidManifestHelper
import groovy.text.SimpleTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_TEST
import static org.gradle.api.logging.Logging.getLogger

class PrepareRobotiumTask extends DefaultTask {

    private l = getLogger(getClass())

    static String NAME = 'prepareRobotium'
    String group = AMEBA_TEST
    String description = 'Prepares file structure for Robotium test framework'

    private String robotiumPath = 'test/android'

    @Inject CommandExecutor executor
    @Inject AndroidConfiguration conf
    @Inject AndroidManifestHelper manifestHelper

    @TaskAction
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
            l.info("Robotium test directory exists, now I'm going to recreate the project (no source files are going to be touched)")
            command = [
                    'android',
                    '-v',
                    'update',
                    'test-project',
                    '-p',
                    '.',
                    '-m',
                    '../..'
            ]
        } else {
            l.info("No Robotium project detected, new one is going to be created")
            path.mkdirs()
            command = [
                    'android',
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
        l.info("Changing Android Manifest file: PolideaInstrumentationTestRunner will be in use")
        File manifest = new File(path.path, 'AndroidManifest.xml')
        String input = manifest.text.replace('android.test.InstrumentationTestRunner', 'pl.polidea.instrumentation.PolideaInstrumentationTestRunner');
        manifest.write(input)
    }

    private void addApphanceInstrumentation(File path) {
        l.info("Downloading PolideaInstrumentationTestRunner library")
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
        l.info("Downloading Robotium library")
        def libs = new File(path.path + '/libs/')
        libs.mkdirs()
        project.configurations.robotium.each {
            downloadFile(it.toURI().toURL(), new File(path.path + File.separator + 'libs' + File.separator + it.name))
        }
    }

    @groovy.transform.PackageScope
    void downloadFile(URL url, File file) {
        l.info("Downloading file from ${url} to ${file}")
        def stream = new FileOutputStream(file)
        def out = new BufferedOutputStream(stream)
        out << url.openStream()
        out.close()
    }

    private void copyTemplateTestActivity(File path) {
        File srcDir = new File(path.path + '/src/' + conf.mainPackage.replace('.', File.separator))
        srcDir.mkdirs()

        // Delete the default test class
        new File(srcDir.path + "/TestActivityTest.java").delete()

        File baseCase = new File(srcDir.path + File.separator + 'BaseTestCase.java')
        URL baseCaseTemplate = this.class.getResource("BaseTestCase.java_")
        File helloCase = new File(srcDir.path + File.separator + 'TestHello.java')
        URL helloCaseTemplate = this.class.getResource("TestHello.java_")

        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        def binding = [packageName: conf.mainPackage, mainActivity: manifestHelper.getMainActivityName(project.rootDir)]
        def baseCaseResult = engine.createTemplate(baseCaseTemplate).make(binding)
        def helloCaseResult = engine.createTemplate(helloCaseTemplate).make(binding)

        baseCase.write(baseCaseResult.toString())
        helloCase.write(helloCaseResult.toString())
    }
}
