// Copyright (c) 2014-2016 Trapeze Group Switzerland
package io.amotech.bleexperimentation.air.utils;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SerialisationHelper {

    private static final int BUFFER_SIZE = 32768;

    static private final String HEX_CHARS = "0123456789ABCDEF";

    // put max <size> bytes of String <val> to <buf>
    public static void putBytes(final ByteBuffer buf, final byte[] value, final int size) {
        for (int i = 0; i < size; i++) {
            if (i < value.length) {
                buf.put(value[i]);
            } else {
                buf.put((byte) 0); // fill with zeros
            }
        }
    }

    // get <size> bytes of String <val> to <buf>
    public static byte[] getBytes(final ByteBuffer buf, final int size) {
        final byte[] value = new byte[size];
        buf.get(value); // read as many bytes as specified
        return value;
    }

    // put max <size> bytes of String <val> to <buf>
    public static void putString(final Charset charset, final ByteBuffer buf, final String value, final int size) {
        final byte[] valueBuf = value.getBytes(charset);
        for (int i = 0; i < size; i++) {
            if (i < valueBuf.length) {
                buf.put(valueBuf[i]);
            } else {
                buf.put((byte) 0); // fill with zeros
            }
        }
    }

    // put max <size> bytes of String <val> to <buf>
    public static String getString(final Charset charset, final ByteBuffer buf, final int size) {
        final byte[] value = new byte[size];
        buf.get(value); // read as many bytes as specified
        int i;
        for (i = 0; i < value.length && value[i] != 0; i++) {
            // scanning 1 byte: 0x00 (i.e. read until zero-termination)
        }
        return new String(value, 0, i, charset);
    }

    // put max <size> bytes of String <val> to <buf>
    public static String getTrimmedStringUtf16LE(final ByteBuffer buf, final int size) {
        final byte[] value = new byte[size];
        buf.get(value); // read as many bytes as specified
        int i;
        for (i = 0; i + 1 < value.length && (value[i] != 0 || value[i + 1] != 0); i += 2) {
            // scanning 2 bytes: 0x00 0x00 (i.e. read until zero-termination)
        }
        int j;
        for (j = i; j > 0 && value[j - 2] == 0 && value[j - 1] == 32; j -= 2) {
            // trimming 2 bytes: 0x00 0x20 (i.e. trim trailing spaces)
        }
        return new String(value, 0, j, StandardCharsets.UTF_16LE);
    }

    // put int <val> of <size> bytes to <buf> LITTLE ENDIAN
    public static void putLittleEndianInt(final ByteBuffer buf, final int value, final int size) {
        if (size >= 1) {
            buf.put((byte) value);
        }
        if (size >= 2) {
            buf.put((byte) (value >>> 8));
        }
        if (size >= 3) {
            buf.put((byte) (value >>> 16));
        }
        if (size >= 4) {
            buf.put((byte) (value >>> 24));
        }
    }

    // get int of <size> bytes from <buf> LITTLE ENDIAN
    public static int getLittleEndianInt(final ByteBuffer buf, final int size) {
        return (size >= 1 ? buf.get() & 0xFF : 0) | (size >= 2 ? (buf.get() & 0xFF) << 8 : 0) | (size >= 3 ? (buf.get() & 0xFF) << 16 : 0) | (size >= 4 ? (buf.get() & 0xFF) << 24 : 0);
    }

    // put int <val> of <size> bytes to <buf> LITTLE ENDIAN
    public static void putLittleEndianLong(final ByteBuffer buf, final long value, final int size) {
        if (size <= 4) {
            SerialisationHelper.putLittleEndianInt(buf, (int) value, size);
        } else {
            SerialisationHelper.putLittleEndianInt(buf, (int) value, 4);
            SerialisationHelper.putLittleEndianInt(buf, (int) (value >>> 32), size - 4);
        }
    }

    // get long of <size> bytes from <buf> LITTLE ENDIAN
    public static long getLittleEndianLong(final ByteBuffer buf, final int size) {
        // return size <= 4 ? getLittleEndianInt(buf, size) : ((long) getLittleEndianInt(buf, 4)) | (((long) getLittleEndianInt(buf, size - 4)) << 32);
        return (size >= 1 ? buf.get() & 0xFFL : 0L) | (size >= 2 ? (buf.get() & 0xFFL) << 8 : 0L) | (size >= 3 ? (buf.get() & 0xFFL) << 16 : 0L) | (size >= 4 ? (buf.get() & 0xFFL) << 24 : 0L)
                | (size >= 5 ? (buf.get() & 0xFFL) << 32 : 0L) | (size >= 6 ? (buf.get() & 0xFFL) << 40 : 0L) | (size >= 7 ? (buf.get() & 0xFFL) << 48 : 0L)
                | (size >= 8 ? (buf.get() & 0xFFL) << 56 : 0L);
    }

    // put int <val> of <size> bytes to <buf> BIG ENDIAN
    public static void putBigEndianInt(final ByteBuffer buf, final int value, final int size) {
        if (size >= 4) {
            buf.put((byte) (value >>> 24));
        }
        if (size >= 3) {
            buf.put((byte) (value >>> 16));
        }
        if (size >= 2) {
            buf.put((byte) (value >>> 8));
        }
        if (size >= 1) {
            buf.put((byte) value);
        }
    }

    // get int of <size> bytes from <buf> BIG ENDIAN
    public static int getBigEndianInt(final ByteBuffer buf, final int size) {
        return (size >= 4 ? (buf.get() & 0xFF) << 24 : 0) | (size >= 3 ? (buf.get() & 0xFF) << 16 : 0) | (size >= 2 ? (buf.get() & 0xFF) << 8 : 0) | (size >= 1 ? buf.get() & 0xFF : 0);
    }

    // put int <val> of <size> bytes to <buf> BIG ENDIAN
    public static void putBigEndianLong(final ByteBuffer buf, final long value, final int size) {
        if (size <= 4) {
            SerialisationHelper.putBigEndianInt(buf, (int) value, size);
        } else {
            SerialisationHelper.putBigEndianInt(buf, (int) (value >>> 32), size - 4);
            SerialisationHelper.putBigEndianInt(buf, (int) value, 4);
        }
    }

    // get long of <size> bytes from <buf> BIG ENDIAN
    public static long getBigEndianLong(final ByteBuffer buf, final int size) {
        // return size <= 4 ? getLittleEndianInt(buf, size) : ((long) getLittleEndianInt(buf, 4)) | (((long) getLittleEndianInt(buf, size - 4)) << 32);
        return (size >= 8 ? (buf.get() & 0xFFL) << 56 : 0L) | (size >= 7 ? (buf.get() & 0xFFL) << 48 : 0L) | (size >= 6 ? (buf.get() & 0xFFL) << 40 : 0L) | (size >= 5 ? (buf.get() & 0xFFL) << 32 : 0L)
                | (size >= 4 ? (buf.get() & 0xFFL) << 24 : 0L) | (size >= 3 ? (buf.get() & 0xFFL) << 16 : 0L) | (size >= 2 ? (buf.get() & 0xFFL) << 8 : 0L) | (size >= 1 ? buf.get() & 0xFFL : 0L);
    }

    // get bit of int
    public static boolean getBitInt(final int target, final int bit) {
        return (target >>> bit & 1) == 1;
    }

    // set bit of int
    public static int setBitInt(final int target, final int bit, final boolean flag) {
        return flag ? target | 1 << bit : target & ~(1 << bit);
    }

    public static String filterHex(final String hex) {
        final String input = hex.toUpperCase();
        final StringBuilder result = new StringBuilder();
        char c;
        for (int i = 0; i < input.length(); i++) {
            c = input.charAt(i);
            if (SerialisationHelper.HEX_CHARS.indexOf(c) != -1) {
                result.append(c);
            }
        }
        return result.toString();
    }

    @SuppressWarnings("unused")
    private static String testHex(final String hex) {
        return SerialisationHelper.getHex(SerialisationHelper.getRaw(SerialisationHelper.filterHex(hex)));
    }

    @SuppressWarnings("unused")
    private static String testRaw(final String raw) {
        return SerialisationHelper.filterHex(SafeHelper.safeString(SerialisationHelper.getRaw(SerialisationHelper.getHex(raw.getBytes())), StandardCharsets.UTF_8));
    }

    public static String getHex(final byte[] raw) {
        return raw != null ? SerialisationHelper.getHex(raw, raw.length) : null;
    }

    public static String getHex(final byte[] raw, int size) {
        if (raw == null) {
            return null;
        }
        if (size == 0) {
            size = raw.length;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            if (size-- > 0) {
                hex.append(SerialisationHelper.HEX_CHARS.charAt((b & 0xF0) >> 4)).append(SerialisationHelper.HEX_CHARS.charAt(b & 0x0F));
            }
        }
        return hex.toString();
    }

    public static byte[] getRaw(final String hex) {
        int len = hex != null ? hex.length() : 0;
        if (len % 2 == 1) {
            len -= 1; // skip last character if length is not even
        }
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public static void serializationTest() {
        ByteBuffer buf = ByteBuffer.allocate(46);
        SerialisationHelper.putLittleEndianInt(buf, 0xA1B2C3D4, 1);
        SerialisationHelper.putLittleEndianInt(buf, 0xA1B2C3D4, 2);
        SerialisationHelper.putLittleEndianInt(buf, 0xA1B2C3D4, 3);
        SerialisationHelper.putLittleEndianInt(buf, 0xA1B2C3D4, 4);
        SerialisationHelper.putLittleEndianLong(buf, 0xA1B2C3D4E5F60718L, 1);
        SerialisationHelper.putLittleEndianLong(buf, 0xA1B2C3D4E5F60718L, 2);
        SerialisationHelper.putLittleEndianLong(buf, 0xA1B2C3D4E5F60718L, 3);
        SerialisationHelper.putLittleEndianLong(buf, 0xA1B2C3D4E5F60718L, 4);
        SerialisationHelper.putLittleEndianLong(buf, 0xA1B2C3D4E5F60718L, 5);
        SerialisationHelper.putLittleEndianLong(buf, 0xA1B2C3D4E5F60718L, 6);
        SerialisationHelper.putLittleEndianLong(buf, 0xA1B2C3D4E5F60718L, 7);
        SerialisationHelper.putLittleEndianLong(buf, 0xA1B2C3D4E5F60718L, 8);
        byte[] array = buf.array();
        buf = ByteBuffer.wrap(array);
        System.out.println("serializationTest len=" + array.length + " raw=" + SerialisationHelper.getHex(array, array.length));
        System.out.println("serializationTest int1=" + String.format("%08X", SerialisationHelper.getLittleEndianInt(buf, 1)));
        System.out.println("serializationTest int2=" + String.format("%08X", SerialisationHelper.getLittleEndianInt(buf, 2)));
        System.out.println("serializationTest int3=" + String.format("%08X", SerialisationHelper.getLittleEndianInt(buf, 3)));
        System.out.println("serializationTest int4=" + String.format("%08X", SerialisationHelper.getLittleEndianInt(buf, 4)));
        System.out.println("serializationTest lon1=" + String.format("%016X", SerialisationHelper.getLittleEndianLong(buf, 1)));
        System.out.println("serializationTest lon2=" + String.format("%016X", SerialisationHelper.getLittleEndianLong(buf, 2)));
        System.out.println("serializationTest lon3=" + String.format("%016X", SerialisationHelper.getLittleEndianLong(buf, 3)));
        System.out.println("serializationTest lon4=" + String.format("%016X", SerialisationHelper.getLittleEndianLong(buf, 4)));
        System.out.println("serializationTest lon5=" + String.format("%016X", SerialisationHelper.getLittleEndianLong(buf, 5)));
        System.out.println("serializationTest lon6=" + String.format("%016X", SerialisationHelper.getLittleEndianLong(buf, 6)));
        System.out.println("serializationTest lon7=" + String.format("%016X", SerialisationHelper.getLittleEndianLong(buf, 7)));
        System.out.println("serializationTest lon8=" + String.format("%016X", SerialisationHelper.getLittleEndianLong(buf, 8)));
        buf = ByteBuffer.allocate(46);
        SerialisationHelper.putBigEndianInt(buf, 0xA1B2C3D4, 1);
        SerialisationHelper.putBigEndianInt(buf, 0xA1B2C3D4, 2);
        SerialisationHelper.putBigEndianInt(buf, 0xA1B2C3D4, 3);
        SerialisationHelper.putBigEndianInt(buf, 0xA1B2C3D4, 4);
        SerialisationHelper.putBigEndianLong(buf, 0xA1B2C3D4E5F60718L, 1);
        SerialisationHelper.putBigEndianLong(buf, 0xA1B2C3D4E5F60718L, 2);
        SerialisationHelper.putBigEndianLong(buf, 0xA1B2C3D4E5F60718L, 3);
        SerialisationHelper.putBigEndianLong(buf, 0xA1B2C3D4E5F60718L, 4);
        SerialisationHelper.putBigEndianLong(buf, 0xA1B2C3D4E5F60718L, 5);
        SerialisationHelper.putBigEndianLong(buf, 0xA1B2C3D4E5F60718L, 6);
        SerialisationHelper.putBigEndianLong(buf, 0xA1B2C3D4E5F60718L, 7);
        SerialisationHelper.putBigEndianLong(buf, 0xA1B2C3D4E5F60718L, 8);
        array = buf.array();
        buf = ByteBuffer.wrap(array);
        System.out.println("serializationTest len=" + array.length + " raw=" + SerialisationHelper.getHex(array, array.length));
        System.out.println("serializationTest int1=" + String.format("%08X", SerialisationHelper.getBigEndianInt(buf, 1)));
        System.out.println("serializationTest int2=" + String.format("%08X", SerialisationHelper.getBigEndianInt(buf, 2)));
        System.out.println("serializationTest int3=" + String.format("%08X", SerialisationHelper.getBigEndianInt(buf, 3)));
        System.out.println("serializationTest int4=" + String.format("%08X", SerialisationHelper.getBigEndianInt(buf, 4)));
        System.out.println("serializationTest lon1=" + String.format("%016X", SerialisationHelper.getBigEndianLong(buf, 1)));
        System.out.println("serializationTest lon2=" + String.format("%016X", SerialisationHelper.getBigEndianLong(buf, 2)));
        System.out.println("serializationTest lon3=" + String.format("%016X", SerialisationHelper.getBigEndianLong(buf, 3)));
        System.out.println("serializationTest lon4=" + String.format("%016X", SerialisationHelper.getBigEndianLong(buf, 4)));
        System.out.println("serializationTest lon5=" + String.format("%016X", SerialisationHelper.getBigEndianLong(buf, 5)));
        System.out.println("serializationTest lon6=" + String.format("%016X", SerialisationHelper.getBigEndianLong(buf, 6)));
        System.out.println("serializationTest lon7=" + String.format("%016X", SerialisationHelper.getBigEndianLong(buf, 7)));
        System.out.println("serializationTest lon8=" + String.format("%016X", SerialisationHelper.getBigEndianLong(buf, 8)));
    }

    public static void main(final String[] args) throws Exception {
        SerialisationHelper.serializationTest();
        System.out.println(SerialisationHelper.bcd2int(0x98765432));
        System.out.println(String.format("%x", SerialisationHelper.int2bcd(98765432)));
        System.out.println(SerialisationHelper.bcd2long(0x9876543298765432L));
        System.out.println(String.format("%x", SerialisationHelper.long2bcd(9876543298765432L)));
    }

    public static int bcd2int(int bcd) {
        int result = 0;
        int factor = 1;
        while (bcd != 0) {
            result += factor * (bcd & 0xf);
            bcd >>>= 4;
            factor *= 10;
        }
        return result;
    }

    public static int int2bcd(int input) {
        int result = 0;
        int factor = 1;
        while (input != 0) {
            result += factor * (input % 10);
            input /= 10;
            factor *= 16;
        }
        return result;
    }

    public static long bcd2long(long bcd) {
        long result = 0;
        long factor = 1;
        while (bcd != 0) {
            result += factor * (bcd & 0xf);
            bcd >>>= 4;
            factor *= 10;
        }
        return result;
    }

    public static long long2bcd(long input) {
        long result = 0;
        long factor = 1;
        while (input != 0) {
            result += factor * (input % 10);
            input /= 10;
            factor *= 16;
        }
        return result;
    }

    public static byte[] ip2bytes(final int ip) {
        final ByteBuffer result = ByteBuffer.allocate(4);
        SerialisationHelper.putBigEndianInt(result, ip, 4);
        return result.array();
    }

    public static Inet4Address ip2addr(final int ip) {
        try {
            return (Inet4Address) InetAddress.getByAddress(SerialisationHelper.ip2bytes(ip));
        } catch (final UnknownHostException e) {
            return null;
        }
    }

    public static int addr2ip(final Inet4Address addr) {
        return SerialisationHelper.getBigEndianInt(ByteBuffer.wrap(addr.getAddress()), 4);
    }

    public static void write(final OutputStream out, final byte[] buffer) {
        ByteArrayInputStream in = null;
        try {
            in = new ByteArrayInputStream(buffer);
            SerialisationHelper.transfer(in, out, buffer.length);
        } catch (final Exception e) {
            Log.w("write", "something went wrong", e);
        } finally {
            SafeHelper.safeClose(in);
        }
    }

    public static void store(final byte[] buffer, final File file) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            write(out, buffer);
        } catch (final Exception e) {
            Log.w("store", "something went wrong", e);
        } finally {
            SafeHelper.safeClose(out);
        }
    }

    public static void store(final InputStream in, final File file) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            SerialisationHelper.transfer(in, out);
        } catch (final Exception e) {
            Log.w("store", "something went wrong", e);
        } finally {
            SafeHelper.safeClose(out);
        }
    }

    public static void store(final String s, final File file) {
        Writer writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(s);
        } catch (final Exception e) {
            Log.w("store", "something went wrong", e);
        } finally {
            SafeHelper.safeClose(writer);
        }
    }

    public static byte[] read(final InputStream is) {
        byte[] result = null;
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream(SerialisationHelper.BUFFER_SIZE); // initial size, will expand if needed
            SerialisationHelper.transfer(is, os);
            result = os.toByteArray();
            // } catch (final IOException e) {
            // result = os.toByteArray();
        } catch (final Exception e) {
            Log.w("read", "something went wrong", e);
        } finally {
            SafeHelper.safeClose(os);
        }
        return result;
    }

    public static byte[] read(final InputStream is, final int size) {
        byte[] result = null;
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream(size); // initial size, will expand if needed
            SerialisationHelper.transfer(is, os, size);
            result = os.toByteArray();
            // } catch (final IOException e) {
            // result = os.toByteArray();
        } catch (final Exception e) {
            Log.w("read", "something went wrong", e);
        } finally {
            SafeHelper.safeClose(os);
        }
        return result;
    }

    public static byte[] read(final File file) {
        byte[] result = null;
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            result = SerialisationHelper.read(is, (int) file.length());
        } catch (final FileNotFoundException e) {
            // ignore
        } catch (final Exception e) {
            Log.w("read", "something went wrong", e);
        } finally {
            SafeHelper.safeClose(is);
        }
        return result;
    }

    public static List<String> lines(final File file, final Charset charset) {
        try {
            return Files.readAllLines(file.toPath(), charset);
        } catch (final Exception e) {
            Log.w("lines", "something went wrong", e);
            return Collections.emptyList();
        }
    }

    public static void read(final InputStream stream, final byte[] array) throws IOException {
        SerialisationHelper.read(stream, array, 0, array.length);
    }

    public static int read(final InputStream stream, final byte[] array, int pos, int len) throws IOException {
        int n; // the bytes read in a particular read operation
        while (0 < len && (n = stream.read(array, pos, len)) != -1) {
            pos += n;
            len -= n;
        }
        if (len == 0) {
            return pos;
        } else {
            throw new IOException("broken stream");
        }
    }

    public static void transfer(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[SerialisationHelper.BUFFER_SIZE];
        int n;
        while ((n = in.read(buffer, 0, SerialisationHelper.BUFFER_SIZE)) > 0) {
            out.write(buffer, 0, n);
        }
        out.flush();
    }

    public static void transfer(final InputStream in, final OutputStream out, final int size) throws IOException {
        final byte[] buffer = new byte[size];
        int n;
        int count = 0;
        while ((n = in.read(buffer, 0, size - count)) > 0) {
            count += n;
            out.write(buffer, 0, n);
        }
        out.flush();
    }

    public static byte[] gzip(final String input) {
        return SerialisationHelper.gzip(input.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] gzip(final byte[] data) {
        if (data == null) {
            return null;
        }
        ByteArrayOutputStream byteStream = null;
        GZIPOutputStream zipStream = null;
        byte[] result = null;
        try {
            byteStream = new ByteArrayOutputStream(data.length);
            zipStream = new GZIPOutputStream(byteStream);
            zipStream.write(data, 0, data.length);
            zipStream.finish();
            byteStream.flush();
            result = byteStream.toByteArray(); // must close streams before accessing byte array!!!
        } catch (final IOException e) {
            // ignore
        } catch (final Exception e) {
            Log.w("gzip", "something went wrong", e);
        } finally {
            SafeHelper.safeClose(zipStream);
            SafeHelper.safeClose(byteStream);
        }
        return result;
    }

    public static byte[] gunzip(final byte[] data) {
        if (data == null) {
            return null;
        }
        ByteArrayInputStream zipStream = null;
        GZIPInputStream byteStream = null;
        byte[] result = null;
        try {
            zipStream = new ByteArrayInputStream(data);
            byteStream = new GZIPInputStream(zipStream);
            result = SerialisationHelper.read(byteStream);
        } catch (final IOException e) {
            // ignore
        } catch (final Exception e) {
            Log.w("gzip", "something went wrong", e);
        } finally {
            SafeHelper.safeClose(zipStream);
            SafeHelper.safeClose(byteStream);
        }
        return result;
    }

    public static byte[] getBytes(final String text) {
        return text != null ? text.getBytes(StandardCharsets.UTF_8) : null;
    }

    public static String fromBytes(final byte[] bytes) {
        return bytes != null ? new String(bytes, StandardCharsets.UTF_8) : null;
    }

    public static void skip(final InputStream stream, long skip) throws IOException {
        long n;
        while (skip > 0 && (n = stream.skip(skip)) >= 0) {
            skip -= n;
        }
    }

    public static boolean equals(final byte[] a, final byte[] b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i != a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    public static ByteBuffer key(final int... i) {
        final ByteBuffer buffer = ByteBuffer.allocate(4 * i.length);
        for (final int j : i) {
            buffer.putInt(j);
        }
        buffer.rewind();
        return buffer;
    }

    public static ByteBuffer key(final ByteBuffer buf, final String s) {
        final byte[] array = SafeHelper.safeBytes(s, StandardCharsets.UTF_8);
        final ByteBuffer buffer = ByteBuffer.allocate(buf.capacity() + array.length);
        buffer.put(buf.array());
        buffer.put(array);
        buffer.rewind();
        return buffer;
    }

    public static ByteBuffer key(final ByteBuffer buf, final int i) {
        final ByteBuffer buffer = ByteBuffer.allocate(buf.capacity() + 4);
        buffer.put(buf.array());
        buffer.putInt(i);
        buffer.rewind();
        return buffer;
    }

    public static void safeClose(final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final IOException e) {
            }
        }

    }

}
