// Copyright (c) 2014-2016 Trapeze Group Switzerland
package io.amotech.bleexperimentation.air.stream.bit1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public abstract class BitInputStream extends InputStream {

    public static BitInputStreamLE wrapLE(final byte[] data) {
        return new BitInputStreamLE(new ByteArrayInputStream(data));
    }

    public static BitInputStreamBE wrapBE(final byte[] data) {
        return new BitInputStreamBE(new ByteArrayInputStream(data));
    }

    public static BitInputStreamLE wrapLE(final InputStream stream) {
        return new BitInputStreamLE(stream);
    }

    public static BitInputStreamBE wrapBE(final InputStream stream) {
        return new BitInputStreamBE(stream);
    }

    public static BitInputStreamLE wrapSafeLE(final byte[] data) {
        final BitInputStreamLE result = new BitInputStreamLE(new ByteArrayInputStream(data));
        result.setSafe(true);
        return result;
    }

    public static BitInputStreamBE wrapSafeBE(final byte[] data) {
        final BitInputStreamBE result = new BitInputStreamBE(new ByteArrayInputStream(data));
        result.setSafe(true);
        return result;
    }

    protected final InputStream in;
    protected int available;
    protected long buffer;
    private boolean safe; // if true, the requested length will be reduced to fit into what is available

    protected BitInputStream(final InputStream in) {
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
        int bits = Byte.SIZE;
        if (safe && bits > available()) {
            bits = available();
        }
        return (int) bits(bits);
    }

    @Override
    public int read(final byte[] buf, int off, int len) throws IOException {
        if (safe && len * Byte.SIZE > available()) {
            len = available() / Byte.SIZE;
        }
        while (len-- != 0) {
            buf[off++] = (byte) bits(Byte.SIZE);
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
        return in.available() * Byte.SIZE + available;
    }

    public byte[] newArray(int len) throws IOException {
        if (safe && len * Byte.SIZE > available()) {
            len = available() / Byte.SIZE;
        }
        final byte[] result = new byte[len];
        read(result, 0, len);
        return result;
    }

    public String newString(final Charset charset, int len) throws IOException {
        if (safe && len * Byte.SIZE > available()) {
            len = available() / Byte.SIZE;
        }
        final byte[] result = new byte[len];
        read(result, 0, len); // read as many bytes as specified
        int i;
        for (i = 0; i != result.length && result[i] != 0; i++) {
            // scanning 1 byte: 0x00 (i.e. read until zero-termination)
        }
        return new String(result, 0, i, charset);
    }

    public byte getSignedByte(int bits) throws IOException {
        if (safe && bits > available()) {
            bits = available();
        }
        final long result = bits(bits);
        return (byte) (bits >= Byte.SIZE ? result : (result >>> bits - 1 & 1) != 0 ? result | ~((1 << bits) - 1) : result); // sign expansion
    }

    public byte getUnsignedByte(int bits) throws IOException {
        if (safe && bits > available()) {
            bits = available();
        }
        return (byte) bits(bits); // only for 64 bits it will be signed
    }

    public short getSignedShort(int bits) throws IOException {
        if (safe && bits > available()) {
            bits = available();
        }
        final long result = bits(bits);
        return (short) (bits >= Short.SIZE ? result : (result >>> bits - 1 & 1) != 0 ? result | ~((1L << bits) - 1) : result); // sign expansion
    }

    public short getUnsignedShort(int bits) throws IOException {
        if (safe && bits > available()) {
            bits = available();
        }
        return (short) bits(bits); // only for 64 bits it will be signed
    }

    public int getSignedInt(int bits) throws IOException {
        if (safe && bits > available()) {
            bits = available();
        }
        final long result = bits(bits);
        return (int) (bits >= Integer.SIZE ? result : (result >>> bits - 1 & 1) != 0 ? result | ~((1L << bits) - 1) : result); // sign expansion
    }

    public int getUnsignedInt(int bits) throws IOException {
        if (safe && bits > available()) {
            bits = available();
        }
        return (int) bits(bits); // only for 64 bits it will be signed
    }

    public long getSignedLong(int bits) throws IOException {
        if (safe && bits > available()) {
            bits = available();
        }
        final long result = bits(bits);
        return bits >= Long.SIZE ? result : (result >>> bits - 1 & 1) != 0 ? result | ~((1L << bits) - 1) : result; // sign expansion
    }

    public long getUnsignedLong(int bits) throws IOException {
        if (safe && bits > available()) {
            bits = available();
        }
        return bits(bits); // only for 64 bits it will be signed
    }

    public float getFloat() throws IOException {
        return Float.intBitsToFloat((int) bits(Float.SIZE));
    }

    public double getDouble() throws IOException {
        return Double.longBitsToDouble(bits(Double.SIZE));
    }

    public boolean getBoolean() throws IOException {
        int bits = 1;
        if (safe && bits > available()) {
            bits = available();
        }
        return bits(bits) != 0;
    }

    protected abstract long bits(int bits) throws IOException;

}
