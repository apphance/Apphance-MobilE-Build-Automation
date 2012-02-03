package com.apphance.ameba


import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat

import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logging;
import org.gradle.api.logging.Logger;


class FileSystemOutput implements Appendable{
    File file
    StringBuilder sb = new StringBuilder()
    Appendable linkedAppendable
    FileSystemOutput(File file, Appendable linkedAppendable = null) {
        this.file = file
        this.linkedAppendable = linkedAppendable
    }
    Appendable append(char arg0) throws IOException {
        if (file != null) {
            file << arg0
        }
        sb.append(arg0)
        if (linkedAppendable != null) {
            linkedAppendable.append(arg0)
        }
        return this
    }
    Appendable append(CharSequence arg0) throws IOException {
        if (file != null) {
            file << arg0
        }
        sb.append(arg0)
        if (linkedAppendable != null) {
            linkedAppendable.append(arg0)
        }
        return this
    }
    Appendable append(CharSequence arg0, int arg1, int arg2) throws IOException {
        if (file != null) {
            file << arg0?.subSequence(arg1, arg1)
        }
        sb.append(arg0, arg1, arg2)
        if (linkedAppendable != null) {
            linkedAppendable.append(arg0, arg1, arg2)
        }
        return this
    }
}

class ProjectHelper {
    static Logger logger = Logging.getLogger(ProjectHelper.class)
    def replacePasswordsWithStars(originalArray) {
        def newList = []
        def nextPassword = false
        originalArray.each {
            if (nextPassword) {
                newList << "*************"
                nextPassword = false
            } else {
                if (it  == null) {
                    throw new GradleException("One of the elements in ${originalArray} is null!")
                }
                if (it.toString().toLowerCase().contains("password")) {
                    def splitElement = it.split("=")
                    if (splitElement.length > 1) {
                        newList << it.split("=")[0] + "=****************"
                    } else {
                        nextPassword = true;
                        newList <<it
                    }
                } else {
                    newList << it
                }
            }
        }
        return newList
    }

    Collection<String> executeCommand(Project project, command, boolean failOnError = true, String [] envp = null,
    input = null, int retryTimes = 1, boolean silentLogging = false) {
        def runDirectory = new File("${project.rootDir}")
        return executeCommand(project, runDirectory, command, failOnError, envp, input, retryTimes, silentLogging)
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
        def relativeUrlOfProjectDir = currentPath.substring(workspacePath.length()).replaceAll('\\\\','/')
        if (relativeUrlOfProjectDir != '' && !relativeUrlOfProjectDir.startsWith('/')) {
            relativeUrlOfProjectDir = '/' + relativeUrlOfProjectDir
        }
        jobUrl = jobUrl.endsWith('/') ? jobUrl : jobUrl + '/'
        return jobUrl + 'ws' + relativeUrlOfProjectDir
    }

    String getCurrentFileNumber(File logDir) {
        File f = new File(logDir,"file_number.txt")
        f.parentFile.mkdirs()
        int number = 0
        try {
            number = new Integer(f.text)
        } catch(Exception e) {
            // do nothing
        }
        f.delete()
        f << (number + 1)
        return String.format('%04d',number)
    }

    void findAllPackages(String currentPackage, File directory, currentPackageList) {
        boolean empty = true
        directory.eachFile(FileType.FILES, { empty = false })
        if (!empty) {
            currentPackageList << currentPackage
        }
        boolean rootDirectory = (currentPackage == '')
        directory.eachDir {
            findAllPackages(rootDirectory ? it.name : (currentPackage + '.' + it.name), it, currentPackageList)
        }
    }

    String getFileNameFromCommand(Project project, File logDir, String command, String postFix) {
        String fileAbleCommandName = command.replaceAll(' |\\p{Punct}',"_")
        fileAbleCommandName = fileAbleCommandName.substring(0,Math.min(80, fileAbleCommandName.length()))
        return getCurrentFileNumber(logDir) + '-' + fileAbleCommandName + postFix
    }

    FileSystemOutput getSystemOutput(Project project, File logDir, String commandToDisplay, postfix, String jenkinsURL) {
        String outFileName = getFileNameFromCommand(project, logDir, commandToDisplay,postfix)
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

    Collection<String> executeCommand(Project project, File runDirectory, command, boolean failOnError = true, String [] envp = null,
    input = null, int retryTimes = 1, boolean silentLogging = false) {
        File logDir = new File(project.rootDir, "log")
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        int timesLeft = retryTimes
        def commandToDisplay = getCommandToDisplay(command)
        while (timesLeft-- > 0) {
            String extraText = ''
            if (retryTimes>1) {
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
                standardOut = getSystemOutput(project, logDir, commandToDisplay, '-output.txt', jenkinsURL)
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

    Process executeCommandInBackground(Project project, File outErrFile, command, String [] envp = null, input = null) {
        def runDirectory = new File("${project.rootDir}")
        return executeCommandInBackground(project, runDirectory, outErrFile, command, envp, input)
    }

    Process executeCommandInBackground(File runDirectory, File outErrFile, command, String [] envp = null, input = null) {
        def commandToDisplay = getCommandToDisplay(command)
        logger.lifecycle("Executing command:\n${commandToDisplay}\nin ${runDirectory} in background")
        logger.lifecycle("Standard output/error is stored in ${outErrFile}")
        def standardOut = new FileSystemOutput(outErrFile)
        def standardErr = new FileSystemOutput(outErrFile, System.err)
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
        return proc
    }

    private waitForProcess(Process proc, Thread errorThread, Thread outputThread) {
        def exitValue = proc.waitFor()
        errorThread.join()
        outputThread.join()
        return exitValue
    }

    private addWriter(Process proc, input) {
        if (input != null) {
            proc.withWriter { writer ->
                input.each { writer << it }
            }
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
                    commandToDisplay.append(it.toString().replaceAll('"','\"'))
                    commandToDisplay.append('" ')
                } else {
                    commandToDisplay.append(it.toString())
                    commandToDisplay.append(' ')
                }
            }
            return commandToDisplay.toString()
        }
    }

    @Override
    def String getExpectedProperty(Project project, String property) {
        if (!project.hasProperty(property)) {
            throw new GradleException("I need ${property} property to be set on project")
        }
        return project[property]
    }

    @Override
    def String getOptionalProperty(Project project, String property) {
        if (!project.hasProperty(property)) {
            return null
        }
        return project[property]
    }

    ProjectConfiguration getProjectConfiguration(Project project){
        if (!project.hasProperty('project.configuration')) {
            project['project.configuration'] = new ProjectConfiguration()
        }
        return project['project.configuration']
    }

    private readBasicProjectData(Project project) {
        ProjectConfiguration conf = getProjectConfiguration(project)
        if (conf.projectName == null) {
            conf.projectName = getExpectedProperty(project,'project.name')
        }
        conf.projectDirectoryName = getExpectedProperty(project,'project.directory.name')
        conf.baseUrl = new URL(getExpectedProperty(project,'project.url.base'))
        conf.iconFile = new File(project.rootDir,getExpectedProperty(project,'project.icon.file'))
        retrieveLocale(project, conf)
        conf.releaseNotes = readReleaseNotes(project)?.tokenize(",")
    }

    private void retrieveLocale(Project project, ProjectConfiguration conf) {
        String language = getOptionalProperty(project, 'project.language')
        String country = getOptionalProperty(project, 'project.country')
        if (language == null) {
            conf.locale = Locale.getDefault()
        } else {
            if (country == null) {
                conf.locale = new Locale(language)
            } else {
                conf.locale = new Locale(language,country)
            }
        }
        conf.buildDate = new SimpleDateFormat("dd-MM-yyyy HH:mm zzz", conf.locale).format(new Date())
    }

    def String readReleaseNotes(Project project) {
        if (project.hasProperty('release.notes')) {
            return project['release.notes']
        } else {
            def notes =  System.getenv('RELEASE_NOTES')
            if (notes == null || notes == "") {
                return null
            }
            project['release.notes'] = notes
            return notes
        }
    }

    String readPropertyOrEnvironmentVariable(Project project, String property) {
        if (project.hasProperty(property)) {
            return project[property]
        } else if (System.getProperty(property) != null){
            return System.getProperty(property)
        } else {
            def envVariable = property.toUpperCase().replace(".","_")
            def val = System.getenv(envVariable)
            if (val == null) {
                throw new GradleException("The property ${property} was not defined (neither in project nor system) and ${envVariable} environment variable is missing.")
            }
            return val
        }
    }

    String isPropertyOrEnvironmentVariableDefined(Project project, String property) {
        if (project.hasProperty(property)) {
            return true
        } else if (System.getProperty(property) != null){
            return true
        } else {
            def envVariable = property.toUpperCase().replace(".","_")
            def val = System.getenv(envVariable)
            return (val != null)
        }
    }


    String getHumanReadableSize(long byteSize) {
        if (byteSize >= 1024L*1024L) {
            return String.format("%.2f",byteSize*1.0/1024.0/1024.0) + " MB"
        } else {
            return String.format("%.2f",byteSize*1.0/1024.0) + " kB"
        }
    }

    private void fillMailSubject(Project project, ResourceBundle resourceBundle) {
        ProjectConfiguration conf = getProjectConfiguration(project)
        String subject = resourceBundle.getString('Subject')
        conf.releaseMailSubject = Eval.me("conf",conf,/"$subject"/)
    }
}
