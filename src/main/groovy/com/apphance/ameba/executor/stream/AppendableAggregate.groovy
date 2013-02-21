package com.apphance.ameba.executor.stream

class AppendableAggregate implements Appendable {

    private Collection<Appendable> appendables

    AppendableAggregate(Collection<Appendable> appendables) {
        this.appendables = appendables
    }

    @Override
    Appendable append(CharSequence csq) throws IOException {
        appendables.each {
            it.append(csq)
        }
        this
    }

    @Override
    Appendable append(CharSequence csq, int start, int end) throws IOException {
        appendables.each {
            it.append(csq, start, end)
        }
        this
    }

    @Override
    Appendable append(char c) throws IOException {
        appendables.each {
            it.append(c)
        }
        this
    }
}
