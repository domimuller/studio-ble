// Copyright (c) 2014-2016 Trapeze Group Switzerland
package io.amotech.bleexperimentation.air.stream.bit8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public abstract class ByteOutputStream extends OutputStream {

    public static ByteOutputStreamBE createBE(final int size) {
        return new ByteOutputStreamBE(size != 0 ? new ByteArrayOutputStream(size) : new ByteArrayOutputStream());
    }

    public static ByteOutputStreamLE createLE(final int size) {
        return new ByteOutputStreamLE(size != 0 ? new ByteArrayOutputStream(size) : new ByteArrayOutputStream());
    }

    public static ByteOutputStreamBE wrapBE(final OutputStream stream) {
        return new ByteOutputStreamBE(stream);
    }

    public static ByteOutputStreamLE wrapLE(final OutputStream stream) {
        return new ByteOutputStreamLE(stream);
    }

    protected final OutputStream out;

    protected ByteOutputStream(final OutputStream out) {
        this.out = out;
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
        bytes(data, 1);
    }

    @Override
    public void write(final byte[] buf, int off, int len) throws IOException {
        while (len-- != 0) {
            bytes(buf[off++], 1);
        }
        // throws exception if unable to write all requested bytes
    }

    @Override
    public void write(final byte[] buf) throws IOException {
        write(buf, 0, buf.length);
    }

    @Override
    public void flush() throws IOException {
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
                result[i] = (byte) 0; // fill with zeros (is this needed at all?)
            }
        }
        write(result);
    }

    public void putSigned(final long data, final int bytes) throws IOException {
        bytes(data, bytes); // no need to revert sign expansion
    }

    public void putUnsigned(final long data, final int bytes) throws IOException {
        bytes(data, bytes);
    }

    public void putFloat(final float data) throws IOException {
        bytes(Float.floatToRawIntBits(data), Float.BYTES);
    }

    public void putDouble(final double data) throws IOException {
        bytes(Double.doubleToRawLongBits(data), Double.BYTES);
    }

    public void putBoolean(final boolean data) throws IOException {
        bytes(data ? 255 : 0, 1);
    }

    protected abstract void bytes(long data, int bytes) throws IOException;

}
