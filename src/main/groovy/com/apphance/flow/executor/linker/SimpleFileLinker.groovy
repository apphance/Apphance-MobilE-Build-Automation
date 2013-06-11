package com.apphance.flow.executor.linker

class SimpleFileLinker implements FileLinker {

    @Override
    String fileLink(File file) {
         file.absolutePath
    }
}
