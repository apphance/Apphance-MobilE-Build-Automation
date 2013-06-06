package com.apphance.ameba.di

import com.apphance.ameba.env.Environment
import com.apphance.ameba.executor.linker.FileLinker
import com.apphance.ameba.executor.linker.JenkinsFileLinker
import com.apphance.ameba.executor.linker.SimpleFileLinker
import com.google.inject.AbstractModule
import com.google.inject.Provides

import static com.apphance.ameba.env.Environment.JENKINS
import static com.apphance.ameba.env.JenkinsEnvVariables.JOB_URL
import static com.apphance.ameba.env.JenkinsEnvVariables.WORKSPACE

class EnvironmentModule extends AbstractModule {

    @Override
    protected void configure() {}

    @Provides
    FileLinker fileLinker() {
        def env = System.getenv()
        if (Environment.env() == JENKINS) {
            return new JenkinsFileLinker(env[JOB_URL.name()], env[WORKSPACE.name()])
        }
        return new SimpleFileLinker()
    }
}
