package com.apphance.ameba.unit

import com.vladium.emma.rt.RT;

class EmmaDumper {
    static dumpEmmaCoverage() {
        String outFileName = System.getProperty("emma.coverage.out.file")
        if (outFileName != null) {
            RT.dumpCoverageData(new File(outFileName), true, false)
        }
    }

}
