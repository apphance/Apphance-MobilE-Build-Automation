package com.apphance.ameba.executor

import com.apphance.ameba.executor.linker.JenkinsFileLinker
import com.apphance.ameba.executor.linker.SimpleFileLinker
import spock.lang.Specification

class FileLinkerSpec extends Specification {

    def 'simple file linker returns empty file link'() {

        given:
        def logfile = new File('.')

        and:
        def simpleFileLinker = new SimpleFileLinker()

        expect:
        logfile.absolutePath == simpleFileLinker.fileLink(logfile)
    }

    def 'jenkins file linker return url file link'(){
        given:
        def logfile = new File('log')

        def workspace = new File('.')
        and:
        def linker = new JenkinsFileLinker('jenkins://job/', workspace.canonicalPath)

        when:
        String link = linker.fileLink(logfile)

        then:
        link.startsWith "jenkins://job/ws/"
        link.substring("jenkins://job/ws/".size()) == logfile.name
    }
}
