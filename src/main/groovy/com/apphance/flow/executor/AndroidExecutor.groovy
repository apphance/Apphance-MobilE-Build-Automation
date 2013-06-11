package com.apphance.flow.executor

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import com.google.inject.Singleton
import groovy.transform.PackageScope
import org.gradle.api.GradleException

import javax.inject.Inject

import static org.apache.commons.lang.StringUtils.isNotBlank

@Singleton
class AndroidExecutor {

    static final TARGET_HEADER_PATTERN = /id: ([0-9]+) or "([A-Za-z:\-\. 0-9]+)"/

    private Map<String, List<String>> skinsForTarget = [:]
    private Map<String, String> defaultSkinForTarget = [:]
    private Map<String, String> idForTarget = [:]

    @Inject CommandExecutor executor
    @Inject AndroidConfiguration conf

    @Lazy List<String> listTargetOutput = { run(conf.rootDir, 'list target') }()

    @Lazy List<String> targets = {
        parseResult(listTargetOutput, TARGET_HEADER_PATTERN).sort().findAll { isNotBlank(it) }
    }()

    def updateProject(File dir, String target, String name) {
        def targetId = idForTarget(target)
        run(dir, "update project -p . -t ${targetId ?: target} -n $name -s")
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

    def listAvd() {
        run(conf.rootDir, 'list avd -c')
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

    List<String> skinsForTarget(String target) {
        if (!skinsForTarget[target]) {
            def targetIdx = listTargetOutput.findIndexOf { it?.contains(target) }
            def skinsIdx = listTargetOutput.findIndexOf(targetIdx) { it?.contains('Skins:') }
            def skinsRaw = listTargetOutput[skinsIdx]
            def skinsProcessed = skinsRaw.substring(skinsRaw.indexOf(':') + 1).replaceAll('\\(default\\)', '')
            skinsForTarget[target] = skinsProcessed.split(',').collect { it.trim() }.sort()
        }
        skinsForTarget[target]
    }

    @PackageScope
    String defaultSkinForTarget(String target) {
        if (!defaultSkinForTarget[target]) {
            def targetIdx = listTargetOutput.findIndexOf { it?.contains(target) }
            def skinsIdx = listTargetOutput.findIndexOf(targetIdx) { it?.contains('Skins:') }
            def skinsRaw = listTargetOutput[skinsIdx]
            def skinForTarget = skinsRaw.substring(skinsRaw.indexOf(':') + 1).split(',').find { it.contains('default') }.replaceAll('\\(default\\)', '').trim()
            defaultSkinForTarget[target] = skinForTarget
        }
        defaultSkinForTarget[target]
    }

    def createAvdEmulator(File directory, String name, String targetName, String skin, String cardSize, File avdDir, boolean snapshotsEnabled) {
        run(directory, "-v create avd -n $name -t $targetName -s $skin -c $cardSize -p $avdDir -f ${snapshotsEnabled ? '-a' : ''}", [input: ['no']])
    }

    private List<String> run(File directory, String command, Map params = [:]) {
        try {
            executor.executeCommand(new Command([runDir: directory, cmd: "android $command".split(), failOnError: false] + params))
        } catch (IOException e) {
            throw new GradleException("""|The android utility is probably not in your PATH. Please add it!
                                         |BEWARE! For eclipse junit build it's best to add symbolic link to your
                                         |\$ANDROID_HOME/tools/android in /usr/bin""".stripMargin(), e)
        }
    }
}