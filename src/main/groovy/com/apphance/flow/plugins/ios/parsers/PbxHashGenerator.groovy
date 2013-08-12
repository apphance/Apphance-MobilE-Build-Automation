package com.apphance.flow.plugins.ios.parsers

import java.util.concurrent.atomic.AtomicInteger

import static java.security.MessageDigest.getInstance

class PbxHashGenerator {

    private AtomicInteger hash = new AtomicInteger()

    String hash() {
        md5(hash.incrementAndGet().toString()).toUpperCase()
    }

    private String md5(String s) {
        def digest = getInstance('MD5')
        digest.update(s.bytes);
        new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')
    }
}
