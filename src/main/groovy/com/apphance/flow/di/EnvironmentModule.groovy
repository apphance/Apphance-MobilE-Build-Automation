package com.apphance.flow.di

import com.apphance.flow.env.Environment
import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.executor.linker.JenkinsFileLinker
import com.apphance.flow.executor.linker.SimpleFileLinker
import com.google.inject.AbstractModule
import com.google.inject.Provides

import static com.apphance.flow.env.Environment.JENKINS
import static com.apphance.flow.env.JenkinsEnvVariables.JOB_URL
import static com.apphance.flow.env.JenkinsEnvVariables.WORKSPACE

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
