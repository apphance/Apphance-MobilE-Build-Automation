package com.apphance.ameba.executor.linker

class JenkinsFileLinker implements FileLinker {

    private String workspace
    private String jobUrl

    JenkinsFileLinker(String jobUrl, String workspace) {
        this.jobUrl = jobUrl
        this.workspace = workspace
    }

    @Override
    String fileLink(File file) {
        String filePathInWorkspace = file.canonicalPath.substring(workspace.size())
        "${jobUrl}ws${filePathInWorkspace}" // jobUrl ends with '/', filePathInWorkspace starts with '/'
    }
}
