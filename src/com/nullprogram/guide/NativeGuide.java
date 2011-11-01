package com.nullprogram.guide;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.logging.Logger;

/**
 * This is a utility class for loading native libraries from the
 * classpath. The JVM is unable to load libraries from inside .jar
 * files. This class works around that by copying them out to a
 * temporary directory on the filesystem.
 */
public final class NativeGuide {

    /** Name of this class's logger. */
    private static final String LOGGER_NAME = NativeGuide.class.getName();

    /** This class's logger. */
    private static final Logger LOG = Logger.getLogger(LOGGER_NAME);

    /** 32-bit Linux. */
    public static final int LINUX_32 = 1;

    /** 64-bit Linux. */
    public static final int LINUX_64 = 2;

    /** 32-bit Microsoft Windows. */
    public static final int WINDOWS_32 = 3;

    /** 64-bit Microsoft Windows. */
    public static final int WINDOWS_64 = 4;

    /** 32-bit Mac OS X. */
    public static final int MAC_32 = 5;

    /** 64-bit Mac OS X. */
    public static final int MAC_64 = 6;

    /** Unknown architecture. */
    public static final int UNKNOWN = 0;

    /** Size of the copying buffer. */
    private static final int BUFFER_SIZE = 1024 * 10;

    /** A 32-bit architecture. */
    private static final int ARCH32 = 32;

    /** A 64-bit architecture. */
    private static final int ARCH64 = 64;

    /** The determined architecture. */
    private static int architecture = UNKNOWN;

    /** Base temporary path for native libaries. */
    private static String base;

    /** Hidden constructor. */
    private NativeGuide() {
    }

    /**
     * Prepare the temporary directory and add it to java.library.path.
     * @throws IOException if the directory could not be prepared
     */
    private static synchronized void setUpTemp() throws IOException {
        File dir = new File(System.getProperty("java.io.tmpdir"),
                            "NativeGuide-" + System.getProperty("user.name"));
        if (dir.exists() && !dir.isDirectory()) {
            throw new IOException("NativeGuide directory is a file");
        } else if (!dir.exists()) {
            dir.mkdir();
        }

        /* Insert this path in java.library.path. */
        String orig = System.getProperty("java.library.path");
        base = dir.getAbsolutePath();
        System.setProperty("java.library.path", orig + ":" + base);

        /* Force reread of java.library.path property. */
        try {
            Field sysPath = ClassLoader.class.getDeclaredField("sys_paths");
            sysPath.setAccessible(true);
            sysPath.set(null, null);
        } catch (Exception e) {
            LOG.severe("Could not modify java.library.path property.");
        }
    }

    /**
     * Load a native library resource by first copying it to a
     * temporary directory.
     * @param arch  the architecture of the library
     * @param path  the path to the library as a resource
     * @throws IOException if the library doesn't exist or could not load
     */
    public static void load(final int arch, final String path)
        throws IOException {
        if (isArchitecture(arch)) {
            setUpTemp();
            System.load(copyToTemp(path).getAbsolutePath());
        }
    }

    /**
     * Prepare a native library to be loaded by copying it to a
     * temporary directory. The directory is added to
     * java.library.path.
     * @param arch  the architecture of the library
     * @param path  the path to the library as a resource
     * @throws IOException if the library does not exist
     */
    public static void prepare(final int arch, final String path)
        throws IOException {
        if (isArchitecture(arch)) {
            setUpTemp();
            copyToTemp(path);
        }
    }

    /**
     * Copy the given resource to the temporary directory.
     * @param path  the path of the resource
     * @return the file to which it was copied
     * @throws IOException if copying failed or the resource was not found
     */
    private static File copyToTemp(final String path) throws IOException {
        String name = new File(path).getName();
        File file = new File(base, name);
        boolean exists = file.isFile();

        InputStream in = NativeGuide.class.getResourceAsStream(path);
        in = new BufferedInputStream(in);
        in.available(); // Triggers exception if resource doesn't exist

        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            byte[] bytes = new byte[BUFFER_SIZE];
            for (int n = 0; n != -1; n = in.read(bytes)) {
                out.write(bytes, 0, n);
            }
        } catch (IOException e) {
            /* On some OSes, we might get an IOException trying to
             * overwrite an existing library file if there is
             * another process using it. If this happens, ignore
             * errors.
             */
            if (!exists) {
                    throw e;
            }
                LOG.info(name + " exists but could not be written.");
        } finally {
            if (out != null) {
                try {
                    out.close();
                    } catch (IOException ioe) {
                    /* Ignore. */
                    LOG.warning(name + " could not close file.");
                }
            }
        }

        /* Try to clean up after the JVM exits. */
        file.deleteOnExit();
        return file;
    }

    /**
     * Determine if the given architecture is the one being run.
     * @param arch  the architecture being queried
     * @return true if the given architecture matches
     */
    private static boolean isArchitecture(final int arch) {
        if (architecture == 0) {
            architecture = getArchitecture();
        }
        return architecture == arch;
    }

    /**
     * Determine the JVM's native architecture.
     * @return the native architecture code
     */
    public static int getArchitecture() {
        int bits = ARCH32;
        if (System.getProperty("os.arch").indexOf("64") != -1) {
            bits = ARCH64;
        }
        String os = System.getProperty("os.name");
        if (os.startsWith("Windows")) {
            if (bits == ARCH32) {
                return WINDOWS_32;
            } else {
                return WINDOWS_64;
            }
        } else if (os.equals("Linux")) {
            if (bits == ARCH32) {
                return LINUX_32;
            } else {
                return LINUX_64;
            }
        } else if (os.equals("Mac OS X")) {
            if (bits == ARCH32) {
                return MAC_32;
            } else {
                return MAC_64;
            }
        } else {
            return UNKNOWN;
        }
    }
}