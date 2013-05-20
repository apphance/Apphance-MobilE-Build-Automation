package com.apphance.ameba.plugins.android.release

import com.apphance.ameba.plugins.android.builder.AndroidBuilderInfo

/**
 * Listener that can be plugged in android build. It will be fired after every variant is built.
 *
 */
interface AndroidBuildListener {

    void buildDone(AndroidBuilderInfo bi)
}
