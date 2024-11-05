// Copyright (c) 2014-2016 Trapeze Group Switzerland
package io.amotech.bleexperimentation.air.stream.bit8;

import java.io.IOException;
import java.io.InputStream;

import io.amotech.bleexperimentation.air.utils.SerialisationHelper;

public class ByteInputStreamLE extends ByteInputStream {

    protected ByteInputStreamLE(final InputStream in) {
        super(in);
    }

    @Override
    protected long bytes(int bytes) throws IOException {
        if (bytes < 0 || bytes > Long.BYTES) {
            throw new IOException("trying to read more bytes than supported");
        }
        long result = 0;
        int pos = 0;
        while (bytes > 0) { // need to read
            long b;
            bytes -= 1;
            if ((b = in.read()) == -1) {
                throw new IOException("end of stream");
            }
            result |= b << pos;
            pos += Byte.SIZE;
        }
        return result;
    }

    public static void main(final String[] args) throws IOException {
        try (ByteInputStreamLE in = ByteInputStream.wrapLE(SerialisationHelper.getRaw("abcdef1234567890abcd"))) {
            System.out.println(in.getSignedLong(6));
            System.out.println(in.getSignedLong(4));
        }
        try (ByteInputStreamLE in = ByteInputStream.wrapLE(SerialisationHelper.getRaw("abcdef1234567890abcdef1234567800"))) {
            System.out.println(in.getSignedLong(8));
            System.out.println(in.getBoolean());
            System.out.println(in.getBoolean());
            System.out.println(in.getBoolean());
            System.out.println(in.getBoolean());
            System.out.println(in.getBoolean());
            System.out.println(in.getBoolean());
            System.out.println(in.getBoolean());
            System.out.println(in.getBoolean());
        }
    }

}
