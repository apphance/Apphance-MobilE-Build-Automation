package com.apphance.ameba.runBuilds.ios;

import static org.junit.Assert.*;

import org.gradle.tooling.BuildException
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.After
import org.junit.Before
import org.junit.Test

class RunVerifyErrorsSetupTest {
    File testIosProject = new File("testProjects/ios/GradleXCode")
    File gradleProperties = new File(testIosProject,"gradle.properties")
    File gradlePropertiesOrig = new File(testIosProject,"gradle.properties.orig")

    @Before
    void before() {
        gradlePropertiesOrig.delete()
        gradlePropertiesOrig << gradleProperties.text
    }


    @After
    void after() {
        gradleProperties.delete()
        gradleProperties << gradlePropertiesOrig.text
    }

    String runTests(String ... tasks) {
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(testIosProject).connect();
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream()
            BuildLauncher bl = connection.newBuild().forTasks(tasks);
            bl.setStandardOutput(os)
            bl.run();
            def res = os.toString("UTF-8")
            println res
            assertFalse(res.contains('BUILD SUCCESSFUL'))
            return res
        } finally {
            connection.close();
        }
    }
    public void runErrorScenario(pattern, String replacement, String expected){
        gradleProperties.delete()
        String newText = gradlePropertiesOrig.text.split('\n')*.
                replaceFirst(pattern,replacement).join('\n')
        println newText
        gradleProperties << newText
        try {
            runTests('verifySetup')
        } catch (BuildException e) {
            println e.cause.cause.cause.message
            assertTrue(e.cause.cause.cause.message.contains(expected))
        }
    }

    @Test
    void testProjectIconFile() {
        runErrorScenario(/^(project\.icon\.file.*)=(.*)$/,'$1=/notexistingfile.txt', 'The icon file')
    }

    @Test
    void testUrl() {
        runErrorScenario(/^(project\.url\.base.*)=(.*)$/,'$1=notaurl', 'java.net.MalformedURLException')
    }

    @Test
    void testLongLanguage() {
        runErrorScenario(/^(project\.language.*)=(.*)$/,'$1=longlanguage', 'language')
    }
    @Test
    void testNotLowercaseLanguage() {
        runErrorScenario(/^(project\.language.*)=(.*)$/,'$1=La', 'language')
    }

    @Test
    void testNoUppercaseCountry() {
        runErrorScenario(/^(project\.country.*)=(.*)$/,'$1=No', 'country')
    }

    @Test
    void testLongCountry() {
        runErrorScenario(/^(project\.country.*)=(.*)$/,'$1=longcountry', 'country')
    }

    @Test
    void testIOSPList() {
        runErrorScenario(/^(ios\.plist\.file.*)=(.*)$/,'$1=missing', 'The plist file')
    }

    @Test
    void testIOSFamilies() {
        runErrorScenario(/^(ios\.families.*)=(.*)$/,'$1=ble', 'The family')
    }

    @Test
    void testDistributionDir() {
        runErrorScenario(/^(ios\.distribution\.resources\.dir.*)=(.*)$/,'$1=missingDirectory', 'The distribution resources directory')
    }
    @Test
    void testTarget() {
        runErrorScenario(/^(ios\.mainTarget.*)=(.*)$/,'$1=missingTarget', 'main target')
    }
    @Test
    void testConfiguration() {
        runErrorScenario(/^(ios\.mainConfiguration.*)=(.*)$/,'$1=missingConfiguration', 'main configuration')
    }
    @Test
    void testIOSSdk() {
        runErrorScenario(/^(ios\.sdk.*)=(.*)$/,'$1=missingsdk', 'iPhone sdk')
    }

    @Test
    void testIOSSimulatorSdk() {
        runErrorScenario(/^(ios\.simulator\.sdk.*)=(.*)$/,'$1=missingsdk', 'iPhone simulator sdk')
    }

    @Test
    void testFoneMonkeyConfiguration() {
        runErrorScenario(/^(ios\.fonemonkey\.configuration.*)=(.*)$/,'$1=missingconfiguration', 'fonemonkey configuration')
    }
    @Test
    void testFrameworkConfiguration() {
        runErrorScenario(/^(ios\.framework\.configuration.*)=(.*)$/,'$1=missingconfiguration', 'framework configuration')
    }
    @Test
    void testFrameworkTarget() {
        runErrorScenario(/^(ios\.framework\.target.*)=(.*)$/,'$1=missingtarget', 'framework target')
    }

    @Test
    void testFrameworkResources() {
        runErrorScenario(/^(ios\.framework\.resources.*)=(.*)$/,'$1=build.gradle,missing_resource', 'The file is missing')
    }

    @Test
    void testFrameworkHeaders() {
        runErrorScenario(/^(ios\.framework\.headers.*)=(.*)$/,'$1=build.gradle,missing_header.h', 'The file is missing')
    }
}
