package com.apphance.flow.configuration.android

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.google.inject.Singleton

import javax.inject.Inject

import static com.apphance.flow.util.file.FileManager.relativeTo
import static java.nio.file.Files.isDirectory

@Singleton
class AndroidTestConfiguration extends AbstractConfiguration {

    String configurationName = 'Android Test Configuration'

    private boolean enabledInternal = false

    @Inject AndroidConfiguration conf

    @Override
    boolean isEnabled() {
        enabledInternal && conf.isEnabled()
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    def testDir = new FileProperty(
            name: 'android.dir.test',
            message: 'Directory where test sources are located. By convention this folder should have "robolectric" subfolder if project has ' +
                    'robolectric tests',
            defaultValue: { relativeTo(conf.rootDir.absolutePath, new File(conf.rootDir, 'test').absolutePath) },
            validator: {
                def file = new File(conf.rootDir, it as String)
                isDirectory(file.toPath()) && isDirectory(new File(file, 'robolectric').toPath())
            }
    )

    @Override
    void checkProperties() {
        File libs = new File(conf.rootDir, 'lib/test')
        check libs.exists(), "Directory 'lib/test' does not exist"
        ['junit', 'robolectric'].each { String lib ->
            check libs.list().find { it.contains(lib) }, "Lib $lib is missing"
        }

        check testDir.validator(testDir.value),
                "Incorrect value '${testDir.value}' of property ${testDir.name}. Check that directory exists and contains 'robolectric' subdirectory"
    }
}
