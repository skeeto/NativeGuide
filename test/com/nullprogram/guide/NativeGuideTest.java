package com.nullprogram.guide;

import static com.nullprogram.guide.NativeGuide.*;
import junit.framework.TestCase;

public class NativeGuideTest extends TestCase {

    public final void testIsArchitecture() {
        String root = "/com/nullprogram/guide/";
        try {
            prepare(LINUX_32,   root + "linux32/libguide.so");
            prepare(LINUX_64,   root + "linux64/libguide.so");
            prepare(WINDOWS_32, root + "windows32/guide.dll");
            System.loadLibrary("guide");
        } catch (java.io.IOException e) {
            fail("Could not prepare library: " + e);
        }
    }
}
