package com.nullprogram.guide;

import com.nullprogram.guide.Arch;
import com.nullprogram.guide.NativeGuide;
import junit.framework.TestCase;

public class NativeGuideTest extends TestCase {

    public final void testIsArchitecture() {
        String root = "/com/nullprogram/guide/";
        try {
            NativeGuide.prepare(Arch.LINUX_32,   root + "linux32/libguide.so");
            NativeGuide.prepare(Arch.LINUX_64,   root + "linux64/libguide.so");
            NativeGuide.prepare(Arch.WINDOWS_32, root + "windows32/guide.dll");
            NativeGuide.prepare(Arch.WINDOWS_64, root + "windows64/guide.dll");
            System.loadLibrary("guide");
        } catch (java.io.IOException e) {
            fail("Could not prepare library: " + e);
        }
    }
}
