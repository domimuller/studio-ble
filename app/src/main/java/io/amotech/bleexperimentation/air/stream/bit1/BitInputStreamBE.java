// Copyright (c) 2014-2016 Trapeze Group Switzerland
package io.amotech.bleexperimentation.air.stream.bit1;

import java.io.IOException;
import java.io.InputStream;

import io.amotech.bleexperimentation.air.utils.SerialisationHelper;

public class BitInputStreamBE extends BitInputStream {

    protected BitInputStreamBE(final InputStream in) {
        super(in);
    }

    @Override
    protected long bits(int bits) throws IOException {
        if (bits < 0 || bits > Long.SIZE) {
            throw new IOException("trying to read more bits than supported");
        }
        long result = 0;
        while (bits > available) { // need to read after consuming the available bits
            bits -= available; // optimization is to keep track before actually consuming
            result |= buffer << bits;
            if ((buffer = in.read()) == -1) {
                throw new IOException("end of stream");
            }
            available = Byte.SIZE;
        }
        if (bits > 0) { // consume as much as needed
            available -= bits; // optimization is to keep track before actually consuming
            result |= buffer >>> available;
            buffer &= (1 << available) - 1;
        }
        return result;
    }

    public static void main(final String[] args) throws IOException {
        BitInputStreamBE in = BitInputStream.wrapBE(SerialisationHelper.getRaw("abcdef1234567890"));
        System.out.println(in.getSignedLong(6));
        System.out.println(in.getSignedLong(4));
        in = BitInputStream.wrapBE(SerialisationHelper.getRaw("0f90ea"));
        System.out.println(in.getSignedLong(16));
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
