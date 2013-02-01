package com.apphance.ameba.util.file

/**
 * System output that also writes to file.
 */

class FileSystemOutput implements Appendable {

    File file
    StringBuilder sb = new StringBuilder()
    Appendable linkedAppendable

    FileSystemOutput(File file, Appendable linkedAppendable = null) {
        this.file = file
        this.linkedAppendable = linkedAppendable
    }

    @Override
    Appendable append(char c) throws IOException {
        if (file != null) {
            file << c
        }
        sb.append(c)
        if (linkedAppendable != null) {
            linkedAppendable.append(c)
        }
        this
    }

    @Override
    Appendable append(CharSequence c) throws IOException {
        if (file != null) {
            file << c
        }
        sb.append(c)
        if (linkedAppendable != null) {
            linkedAppendable.append(c)
        }
        this
    }

    @Override
    Appendable append(CharSequence cs, int start, int end) throws IOException {
        if (file != null) {
            file << cs?.subSequence(start, start)
        }
        sb.append(cs, start, end)
        if (linkedAppendable != null) {
            linkedAppendable.append(cs, start, end)
        }
        this
    }
}
