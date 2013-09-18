package com.apphance.flow.executor

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import com.google.inject.Singleton
import groovy.transform.PackageScope

import javax.inject.Inject
import javax.inject.Named

import static org.apache.commons.lang.StringUtils.isNotBlank

@Singleton
class AndroidExecutor {

    static final TARGET_HEADER_PATTERN = /id: ([0-9]+) or "([A-Za-z:\-\. 0-9]+)"/

    @Inject AndroidConfiguration conf
    @Inject CommandExecutor executor
    @Inject
    @Named('executable.android') ExecutableCommand executableAndroid
    @Inject
    @Named('executable.lint') ExecutableCommand lint


    @Lazy List<String> targets = {
        parseResult(listTargetOutput, TARGET_HEADER_PATTERN).sort().findAll { isNotBlank(it) }
    }()

    def updateProject(File dir, String target, String name) {
        def targetParam = target ? ['-t', "${idForTarget.call(target) ?: target}"] : []
        def nameParam = name ? ['-n', name] : []
        executor.executeCommand(new Command(
                runDir: dir,
                cmd: executableAndroid.cmd + ['update', 'project', '-p', '.', '-s'] + targetParam + nameParam
        ))
    }

    File runLint(File dir, File reportDir) {
        executor.executeCommand(new Command(
                runDir: dir,
                cmd: lint.cmd + ['--html', new File(reportDir, 'report.html').absolutePath, '--xml', new File(reportDir, 'report.xml').absolutePath, '.']
        ))
        reportDir
    }

    @PackageScope
    Closure<String> idForTarget = { String target ->
        def pattern = /id: ([0-9]+) or "$target"/
        def header = listTargetOutput.find { it =~ pattern } =~ pattern
        header.matches() ? header[0][1] : target
    }.memoize()

    @Lazy List<String> listTargetOutput = {
        executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: executableAndroid.cmd + ['list', 'target']
        )).toList()
    }()

    private List<String> parseResult(input, regex) {
        input.collect {
            def matcher = (it?.trim() =~ regex)
            matcher.matches() ? matcher[0][2] : ''
        }
    }
}