// Copyright (c) 2014-2016 Trapeze Group Switzerland
package io.amotech.bleexperimentation.air.stream.bit1;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import io.amotech.bleexperimentation.air.utils.SerialisationHelper;

public class BitOutputStreamBE extends BitOutputStream {

    protected BitOutputStreamBE(final OutputStream out) {
        super(out);
    }

    @Override
    protected void bits(final long data, int bits) throws IOException {
        if (bits < 0 || bits > Long.SIZE) {
            throw new IOException("trying to write more bits than supported");
        }
        while (bits >= available) { // need to write after filling the available bits
            bits -= available; // optimization is to keep track before actually consuming
            buffer |= data >>> bits & (1 << available) - 1;
            out.write((int) buffer);
            buffer = 0;
            available = Byte.SIZE;
        }
        if (bits > 0) { // fill as much as needed
            available -= bits; // optimization is to keep track before actually filling
            buffer |= (data & (1 << bits) - 1) << available;
        }
    }

    public static void main(final String[] args) throws IOException {
        final PrintStream console = System.out;
        try (BitInputStreamBE in = BitInputStream.wrapBE(SerialisationHelper.getRaw("abcdef123456789f"))) {
            try (BitOutputStreamBE out = BitOutputStream.createBE(0)) {
                out.putUnsigned(in.getUnsignedLong(1), 1);
                out.putUnsigned(in.getUnsignedLong(2), 2);
                out.putUnsigned(in.getUnsignedLong(3), 3);
                out.putUnsigned(in.getUnsignedLong(4), 4);
                out.putUnsigned(in.getUnsignedLong(5), 5);
                out.putUnsigned(in.getUnsignedLong(6), 6);
                out.putUnsigned(in.getUnsignedLong(7), 7);
                out.putUnsigned(in.getUnsignedLong(8), 8);
                out.putUnsigned(in.getUnsignedLong(9), 9);
                out.putUnsigned(in.getUnsignedLong(10), 10);
                out.putUnsigned(in.getUnsignedLong(2), 2); // read only MSB of last byte => is "1" => byte is 0x80
                console.println(SerialisationHelper.getHex(out.array()));
            }
        }
        try (BitOutputStreamBE out = BitOutputStream.createBE(0)) {
            out.putSigned(1, 1); // MSB is "1" => negative
            out.putSigned(2, 2); // MSB is "1" => negative
            out.putSigned(3, 3);
            out.putSigned(4, 4);
            out.putSigned(5, 5);
            out.putSigned(6, 6);
            out.putSigned(7, 7);
            out.putSigned(8, 8);
            out.putSigned(9, 9);
            out.putSigned(10, 10);
            out.putDouble(47.654321);
            out.putSigned(2, 2);
            try (BitInputStreamBE in = BitInputStream.wrapBE(out.array())) {
                console.println(in.getSignedLong(1));
                console.println(in.getSignedLong(2));
                console.println(in.getSignedLong(3));
                console.println(in.getSignedLong(4));
                console.println(in.getSignedLong(5));
                console.println(in.getSignedLong(6));
                console.println(in.getSignedLong(7));
                console.println(in.getSignedLong(8));
                console.println(in.getSignedLong(9));
                console.println(in.getSignedLong(10));
                console.println(in.getDouble());
                console.println(in.getSignedLong(2));
            }
        }
    }
}
