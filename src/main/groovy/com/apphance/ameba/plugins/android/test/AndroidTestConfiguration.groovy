package com.apphance.ameba.plugins.android.test

class AndroidTestConfiguration {

    public static final String AVD_PATH = 'avds'

    File androidTestDirectory
    String emmaDumpFile
    String xmlJUnitDir
    File coverageDir
    File rawDir
    File coverageEmFile
    File coverageEcFile
    File adbBinary
    File androidBinary
    File avdDir

    def testProjectPackage
    def testProjectName

    String emulatorName
    String emulatorTargetName

    String emulatorSkin
    String emulatorCardSize
    boolean emulatorSnapshotsEnabled
    boolean useEmma
    boolean testPerPackage
    boolean emulatorNoWindow

    int emulatorPort
}
