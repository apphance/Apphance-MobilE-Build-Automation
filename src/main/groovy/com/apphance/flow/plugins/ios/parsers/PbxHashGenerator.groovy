package com.apphance.flow.plugins.ios.parsers

import java.util.concurrent.atomic.AtomicInteger

import static java.security.MessageDigest.getInstance

class PbxHashGenerator {

    private static final AtomicInteger ATOMIC_HASH_SEED = new AtomicInteger()

    String hash() {
        md5(ATOMIC_HASH_SEED.incrementAndGet().toString()).toUpperCase()
    }

    private String md5(String s) {
        def digest = getInstance('MD5')
        digest.update(s.bytes);
        new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')
    }
}
