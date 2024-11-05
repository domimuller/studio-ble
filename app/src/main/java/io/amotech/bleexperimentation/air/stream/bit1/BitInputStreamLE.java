// Copyright (c) 2014-2016 Trapeze Group Switzerland
package io.amotech.bleexperimentation.air.stream.bit1;

import java.io.IOException;
import java.io.InputStream;

import io.amotech.bleexperimentation.air.utils.SerialisationHelper;

public class BitInputStreamLE extends BitInputStream {

    protected BitInputStreamLE(final InputStream in) {
        super(in);
    }

    @Override
    protected long bits(int bits) throws IOException {
        if (bits < 0 || bits > Long.SIZE) {
            throw new IOException("trying to read more bits than supported");
        }
        long result = 0;
        int pos = 0;
        while (bits > available) { // need to read after consuming the available bits
            bits -= available; // optimization is to keep track before actually consuming
            result |= buffer << pos;
            pos += available;
            if ((buffer = in.read()) == -1) {
                throw new IOException("end of stream");
            }
            available = Byte.SIZE;
        }
        if (bits > 0) { // consume as much as needed
            available -= bits; // optimization is to keep track before actually consuming
            result |= (buffer & (1 << bits) - 1) << pos;
            buffer >>>= bits;
        }
        return result;
    }

    public static void main(final String[] args) throws IOException {
        BitInputStreamLE in = BitInputStream.wrapLE(SerialisationHelper.getRaw("abcdef1234567890"));
        System.out.println(in.getSignedLong(5)); // 11
        System.out.println(in.getSignedLong(5)); // 13
        System.out.println(in.getSignedInt(19)); // 310259 / -214029
        in = BitInputStream.wrapLE(SerialisationHelper.getRaw("0f90ea"));
        System.out.println(in.getUnsignedLong(16));
        System.out.println(in.getBoolean());
        System.out.println(in.getBoolean());
        System.out.println(in.getBoolean());
        System.out.println(in.getBoolean());
        System.out.println(in.getBoolean());
        System.out.println(in.getBoolean());
        System.out.println(in.getBoolean());
        System.out.println(in.getBoolean());
        in.close();
    }

}
