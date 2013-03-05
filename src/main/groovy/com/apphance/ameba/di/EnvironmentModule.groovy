package com.apphance.ameba.di

import com.apphance.ameba.executor.linker.FileLinker
import com.apphance.ameba.executor.linker.JenkinsFileLinker
import com.apphance.ameba.executor.linker.SimpleFileLinker
import com.google.inject.AbstractModule
import com.google.inject.Provides

import static com.apphance.ameba.di.EnvironmentModule.JenkinsEnvVaribles.*

class EnvironmentModule extends AbstractModule {

    @Override
    protected void configure() {}

    @Provides
    FileLinker fileLinker() {
        def env = System.getenv()
        if (isJenkinsEnv(env)) {
            return new JenkinsFileLinker(env[JOB_URL.name()], env[WORKSPACE.name()])
        }
        return new SimpleFileLinker()
    }

    private boolean isJenkinsEnv(env) {
        (env[JENKINS_URL.name()] && env[JOB_URL.name()] && env[WORKSPACE.name()])
    }

    enum JenkinsEnvVaribles {
        JOB_URL, JENKINS_URL, WORKSPACE
    }
}
