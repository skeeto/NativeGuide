This is a utility library for loading native libraries from the
classpath. The JVM is unable to load libraries from inside .jar
files. This class works around that by copying them out to a
temporary directory on the filesystem.

When using this utility class, imagine you are registering all
versions of your native library. It would be used like this, for
example,

    NativeGuide.prepare(Arch.LINUX_32, "/x86/libexample.so");
    NativeGuide.prepare(Arch.LINUX_64, "/amd64/libexample.so");
    NativeGuide.prepare(Arch.WINDOWS_32, "/x86/example.dll");
    NativeGuide.prepare(Arch.WINDOWS_64, "/amd64/example.dll");

Libraries not used by the running architecture will be ignored.
