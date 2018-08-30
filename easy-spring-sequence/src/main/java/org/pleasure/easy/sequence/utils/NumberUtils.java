package org.pleasure.easy.sequence.utils;

/**
 * 功能描述
 * 
 * <p>
 * <a href="NumberUtils.java"><i>View Source</i></a>
 * 
 * @author xian
 * @version 1.0
 * @since 1.0
 */
public class NumberUtils {

    private final static char[] CHAR_DIGIT = new char[64];
    private final static String CHAR64 = "0123456789abcdefghijklmnopqrstuvwxyz-_ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String longTo64(long l) {
        char[] array = new char[11];
        for (int i = 10; i >= 0; i--) {
            array[i] = CHAR_DIGIT[(int) (l & 63)];
            l >>= 6;
        }
        return new String(array);
    }

    public static byte[] long2bytes(long l) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte) (l & 0xff);
            l = l >> 8;
        }
        return array;
    }

    public static byte[] int2bytes(int l) {
        byte[] array = new byte[4];
        for (int i = 3; i >= 0; i--) {
            array[i] = (byte) (l & 0xff);
            l = l >> 8;
        }
        return array;
    }

    static {
        for (int i = 0; i < 64; i++) {
            CHAR_DIGIT[i] = CHAR64.charAt(i);
        }
    }

}
