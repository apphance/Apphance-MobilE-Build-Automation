package com.apphance.flow.documentation.sites

import com.google.gdata.data.sites.ContentEntry

class AmebaFileUploader {
    private static File getZipFile(String... args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("The third argument of the call should be file name")
        }
        File documentationZip = new File(args[2])
        if (!documentationZip.exists()) {
            throw new IllegalStateException("The file ${documentationZip} does not exist")
        }
        return documentationZip
    }


    public static void main(String[] args) {
        SitesCommunicator sitesCommunicator = new SitesCommunicator()
        File zipFile = getZipFile(args)
        sitesCommunicator.readUserPassword(args)
        sitesCommunicator.createService()
        sitesCommunicator.loggingIn()
        ContentEntry page = sitesCommunicator.retrieveDownloadPage()
        sitesCommunicator.uploadAttachment(zipFile, page, "Documentation", "Documentation: ${zipFile}")
    }


}
