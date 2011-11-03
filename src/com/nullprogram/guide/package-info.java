/**
 * Utilities for loading distributed native libraries from
 * resources. This allows you to pack your native libraries into your
 * distributed JAR file, making their use completely transparent to
 * the end-user. It also manages the java.library.path property, so no
 * need to hack OS detection in some JAR wrapper ahead of time.
 */
package com.nullprogram.guide;
