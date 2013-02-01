package com.apphance.ameba

import com.apphance.ameba.util.file.FileSystemOutput
import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Useful helper for common project-related methods.
 *
 */
class ProjectHelper {

    public static final String[] GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=512m', '-XX:+CMSClassUnloadingEnabled',
            '-XX:+CMSPermGenSweepingEnabled', '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    static Logger logger = Logging.getLogger(ProjectHelper.class)

    Collection<String> executeCommand(Project project, command, boolean failOnError = true, String[] envp = null, input = null, int retryTimes = 1, boolean silentLogging = false) {
        def runDirectory = new File("${project.rootDir}")
        return executeCommand(project, runDirectory, command, failOnError, envp, input, retryTimes, silentLogging)
    }

    Collection<String> executeCommand(Project project, File runDirectory, command, boolean failOnError = true, String[] envp = null, input = null, int retryTimes = 1, boolean silentLogging = false) {

        File logDir = project.file("log")
        if (!logDir.exists()) {
            logDir.mkdirs()
        }

        int timesLeft = retryTimes
        def commandToDisplay = getCommandToDisplay(command)
        while (timesLeft-- > 0) {
            String extraText = ''
            if (retryTimes > 1) {
                extraText = " for the ${retryTimes - timesLeft}. time (maximum number of retries: ${retryTimes})"
            }
            logger.lifecycle("Executing command:\n${commandToDisplay}\nin ${runDirectory} ${extraText}")
            String jenkinsURL = getJenkinsURL(project, System.getenv())
            def standardOut
            def standardErr
            if (silentLogging) {
                standardOut = new FileSystemOutput(null)
                standardErr = new FileSystemOutput(null)
            } else {
                standardOut = getSystemOutput(logDir, commandToDisplay, '-output.txt', jenkinsURL)
                standardErr = new FileSystemOutput(standardOut.file, System.err)
            }
            Process proc = null
            try {
                proc = command.execute(envp, runDirectory)
            } catch (IOException e) {
                if (e.getMessage().contains("error=2")) {
                    if (command instanceof GString) {
                        command = (command.toString()).replaceFirst(/(\w*)/) { all, name ->
                            return "${name}.bat"
                        }
                    } else if (command instanceof String) {
                        command = command.replaceFirst(/(\w*)/) { all, name ->
                            return "${name}.bat"
                        }
                    } else {
                        command[0] = command[0] + '.bat'
                    }
                    commandToDisplay = getCommandToDisplay(command)
                    logger.lifecycle("Command failed. Trying to execute .bat version: ${commandToDisplay}")
                    proc = command.execute(envp, runDirectory)
                } else {
                    throw e
                }
            }
            addWriter(proc, input)
            Thread errorThread = ProcessGroovyMethods.consumeProcessErrorStream(proc, standardErr)
            Thread outputThread = ProcessGroovyMethods.consumeProcessOutputStream(proc, standardOut)
            def exitValue = waitForProcess(proc, errorThread, outputThread)
            if (exitValue != 0) {
                def errorOutput = standardErr.sb.toString().split("\n")
                if (failOnError) {
                    if (timesLeft == 0) {
                        throw new GradleException("""Error when executing ${commandToDisplay}:\n   working directory: ${runDirectory},
        \n   exit value: ${exitValue},\n\n   errorOutput:\n
        ${errorOutput.join('\n')},\n""")
                    } else {
                        continue // try again
                    }
                }
            }
            return standardOut.sb.toString().split("\n")
        }
    }

    private String getCommandToDisplay(command) {
        if (command instanceof String || command instanceof GString) {
            return command.toString()
        } else {
            StringBuilder commandToDisplay = new StringBuilder()
            def commandToDisplayTmp = replacePasswordsWithStars(command)
            commandToDisplayTmp.each {
                if (it.toString().contains(' ')) {
                    commandToDisplay.append('"')
                    commandToDisplay.append(it.toString().replaceAll('"', '\"'))
                    commandToDisplay.append('" ')
                } else {
                    commandToDisplay.append(it.toString())
                    commandToDisplay.append(' ')
                }
            }
            return commandToDisplay.toString()
        }
    }

    def replacePasswordsWithStars(originalArray) {
        def newList = []
        def nextPassword = false
        originalArray.each {
            if (nextPassword) {
                newList << "*************"
                nextPassword = false
            } else {
                if (it == null) {
                    throw new GradleException("One of the elements in ${originalArray} is null!")
                }
                if (it.toString().toLowerCase().contains("password")) {
                    def splitElement = it.split("=")
                    if (splitElement.length > 1) {
                        newList << it.split("=")[0] + "=****************"
                    } else {
                        nextPassword = true;
                        newList << it
                    }
                } else {
                    newList << it
                }
            }
        }
        return newList
    }

    String getJenkinsURL(Project project, Map env) {
        def jenkinsUrl = env['JENKINS_URL']
        if (jenkinsUrl == null) {
            return null
        }
        def jobUrl = env['JOB_URL']
        if (jobUrl == null) {
            return null
        }
        def currentPath = project.rootDir.canonicalPath
        def workspacePath = new File(env['WORKSPACE']).canonicalPath
        def relativeUrlOfProjectDir = currentPath.substring(workspacePath.length()).replaceAll('\\\\', '/')
        if (relativeUrlOfProjectDir != '' && !relativeUrlOfProjectDir.startsWith('/')) {
            relativeUrlOfProjectDir = '/' + relativeUrlOfProjectDir
        }
        jobUrl = jobUrl.endsWith('/') ? jobUrl : jobUrl + '/'
        return jobUrl + 'ws' + relativeUrlOfProjectDir
    }

    FileSystemOutput getSystemOutput(File logDir, String commandToDisplay, postfix, String jenkinsURL) {
        String outFileName = getFileNameFromCommand(logDir, commandToDisplay, postfix)
        File outFile = new File(logDir, outFileName)
        outFile.delete()
        outFile << ''
        if (jenkinsURL == null) {
            logger.lifecycle("OUTPUT: ${outFile}")
        } else {
            String resultUrl = jenkinsURL + '/' + logDir.getName() + '/' + outFileName
            logger.lifecycle("OUTPUT: ${resultUrl}")
        }
        return new FileSystemOutput(outFile)
    }

    String getFileNameFromCommand(File logDir, String command, String postFix) {
        String fileAbleCommandName = command.replaceAll(' |\\p{Punct}', "_")
        fileAbleCommandName = fileAbleCommandName.substring(0, Math.min(80, fileAbleCommandName.length()))
        return getCurrentFileNumber(logDir) + '-' + fileAbleCommandName + postFix
    }


    String getCurrentFileNumber(File logDir) {
        File f = new File(logDir, "file_number.txt")
        f.parentFile.mkdirs()
        int number = 0
        try {
            number = new Integer(f.text)
        } catch (Exception e) {
            // do nothing
        }
        f.delete()
        f << (number + 1)
        return String.format('%04d', number)
    }

    private waitForProcess(Process proc, Thread errorThread, Thread outputThread) {
        def exitValue = proc.waitFor()
        errorThread.join()
        outputThread.join()
        return exitValue
    }

    Process executeCommandInBackground(File runDirectory, File outErrFile, command, String[] envp = null, input = null) {
        def commandToDisplay = getCommandToDisplay(command)
        logger.lifecycle("Executing command:\n${commandToDisplay}\nin ${runDirectory} in background")
        logger.lifecycle("Standard output/error is stored in ${outErrFile}")
        def standardOut = new FileSystemOutput(outErrFile)
        def standardErr = new FileSystemOutput(outErrFile, System.err)
        Process proc = null
        try {
            proc = command.execute(envp, runDirectory)
        } catch (IOException e) {
            if (e.getMessage().contains("error=2")) {
                if (command instanceof GString) {
                    command = (command.toString()).replaceFirst(/(\w*)/) { all, name ->
                        return "${name}.bat"
                    }
                } else if (command instanceof String) {
                    command = command.replaceFirst(/(\w*)/) { all, name ->
                        return "${name}.bat"
                    }
                } else {
                    command[0] = command[0] + '.bat'
                }
                commandToDisplay = getCommandToDisplay(command)
                logger.lifecycle("Command failed. Trying to execute .bat version: ${commandToDisplay}")
                proc = command.execute(envp, runDirectory)
            } else {
                throw e
            }
        }
        addWriter(proc, input)
        ProcessGroovyMethods.consumeProcessErrorStream(proc, standardErr)
        ProcessGroovyMethods.consumeProcessOutputStream(proc, standardOut)
        return proc
    }

    private addWriter(Process proc, input) {
        if (input != null) {
            proc.withWriter { writer ->
                input.each { writer << it }
            }
        }
    }

}
