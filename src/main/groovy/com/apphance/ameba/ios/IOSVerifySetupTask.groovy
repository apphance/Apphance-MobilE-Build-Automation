package com.apphance.ameba.ios

import java.util.Properties

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction
import org.gradle.execution.ExcludedTaskFilteringBuildConfigurationAction;

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectHelper;


class IOSVerifySetupTask extends DefaultTask {
    Logger logger = Logging.getLogger(IOSVerifySetupTask.class)
	
	public enum IosProperty {
		
		PLIST_FILE(false, 'ios.plist.file', 'Path to plist file'),
		EXCLUDED_BUILDS(false, 'ios.excluded.builds', 'List of excluded builds'),
		IOS_FAMILIES(false, 'ios.families', 'List of iOS families'),
		DISTRIBUTION_DIR(false, 'ios.distribution.resources.dir', 'Path to distribution resources directory'),
		MAIN_TARGET(true, 'ios.mainTarget', 'Main target for release build'),
		MAIN_CONFIGURATION(true, 'ios.mainConfiguration', 'Main configuration for release build'),
		IOS_SDK(true, 'ios.sdk', 'List of iOS SDKs'),
		IOS_SIMULATOR_SDK(true, 'ios.simulator.sdk', 'List of iOS simulator SDKs'),
		FONE_MONKEY_CONFIGURATION(true, 'ios.fonemonkey.configuration', 'FoneMonkey build configuration'),
		KIF_CONFIGURATION(true, 'ios.kif.configuration', 'KIF build configuration');
		
		private final boolean optional
		private final String name
		private final String description
		
		IosProperty(boolean optional, String name, String description) {
			this.optional = optional
			this.name = name
			this.description = description
		}
		
		public boolean isOptional() {
			return optional
		}
		
		public String getName() {
			return name
		}
	}
	
    IOSVerifySetupTask() {
        this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        this.description = 'Verifies if iOS-specific properties of the project have been setup properly'
        project.verifySetup.dependsOn(this)
    }


    @TaskAction
    void verifySetup() {
        Properties projectProperties = project['gradleProperties']
		for (IosProperty p : IosProperty.values()) {
			if (!p.isOptional()) {
				checkProperty(projectProperties, p.getName())
			}
		}
		checkPlistFile(projectProperties)
		checkFamilies(projectProperties)
        checkDistributionDir(projectProperties)
		checkTargets(projectProperties)
        logger.lifecycle("GOOD!!! ALL IOS PROJECT PROPERTIES SET CORRECTLY!!!")
    }

    void checkPlistFile(Properties projectProperties) {
        File plistFile = new File(project.rootDir,projectProperties.getProperty('ios.plist.file'))
        if (!plistFile.exists() || !plistFile.isFile()) {
            throw new GradleException("""The plist file (${plistFile}) does not exist
or is not a file. Please run 'gradle prepareSetup' to correct it.""")
        }
    }

    void checkDistributionDir(Properties projectProperties) {
        File distributionResourcesDir = new File(project.rootDir,projectProperties.getProperty('ios.distribution.resources.dir'))
        if (!distributionResourcesDir.exists() || !distributionResourcesDir.isDirectory()) {
            throw new GradleException("""The distribution resources directory (${distributionResourcesDir})
does not exist or is not a directory. Please run 'gradle prepareSetup' to correct it.""")
        }
    }

    void checkFamilies(Properties projectProperties) {
        String[] families = projectProperties.getProperty('ios.families').split(',')
        def validFamilies = ['iPad', 'iPhone']
        families.each { family ->
            if (!validFamilies.contains(family)) {
                throw new GradleException("""The family in ${families} can only be one of ${validFamilies}""")
            }
        }
    }

    void checkProperty(Properties projectProperties, String propertyName) {
        if (projectProperties.getProperty(propertyName) == null) {
            throw new GradleException("""Property ${propertyName} should be defined in gradle.properties.
!!!!! Please run "gradle prepareSetup" to correct project's configuration !!!!!""")
        }
    }
	
	void checkTargets(Properties projectProperties) {
		ProjectHelper projectHelper = new ProjectHelper();
		def lines = projectHelper.executeCommand(project, ["xcodebuild", "-list"]as String[],false, null, null, 1, true)
		def trimmed = lines*.trim()
		IOSConfigurationAndTargetRetriever iosConfigurationAndTargetRetriever = new IOSConfigurationAndTargetRetriever()
		IOSProjectConfiguration iosConf = iosConfigurationAndTargetRetriever.getIosProjectConfiguration(project)
		iosConf.targets = iosConfigurationAndTargetRetriever.readBuildableTargets(trimmed)
		iosConf.configurations = iosConfigurationAndTargetRetriever.readBuildableConfigurations(trimmed)
		iosConf.excludedBuilds = project.hasProperty('ios.excluded.builds') ? project['ios.excluded.builds'].split(",")*.trim() : []
		if (iosConf.targets == ['']) {
			throw new GradleException("You must specify at least one target")
		}
		if (iosConf.configurations == ['']) {
			throw new GradleException("You must specify at least one configuration")
		}
		if (iosConf.excludedBuilds != ['.*'] && iosConf.excludedBuilds.size != ios.targets.size * ios.configurations.size) {
			def mainTarget
			if (!project.hasProperty(IosProperty.MAIN_TARGET.getName())) {
				mainTarget = projectProperties.getProperty(IosProperty.MAIN_TARGET.getName())
			} else {
				mainTarget = iosConf.targets[0]
			}
			
			def mainConfiguration
			if (!project.hasProperty(IosProperty.MAIN_CONFIGURATION.getName())) {
				mainConfiguration = projectProperties.getProperty(IosProperty.MAIN_CONFIGURATION.getName())
			} else {
				mainConfiguration = iosConf.configurations[0]
			}
			
			def id = "${mainTarget}-${mainConfiguration}".toString()
			
			if (iosConf.isBuildExcluded(id)) {
				throw new GradleException("Main target ${id} is excluded from build")
			}
		}


	}
}
