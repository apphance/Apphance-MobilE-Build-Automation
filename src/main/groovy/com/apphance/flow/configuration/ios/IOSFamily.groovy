package com.apphance.flow.configuration.ios

enum IOSFamily {

    IPHONE('1'), IPAD('2')

    private String UIDDeviceFamily

    IOSFamily(String uIdDeviceFamily) {
        this.UIDDeviceFamily = uIdDeviceFamily
    }

    String getUIDDeviceFamily() {
        return UIDDeviceFamily
    }

    String iFormat() {
        name()[0].toLowerCase() + name().substring(1).toLowerCase().capitalize()
    }
}