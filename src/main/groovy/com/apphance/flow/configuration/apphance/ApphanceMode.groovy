package com.apphance.flow.configuration.apphance

enum ApphanceMode {
    QA('preprod'), SILENT('preprod'), PROD('prod'), DISABLED('')

    private String repoSuffix

    ApphanceMode(String repoSuffix) {
        this.repoSuffix = repoSuffix
    }

    String getRepoSuffix() {
        return repoSuffix
    }
}