package com.apphance.ameba.wp7;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import com.apphance.ameba.ProjectHelper;
import com.apphance.ameba.android.AndroidProjectConfiguration;

public class Wp7SingleVariantBuilder {

	ProjectHelper projectHelper
	Wp7ProjectConfiguration wp7conf

	static Logger logger = Logging.getLogger(Wp7SingleVariantBuilder.class)

	public Wp7SingleVariantBuilder(Wp7ProjectConfiguration wp7conf) {
		this.projectHelper = new ProjectHelper()
		this.wp7conf = wp7conf
	}

  void buildRelease(Project project, String target, String configuration) {
        logger.lifecycle( "\n\n\n=== Building target ${target}, configuration ${configuration}  ===")
        if (System.getenv()["SKIP_WP7_BUILDS"] != null) {
            logger.lifecycle ("********************* CAUTION !!!! *********************************")
            logger.lifecycle ("* Skipping WP7 builds because SKIP_WP7_BUILDS variable is set  *")
            logger.lifecycle ("* This should never happen on actual jenkins build                 *")
            logger.lifecycle ("* If it does make sure that SKIP_WP7_BUILDS variable is unset    *")
            logger.lifecycle ("********************************************************************")
        } else {

			File variantDirectory = wp7conf.getVariantDirectory(project, target, configuration)
            projectHelper.executeCommand(project, variantDirectory, [
                "MSBuild"
            ])
        }
		/*
        if (conf.versionString != null) {
            IOSArtifactBuilderInfo bi = buidSingleArtifactBuilderInfo(target, configuration, project)
            prepareDistributionZipFile(project, bi)
            prepareDSYMZipFile(project, bi)
            prepareIpaFile(project, bi)
            prepareManifestFile(project,bi)
            prepareMobileProvisionFile(project,bi)
        } else {
            logger.lifecycle("Skipping building artifacts -> the build is not versioned")
        }
        */
    }
}
