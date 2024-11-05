// Copyright (c) 2014-2016 Trapeze Group Switzerland
package io.amotech.bleexperimentation.air.stream.bit8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public abstract class ByteInputStream extends InputStream {

    public static ByteInputStreamLE wrapLE(final byte[] data) {
        return new ByteInputStreamLE(new ByteArrayInputStream(data));
    }

    public static ByteInputStreamBE wrapBE(final byte[] data) {
        return new ByteInputStreamBE(new ByteArrayInputStream(data));
    }

    public static ByteInputStreamLE wrapLE(final InputStream stream) {
        return new ByteInputStreamLE(stream);
    }

    public static ByteInputStreamBE wrapBE(final InputStream stream) {
        return new ByteInputStreamBE(stream);
    }

    public static ByteInputStreamLE wrapSafeLE(final byte[] data) {
        final ByteInputStreamLE result = new ByteInputStreamLE(new ByteArrayInputStream(data));
        result.setSafe(true);
        return result;
    }

    public static ByteInputStreamBE wrapSafeBE(final byte[] data) {
        final ByteInputStreamBE result = new ByteInputStreamBE(new ByteArrayInputStream(data));
        result.setSafe(true);
        return result;
    }

    protected final InputStream in;
    private boolean safe; // if true, the requested length will be reduced to fit into what is available

    protected ByteInputStream(final InputStream in) {
        this.in = in;
    }

    protected void setSafe(final boolean flag) {
        safe = flag;
    }

    protected InputStream stream() {
        return in;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public int read() throws IOException {
        int bytes = 1;
        if (safe && bytes > available()) {
            bytes = available();
        }
        return (int) bytes(bytes);
    }

    @Override
    public int read(final byte[] buf, int off, int len) throws IOException {
        if (safe && len > available()) {
            len = available();
        }
        while (len-- != 0) {
            buf[off++] = (byte) bytes(1);
        }
        return 0; // throws exception if unable to read all requested bytes
    }

    @Override
    public int read(final byte[] buf) throws IOException {
        read(buf, 0, buf.length);
        return buf.length;
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    public byte[] newArray(int len) throws IOException {
        if (safe && len > available()) {
            len = available();
        }
        final byte[] result = new byte[len];
        read(result, 0, len);
        return result;
    }

    public String newString(final Charset charset, int len) throws IOException {
        if (safe && len > available()) {
            len = available();
        }
        final byte[] result = new byte[len];
        read(result, 0, len); // read as many bytes as specified
        int i;
        for (i = 0; i != result.length && result[i] != 0; i++) {
            // scanning 1 byte: 0x00 (i.e. read until zero-termination)
        }
        return new String(result, 0, i, charset);
    }

    public byte getSignedByte(int bytes) throws IOException {
        if (safe && bytes > available()) {
            bytes = available();
        }
        final long result = bytes(bytes);
        return (byte) (bytes >= Long.BYTES ? result : (result >>> bytes * Byte.SIZE & 0x80) != 0 ? result | ~((1 << bytes * Byte.SIZE) - 1) : result); // sign expansion
    }

    public byte getUnsignedByte(int bytes) throws IOException {
        if (safe && bytes > available()) {
            bytes = available();
        }
        return (byte) bytes(bytes); // only for 64 bits it will be signed
    }

    public short getSignedShort(int bytes) throws IOException {
        if (safe && bytes > available()) {
            bytes = available();
        }
        final long result = bytes(bytes);
        return (short) (bytes >= Long.BYTES ? result : (result >>> bytes * Byte.SIZE & 0x80) != 0 ? result | ~((1 << bytes * Byte.SIZE) - 1) : result); // sign expansion
    }

    public short getUnsignedShort(int bytes) throws IOException {
        if (safe && bytes > available()) {
            bytes = available();
        }
        return (short) bytes(bytes); // only for 64 bits it will be signed
    }

    public int getSignedInt(int bytes) throws IOException {
        if (safe && bytes > available()) {
            bytes = available();
        }
        final long result = bytes(bytes);
        return (int) (bytes >= Long.BYTES ? result : (result >>> bytes * Byte.SIZE & 0x80) != 0 ? result | ~((1 << bytes * Byte.SIZE) - 1) : result); // sign expansion
    }

    public int getUnsignedInt(int bytes) throws IOException {
        if (safe && bytes > available()) {
            bytes = available();
        }
        return (int) bytes(bytes); // only for 64 bits it will be signed
    }

    public long getSignedLong(int bytes) throws IOException {
        if (safe && bytes > available()) {
            bytes = available();
        }
        final long result = bytes(bytes);
        return bytes >= Long.BYTES ? result : (result >>> bytes * Byte.SIZE & 0x80) != 0 ? result | ~((1 << bytes * Byte.SIZE) - 1) : result; // sign expansion
    }

    public long getUnsignedLong(int bytes) throws IOException {
        if (safe && bytes > available()) {
            bytes = available();
        }
        return bytes(bytes); // only for 64 bits it will be signed
    }

    public float getFloat() throws IOException {
        return Float.intBitsToFloat((int) bytes(Float.BYTES));
    }

    public double getDouble() throws IOException {
        return Double.longBitsToDouble(bytes(Double.BYTES));
    }

    public boolean getBoolean() throws IOException {
        int bytes = 1;
        if (safe && bytes > available()) {
            bytes = available();
        }
        return bytes(bytes) != 0;
    }

    protected abstract long bytes(int bytes) throws IOException;

}
