package com.h264player;

/**
 * Created by Nat on 2017/2/15.
 */

public class G711UCodec {
// s00000001wxyz...s000wxyz
// s0000001wxyza...s001wxyz
// s000001wxyzab...s010wxyz
// s00001wxyzabc...s011wxyz
// s0001wxyzabcd...s100wxyz
// s001wxyzabcde...s101wxyz
// s01wxyzabcdef...s110wxyz
// s1wxyzabcdefg...s111wxyz

    private static byte[] table13to8 = new byte[8192];
    private static short[] table8to16 = new short[256];

    static {
        // b13 --> b8
        for (int p = 1, q = 0; p <= 0x80; p <<= 1, q += 0x10) {
            for (int i = 0, j = (p << 4) - 0x10; i < 16; i++, j += p) {
                int v = (i + q) ^ 0x7F;
                byte value1 = (byte) v;
                byte value2 = (byte) (v + 128);
                for (int m = j, e = j + p; m < e; m++) {
                    table13to8[m] = value1;
                    table13to8[8191 - m] = value2;
                }
            }
        }

        // b8 --> b16
        for (int q = 0; q <= 7; q++) {
            for (int i = 0, m = (q << 4); i < 16; i++, m++) {
                int v = (((i + 0x10) << q) - 0x10) << 3;
                table8to16[m ^ 0x7F] = (short) v;
                table8to16[(m ^ 0x7F) + 128] = (short) (65536 - v);
            }
        }
    }

    public int decode(short[] b16, byte[] b8, int count, int offset) {
        for (int i = 0, j = offset; i < count; i++, j++) {
            b16[i] = table8to16[b8[j] & 0xFF];
        }
        return count;
    }

    public int encode(short[] b16, int count, byte[] b8, int offset) {

        for (int i = 0, j = offset; i < count; i++, j++) {
            b8[j] = table13to8[(b16[i] >> 4) & 0x1FFF];
        }
        return count;
    }

    public int getSampleCount(int frameSize) {
        return frameSize;
    }
}