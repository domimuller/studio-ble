// Copyright (c) 2014-2016 Trapeze Group Switzerland
package io.amotech.bleexperimentation.air.utils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Scanner;

public class SafeHelper {

    public static final String safeString(final byte[] bytes, final Charset charset) {
        try {
            return new String(bytes, charset);
        } catch (final Exception e) {
            return null;
        }
    }

    public static String safeString(final Object value) {
        try {
            return value != null ? value.toString() : null;
        } catch (final Exception e) {
            return null;
        }
    }

    public static boolean safeBool(final String value, final boolean defaultValue) {
        try {
            return Boolean.parseBoolean(value);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    public static int safeInt(final String value, final int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    public static long safeLong(final String value, final long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    public static float safeFloat(final String value, final float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    public static float safeFloatOrNaN(final String value) {
        try {
            return Float.parseFloat(value);
        } catch (final Exception e) {
            return Float.NaN;
        }
    }

    public static double safeDouble(final String value, final double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    public static double safeDoubleOrNan(final String value) {
        try {
            return Double.parseDouble(value);
        } catch (final Exception e) {
            return Double.NaN;
        }
    }

    public static final boolean safeClose(final AutoCloseable closeable) {
        try {
            closeable.close();
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    public static final boolean safeClose(final Closeable closeable) {
        try {
            closeable.close();
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    public static boolean safeSleep(final long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (final InterruptedException e) {
            return false;
        }
    }

    public static String safeUrl(final String url) {
        try {
            return url != null ? URLEncoder.encode(url, "UTF-8") : null;
        } catch (final UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String decodeUrl(final String url) {
        try {
            return url != null ? URLDecoder.decode(url, "UTF-8") : null;
        } catch (final UnsupportedEncodingException e) {
            return null;
        }
    }

    public static URI safeURI(final String uri) {
        try {
            return uri != null ? new URI(uri) : null;
        } catch (final URISyntaxException e) {
            return null;
        }
    }

    public static InetAddress safeAddress(final String addr) {
        try {
            return addr != null ? InetAddress.getByName(addr) : null;
        } catch (final UnknownHostException e) {
            return null;
        }
    }

    public static void safeJoin(final Thread thread) {
        if (thread != null && thread.isAlive()) {
            try {
                thread.interrupt(); // double stitching
                thread.join(); // wait for thread to terminate
            } catch (final Exception e) {
            }
        }
    }

    public static byte[] safeBytes(final String string, final Charset charset) {
        try {
            return string.getBytes(charset);
        } catch (final Exception e) {
            return new byte[0];
        }
    }

    public static Charset safeCharset(final String charsetName, final Charset defaultCharset) {
        try {
            return Charset.forName(charsetName.toLowerCase(Locale.US).replaceAll("_", "-").replaceAll("\\s", ""));// replace underscores by dashes, remove blanks
        } catch (final Exception e) {
            return defaultCharset;
        }
    }

    public static ZoneId safeZoneId(final String timezone) {
        try {
            return timezone != null ? ZoneId.of(timezone) : null;
        } catch (final Exception e) {
            return null;
        }
    }

    public static String safeTimezone(final ZoneId zoneId) {
        return zoneId != null ? zoneId.toString() : null;
    }

    public static Locale safeLocale(final String localeName) {
        try {
            return Locale.forLanguageTag(localeName);
        } catch (final Exception e) {
            return null;
        }
    }

    public static void safeWait(final String symbol) {
        // wait for user input
        System.out.println(String.format("enter \"%s\" to stop", symbol));
        String cmd = "";
        try {
            final Scanner sc = new Scanner(System.in);
            do {
                cmd = sc.nextLine().toLowerCase();
            } while (!cmd.equals(symbol));
            sc.close();
        } catch (final Exception e) {
            // ignore
        }
    }

    public static boolean safeDelete(final File file) {
        try {
            Files.delete(file.toPath());
            return true;
        } catch (final IOException e) {
            return false;
        }
    }

    public static void safeWait() {
        System.out.println("press Ctrl+C or send SIGTERM to terminate");
        boolean keepRunning = true;
        while (keepRunning) {
            try {
                Thread.sleep(500);
            } catch (final InterruptedException e) {
                keepRunning = false;
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("terminating");
    }

    public static void main(final String[] args) {
        System.out.println(System.getProperty("os.name"));
    }

}
