// Copyright (c) 2014-2016 Trapeze Group Switzerland
package io.amotech.bleexperimentation.air.stream.bit8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.amotech.bleexperimentation.air.utils.SerialisationHelper;

public class ByteOutputStreamBE extends ByteOutputStream {

    protected ByteOutputStreamBE(final OutputStream out) {
        super(out);
    }

    @Override
    protected void bytes(final long data, int bytes) throws IOException {
        if (bytes < 0 || bytes > Long.BYTES) {
            throw new IOException("trying to write more bytes than supported");
        }
        while (bytes > 0) { // something to write
            bytes -= 1; // optimization is to keep track before actually writing
            out.write((int) (data >>> bytes * Byte.SIZE & 255));
        }
    }

    public static void main(final String[] args) throws IOException {
        try (ByteOutputStreamBE out = ByteOutputStream.createBE(0)) {
            try (ByteInputStreamBE in = ByteInputStream
                    .wrapBE(SerialisationHelper.getRaw("abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890"))) {
                out.putUnsigned(in.getUnsignedLong(1), 1);
                System.out.println("1: " + SerialisationHelper.getHex(out.array()));
                out.putUnsigned(in.getUnsignedLong(2), 2);
                System.out.println("1+2: " + SerialisationHelper.getHex(out.array()));
                out.putUnsigned(in.getUnsignedLong(3), 3);
                System.out.println("1+2+3: " + SerialisationHelper.getHex(out.array()));
                out.putUnsigned(in.getUnsignedLong(4), 4);
                System.out.println("1+2+3+4: " + SerialisationHelper.getHex(out.array()));
                out.putUnsigned(in.getUnsignedLong(5), 5);
                System.out.println("1+2+3+4+5: " + SerialisationHelper.getHex(out.array()));
                out.putUnsigned(in.getUnsignedLong(6), 6);
                System.out.println("1+2+3+4+5+6: " + SerialisationHelper.getHex(out.array()));
                out.putUnsigned(in.getUnsignedLong(7), 7);
                System.out.println("1+2+3+4+5+6+7: " + SerialisationHelper.getHex(out.array()));
                out.putUnsigned(in.getUnsignedLong(8), 8);
                System.out.println("1+2+3+4+5+6+7+8: " + SerialisationHelper.getHex(out.array()));
                out.putUnsigned(in.getUnsignedLong(9), 9);
                System.out.println("1+2+3+4+5+6+7+8+9: " + SerialisationHelper.getHex(out.array()));
                out.putUnsigned(in.getUnsignedLong(10), 10);
                System.out.println("1+2+3+4+5+6+7+8+9+10: " + SerialisationHelper.getHex(out.array()));
                out.putUnsigned(in.getUnsignedLong(2), 2); // read only MSB of last byte => is "1" => byte is 0x80
                System.out.println("1+2+3+4+5+6+7+8+9+10+2: " + SerialisationHelper.getHex(out.array()));
            }
        }
        try (ByteOutputStreamBE out = new ByteOutputStreamBE(new ByteArrayOutputStream())) {
            out.putDouble(47.654321);
            out.putSigned(1, 1); // MSB is "1" => negative
            out.putSigned(2, 2);
            out.putSigned(3, 3);
            out.putSigned(4, 4);
            out.putSigned(5, 5);
            out.putSigned(6, 6);
            out.putSigned(7, 7);
            out.putSigned(8, 8);
            out.putSigned(9, 9);
            out.putSigned(10, 10);
            out.putSigned(2, 2);
            try (ByteInputStream in = ByteInputStream.wrapBE(out.array())) {
                System.out.println(in.getDouble());
                System.out.println(in.getSignedLong(1));
                System.out.println(in.getSignedLong(2));
                System.out.println(in.getSignedLong(3));
                System.out.println(in.getSignedLong(4));
                System.out.println(in.getSignedLong(5));
                System.out.println(in.getSignedLong(6));
                System.out.println(in.getSignedLong(7));
                System.out.println(in.getSignedLong(8));
                System.out.println(in.getSignedLong(9));
                System.out.println(in.getSignedLong(10));
                System.out.println(in.getSignedLong(2));
            }
        }
    }
}
