package com.apphance.ameba.android.plugins.test

/**
 * Properties for the android test plugin.
 *
 */
public enum AndroidTestProperty {
    EMULATOR_SKIN('android.test.emulator.skin', 'Android emulator skin', 'WVGA800'),
    EMULATOR_CARDSIZE('android.test.emulator.cardSize', 'Size of the sd card attached to emulator', '200M'),
    EMULATOR_SNAPSHOT_ENABLED('android.test.emulator.snapshotEnabled', 'Flag specifying if emulator uses snapshots (much faster)', 'true'),
    EMULATOR_NO_WINDOW('android.test.emulator.noWindow', 'Flag specifying if no-window option should be used with emulator', 'true'),
    EMULATOR_TARGET('android.test.emulator.target', 'Target of the emulator'),
    TEST_DIRECTORY('android.test.directory', 'Directory where Robotium test project is located', 'test/android'),
    TEST_PER_PACKAGE('android.test.perPackage', 'Flag specifying if tests should be run per package. If false, then all are run at once', 'false'),
    MOCK_LOCATION('android.test.mockLocation', 'Whether the test application should be build with location mocking enabled (for testing location-based apps)', 'false'),
    USE_EMMA('android.useEmma', 'Whether emma test coverage should be run', 'true'),

    public static final DESCRIPTION = 'Android test properties'


    final String propertyName
    final String description
    final String defaultValue

    AndroidTestProperty(String propertyName, String description, String defaultValue = null) {
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}
