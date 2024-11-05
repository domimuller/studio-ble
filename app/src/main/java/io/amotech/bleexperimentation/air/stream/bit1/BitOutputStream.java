// Copyright (c) 2014-2016 Trapeze Group Switzerland
package io.amotech.bleexperimentation.air.stream.bit1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public abstract class BitOutputStream extends OutputStream {

    public static BitOutputStreamBE createBE(final int size) { // size in bytes
        return new BitOutputStreamBE(size != 0 ? new ByteArrayOutputStream(size) : new ByteArrayOutputStream());
    }

    public static BitOutputStreamLE createLE(final int size) { // size in bytes
        return new BitOutputStreamLE(size != 0 ? new ByteArrayOutputStream(size) : new ByteArrayOutputStream());
    }

    public static BitOutputStreamBE wrapBE(final OutputStream stream) {
        return new BitOutputStreamBE(stream);
    }

    public static BitOutputStreamLE wrapLE(final OutputStream stream) {
        return new BitOutputStreamLE(stream);
    }

    protected final OutputStream out;
    protected int available;
    protected long buffer;

    protected BitOutputStream(final OutputStream out) {
        this.out = out;
        available = Byte.SIZE;
    }

    protected OutputStream stream() {
        return out;
    }

    public byte[] array() {
        return out instanceof ByteArrayOutputStream ? ((ByteArrayOutputStream) out).toByteArray() : null;
    }

    @Override
    public void close() throws IOException {
        flush();
        out.close();
    }

    @Override
    public void write(final int data) throws IOException {
        bits(data, Byte.SIZE);
    }

    @Override
    public void write(final byte[] buf, int off, int len) throws IOException {
        while (len-- != 0) {
            bits(buf[off++], Byte.SIZE);
        }
        // throws exception if unable to write all requested bytes
    }

    @Override
    public void write(final byte[] buf) throws IOException {
        write(buf, 0, buf.length);
    }

    @Override
    public void flush() throws IOException {
        if (available != Byte.SIZE) { // something pending
            out.write((int) buffer);
        }
        out.flush();
    }

    public void putArray(final byte[] array, final int len) throws IOException {
        write(array, 0, len);
    }

    public void putString(final String string, final Charset charset, final int len) throws IOException {
        final byte[] value = string != null ? string.getBytes(charset) : new byte[0];
        final byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            if (i < value.length) {
                result[i] = value[i];
            } else {
                result[i] = (byte) 0; // fill with zeros (is this required at all?)
            }
        }
        write(result);
    }

    public void putSigned(final long data, final int bits) throws IOException {
        bits(data, bits); // no need to revert sign expansion
    }

    public void putUnsigned(final long data, final int bits) throws IOException {
        bits(data, bits);
    }

    public void putFloat(final float data) throws IOException {
        bits(Float.floatToRawIntBits(data), Float.SIZE);
    }

    public void putDouble(final double data) throws IOException {
        bits(Double.doubleToRawLongBits(data), Double.SIZE);
    }

    public void putBoolean(final boolean data) throws IOException {
        bits(data ? 1 : 0, 1);
    }

    protected abstract void bits(long data, int bits) throws IOException;

}
