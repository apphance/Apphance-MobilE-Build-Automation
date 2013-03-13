package com.apphance.ameba.util.file

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Specification

import static com.apphance.ameba.util.file.FileManager.findAllPackages
import static com.apphance.ameba.util.file.FileManager.removeMissingSymlinks
import static org.junit.Assert.assertTrue

class FileManagerIntegrationSpec extends Specification {

    def 'removes symlinks'() {
        given:
        def dir = new File('.')
        and:
        def missingFileLink = 'missingFileLink'

        when:
        [
                'ln',
                '-s',
                'missingFile',
                missingFileLink,
        ].execute([], dir).waitFor()

        then:
        dir.list().contains(missingFileLink)
        !(new File(dir, missingFileLink).canonicalFile.exists())

        when:
        removeMissingSymlinks(dir)

        then:
        !(dir.list().contains(missingFileLink))
        !(new File(dir, missingFileLink).canonicalFile.exists())
    }

    //TODO enable when tests refactor finished
    @Ignore('this tests is ignored because the package structure of test dir is still under construction')
    def 'finds all packages correctly'() {

        given:
        def f = new File('src/test/groovy')
        def currentPackageList = []

        and:
        def packageList = [
                'com.apphance.ameba.integration.android',
                'com.apphance.ameba.integration.android.apphance',
                'com.apphance.ameba.integration.android.robolectric',
                'com.apphance.ameba.integration.android.robotium',
                'com.apphance.ameba.integration.android.setup',
                'com.apphance.ameba.integration.conventions',
                'com.apphance.ameba.integration.ios',
                'com.apphance.ameba.integration.ios.apphance',
                'com.apphance.ameba.integration.ios.setup',
                'com.apphance.ameba.unit.android',
                'integration.com.apphance.ameba',
                'integration.com.apphance.ameba.executor.jython',
                'integration.com.apphance.ameba.util.file',
                'unit.com.apphance.ameba',
                'unit.com.apphance.ameba.detection',
                'unit.com.apphance.ameba.executor.command',
                'unit.com.apphance.ameba.executor.linker',
                'unit.com.apphance.ameba.ios',
                'unit.com.apphance.ameba.ios.plugins',
                'unit.com.apphance.ameba.ios.plugins.apphance',
                'unit.com.apphance.ameba.ios.plugins.buildplugin',
                'unit.com.apphance.ameba.ios.plugins.ocunit',
                'unit.com.apphance.ameba.plugins', 'unit.com.apphance.ameba.plugins.release',
        ]

        when:
        findAllPackages('', f, currentPackageList)

        then:
        packageList == currentPackageList
    }

    //TODO rewrite this test to spock but, method getDirectoriesSortedAccordingToDepth also needs rewriting
    //TODO reference to project should not be used in this class
    public void testSortingDirectoriesWorksInSimpleCaseOnFiles() throws Exception {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        projectBuilder.withProjectDir(new File("testProjects/ios/GradleXCode"))
        Project project = projectBuilder.build()
        List res = FileManager.getDirectoriesSortedAccordingToDepth(project, { true })
        List newRes = res.collect { sprintf("%08d", it.findAll('[/\\\\]').size()) }
        int last = 0
        newRes.each {
            int current = Integer.parseInt(it)
            assertTrue(current >= last)
            last = current
        }
    }
}
