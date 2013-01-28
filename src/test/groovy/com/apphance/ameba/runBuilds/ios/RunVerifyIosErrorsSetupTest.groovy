package com.apphance.ameba.runBuilds.ios

import com.apphance.ameba.ProjectHelper
import org.gradle.tooling.BuildException
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.*

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class RunVerifyIosErrorsSetupTest {
    static File testIosProject = new File("testProjects/ios/GradleXCode")
    static File gradleProperties = new File(testIosProject, "gradle.properties")
    static File gradlePropertiesOrig = new File(testIosProject, "gradle.properties.orig")
    static ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(testIosProject).connect();

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

    @BeforeClass
    static void beforeClass() {
        connection = GradleConnector.newConnector().forProjectDirectory(testIosProject).connect()
    }

    @AfterClass
    static public void afterClass() {
        connection.close()
    }

    String runTests(String... tasks) {
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        BuildLauncher bl = connection.newBuild().forTasks(tasks);
        bl.setStandardOutput(os)
        bl.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
        bl.run();
        def res = os.toString("UTF-8")
        println res
        assertFalse(res.contains('BUILD SUCCESSFUL'))
        return res
    }

    public void runErrorScenario(pattern, String replacement, String expected) {
        gradleProperties.delete()
        String newText = gradlePropertiesOrig.text.split('\n')*.
                replaceFirst(pattern, replacement).join('\n')
        println newText
        gradleProperties << newText
        // HACK ... Run it up to two times just to avoid some random gradle daemon problems
        String message
        int count = 2
        while (--count > 0) {
            try {
                runTests('verifySetup')
            } catch (BuildException e) {
                message = e.cause.cause.cause.message
                println message
                if (message.contains(expected)) {
                    break
                }
            }
        }
        assertTrue(message.contains(expected))
    }

    @Test
    void testProjectIconFile() {
        runErrorScenario(/^(release\.project\.icon\.file.*)=(.*)$/, '$1=/notexistingfile.txt', 'The icon file')
    }

    @Test
    void testUrl() {
        runErrorScenario(/^(release\.project\.url.*)=(.*)$/, '$1=notaurl', 'protocol')
    }

    @Test
    void testLongLanguage() {
        runErrorScenario(/^(release\.project\.language.*)=(.*)$/, '$1=longlanguage', 'language')
    }

    @Test
    void testNotLowercaseLanguage() {
        runErrorScenario(/^(release\.project\.language.*)=(.*)$/, '$1=La', 'language')
    }

    @Test
    void testNoUppercaseCountry() {
        runErrorScenario(/^(release\.project\.country.*)=(.*)$/, '$1=No', 'country')
    }

    @Test
    void testLongCountry() {
        runErrorScenario(/^(release\.project\.country.*)=(.*)$/, '$1=longcountry', 'country')
    }

    @Test
    void testIOSPList() {
        runErrorScenario(/^(ios\.plist\.file.*)=(.*)$/, '$1=missing', 'The plist file')
    }

    @Test
    void testIOSFamilies() {
        runErrorScenario(/^(ios\.families.*)=(.*)$/, '$1=ble', 'The family')
    }

    @Test
    void testDistributionDir() {
        runErrorScenario(/^(ios\.distribution\.resources\.dir.*)=(.*)$/, '$1=missingDirectory', 'The distribution resources directory')
    }

    @Test
    void testTarget() {
        runErrorScenario(/^(ios\.mainTarget.*)=(.*)$/, '$1=missingTarget', 'target')
    }

    @Test
    void testConfiguration() {
        runErrorScenario(/^(ios\.mainConfiguration.*)=(.*)$/, '$1=missingConfiguration', 'configuration')
    }

    @Test
    void testIOSSdk() {
        runErrorScenario(/^(ios\.sdk.*)=(.*)$/, '$1=missingsdk', 'iPhone sdk')
    }

    @Test
    void testIOSSimulatorSdk() {
        runErrorScenario(/^(ios\.simulator\.sdk.*)=(.*)$/, '$1=missingsdk', 'iPhone simulator sdk')
    }

    @Test
    void testFoneMonkeyConfiguration() {
        runErrorScenario(/^(ios\.fonemonkey\.configuration.*)=(.*)$/, '$1=missingconfiguration', 'configuration')
    }

    @Test
    void testFrameworkConfiguration() {
        runErrorScenario(/^(ios\.framework\.configuration.*)=(.*)$/, '$1=missingconfiguration', 'configuration')
    }

    @Test
    void testFrameworkTarget() {
        runErrorScenario(/^(ios\.framework\.target.*)=(.*)$/, '$1=missingtarget', 'target')
    }

    @Test
    void testFrameworkResources() {
        runErrorScenario(/^(ios\.framework\.resources.*)=(.*)$/, '$1=build.gradle,missing_resource', 'The file is missing')
    }

    @Test
    void testFrameworkHeaders() {
        runErrorScenario(/^(ios\.framework\.headers.*)=(.*)$/, '$1=build.gradle,missing_header.h', 'The file is missing')
    }

    @Test
    void testMailFrom() {
        runErrorScenario(/^(release\.mail\.from.*)=(.*)$/, '$1=wrongmail@test', 'The email in')
    }

    @Test
    void testMailTo() {
        runErrorScenario(/^(release\.mail\.to.*)=(.*)$/, '$1=wrongmail@test', 'The email in')
    }

    @Test
    void testMailFlags() {
        runErrorScenario(/^(release\.mail\.flags.*)=(.*)$/, '$1=qrCode,zzzz', 'flags')
    }
}
