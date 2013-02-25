package com.apphance.ameba.executor.stream

class AppendableAdapter implements Appendable {
    private Collection<File> delegates = []

    AppendableAdapter(File... delegates) {
        this.delegates = delegates
    }

    @Override
    Appendable append(CharSequence csq) throws IOException {
        delegates.each { it.append(csq as String) }
        this
    }

    @Override
    Appendable append(CharSequence csq, int start, int end) throws IOException {
        delegates.each { it.append(csq.subSequence(start, end) as String) }
        this
    }

    @Override
    Appendable append(char c) throws IOException {
        delegates.each { it.append(c as String) }
        this
    }
}
