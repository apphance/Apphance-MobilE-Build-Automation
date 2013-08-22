package com.apphance.flow.executor

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.util.FlowUtils
import com.google.inject.Singleton
import groovy.transform.PackageScope

import javax.inject.Inject
import javax.inject.Named

import static org.apache.commons.lang.StringUtils.isNotBlank

@Singleton
class AndroidExecutor {

    static final TARGET_HEADER_PATTERN = /id: ([0-9]+) or "([A-Za-z:\-\. 0-9]+)"/

    private Map<String, String> idForTarget = [:]

    @Inject AndroidConfiguration conf
    @Inject CommandExecutor executor
    @Inject
    @Named('executable.android') ExecutableCommand executableAndroid
    @Inject
    @Named('executable.lint') ExecutableCommand lint


    @Lazy List<String> listTargetOutput = {
        executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: executableAndroid.cmd + ['list', 'target']
        )).toList()
    }()

    @Lazy List<String> targets = {
        parseResult(listTargetOutput, TARGET_HEADER_PATTERN).sort().findAll { isNotBlank(it) }
    }()

    def updateProject(File dir, String target, String name) {
        def targetParam = target ? ['-t', "${idForTarget(target) ?: target}"] : []
        def nameParam = name ? ['-n', name] : []
        executor.executeCommand(new Command(
                runDir: dir,
                cmd: executableAndroid.cmd + ['update', 'project', '-p', '.', '-s'] + targetParam + nameParam
        ))
    }

    File runLint(File dir, File report) {
        executor.executeCommand(new Command(
                runDir: dir,
                cmd: lint.cmd + ['--html', report.absolutePath, '.']
        ))
        report
    }

    @PackageScope
    String idForTarget(String target) {
        if (!idForTarget[target]) {
            listTargetOutput.collect {
                def header = (it =~ TARGET_HEADER_PATTERN)
                if (header.matches())
                    idForTarget[header[0][2]] = header[0][1]
            }
        }
        idForTarget[target]
    }

    private List<String> parseResult(input, regex) {
        def result = []
        input.each {
            it = it?.trim()
            def matcher = (it =~ regex)
            if (matcher.matches()) {
                result << matcher[0][2]
            }
        }
        result
    }
}