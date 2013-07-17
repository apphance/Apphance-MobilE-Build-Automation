package com.apphance.flow.plugins.android.test.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidTestConfiguration
import com.apphance.flow.executor.ExecutableCommand
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import groovy.text.SimpleTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import javax.inject.Named

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_TEST

class PrepareRobotiumTask extends DefaultTask {

    static String NAME = 'prepareRobotium'
    String group = FLOW_TEST
    String description = 'Prepares file structure for Robotium test framework'

    @Inject CommandExecutor executor
    @Inject AndroidConfiguration conf
    @Inject AndroidTestConfiguration testConf
    @Inject AndroidManifestHelper manifestHelper
    @Inject
    @Named('executable.android') ExecutableCommand executableAndroid

    @TaskAction
    void prepareRobotium() {
        File path = new File(conf.rootDir.path, testConf.testDir.value.path)
        setUpAndroidRobotiumProject(path)
        replaceInstrumentationLibrary(path)
        addApphanceInstrumentation(path)
        addRobotiumLibrary(path)
        copyTemplateTestActivity(path)
    }

    private void setUpAndroidRobotiumProject(File path) {
        String[] command
        if (path.exists()) {
            logger.info("Robotium test directory exists, now I'm going to recreate the project (no source files are going to be touched)")
            command = executableAndroid.cmd + [
                    '-v',
                    'update',
                    'test-project',
                    '-p',
                    '.',
                    '-m',
                    '../..'
            ]
        } else {
            logger.info("No Robotium project detected, new one is going to be created")
            path.mkdirs()
            command = executableAndroid.cmd + [
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
        logger.info("Changing Android Manifest file: PolideaInstrumentationTestRunner will be in use")
        File manifest = new File(path.path, 'AndroidManifest.xml')
        String input = manifest.text.replace('android.test.InstrumentationTestRunner', 'pl.polidea.instrumentation.PolideaInstrumentationTestRunner');
        manifest.write(input)
    }

    private void addApphanceInstrumentation(File path) {
        logger.info("Downloading PolideaInstrumentationTestRunner library")
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
        logger.info("Downloading Robotium library")
        def libs = new File(path.path + '/libs/')
        libs.mkdirs()
        project.configurations.robotium.each {
            downloadFile(it.toURI().toURL(), new File(path.path + File.separator + 'libs' + File.separator + it.name))
        }
    }

    @groovy.transform.PackageScope
    void downloadFile(URL url, File file) {
        logger.info("Downloading file from ${url} to ${file}")
        def stream = new FileOutputStream(file)
        def out = new BufferedOutputStream(stream)
        out << url.openStream()
        out.close()
    }

    private void copyTemplateTestActivity(File path) {
        File srcDir = new File(path.path + '/src/' + conf.mainPackage.replace('.', File.separator))
        srcDir.mkdirs()

        new File(srcDir.path + "/TestActivityTest.java").delete()

        File baseCase = new File(srcDir.path + File.separator + 'BaseTestCase.java')
        URL baseCaseTemplate = this.class.getResource("BaseTestCase.java_")
        File helloCase = new File(srcDir.path + File.separator + 'TestHello.java')
        URL helloCaseTemplate = this.class.getResource("TestHello.java_")

        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        def binding = [packageName: conf.mainPackage, mainActivity: manifestHelper.getMainActivityName(conf.rootDir)]
        def baseCaseResult = engine.createTemplate(baseCaseTemplate).make(binding)
        def helloCaseResult = engine.createTemplate(helloCaseTemplate).make(binding)

        baseCase.write(baseCaseResult.toString())
        helloCase.write(helloCaseResult.toString())
    }
}
