package tech.finovy.gateway.common.util;

import org.apache.commons.lang3.StringUtils;

public class ConversionUtil {
    private static final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int minLength = 3;

    private ConversionUtil() {
    }

    public static String encode(long num, int scale) {
        StringBuilder sb = new StringBuilder();
        int remainder;
        while (num > scale - 1) {
            remainder = (int) (num % scale);
            sb.append(chars.charAt(remainder));
            num = num / scale;
        }
        sb.append(chars.charAt((int) num));
        String value = sb.reverse().toString();
        return StringUtils.leftPad(value, minLength, '0');
    }

    public static long decode(String str, int scale) {
        str = str.replace("^0*", "");
        long value = 0;
        char tempChar;
        int tempCharValue;
        for (int i = 0; i < str.length(); i++) {
            tempChar = str.charAt(i);
            tempCharValue = chars.indexOf(tempChar);
            value += (long) (tempCharValue * Math.pow(scale, (str.length() - i - 1)));
        }
        return value;
    }
}
