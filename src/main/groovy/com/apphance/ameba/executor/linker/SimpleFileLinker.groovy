package com.apphance.ameba.executor.linker

class SimpleFileLinker implements FileLinker {

    @Override
    String fileLink(File file) {
         file.absolutePath
    }
}
