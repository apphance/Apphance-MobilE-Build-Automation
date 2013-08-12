package com.apphance.flow.util

import static com.google.common.base.Preconditions.checkArgument
import static java.lang.Math.max
import static org.apache.commons.lang.StringUtils.isNotBlank

class Version implements Comparable<Version> {

    static final VERSION_PATTERN = '^(\\d+\\.)*\\d+$'

    private String version

    final String getVersion() {
        this.@version
    }

    Version(String version) {

        checkArgument(isNotBlank(version), 'Version can not be blank!')
        checkArgument(version.matches(VERSION_PATTERN), 'Invalid version format!')

        this.version = version
    }

    @Override
    public int compareTo(Version that) {
        if (that == null)
            return 1
        String[] thisParts = version.split('\\.')
        String[] thatParts = that.version.split('\\.')
        int length = max(thisParts.length, thatParts.length)
        for (int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ? thisParts[i].toInteger() : 0
            int thatPart = i < thatParts.length ? thatParts[i].toInteger() : 0
            if (thisPart < thatPart)
                return -1
            if (thisPart > thatPart)
                return 1
        }
        return 0
    }

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true
        if (that == null)
            return false
        if (this.getClass() != that.getClass())
            return false
        this.compareTo((Version) that) == 0
    }

    @Override
    String toString() {
        version
    }
}
