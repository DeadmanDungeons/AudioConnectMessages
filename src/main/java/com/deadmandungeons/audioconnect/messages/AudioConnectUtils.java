package com.deadmandungeons.audioconnect.messages;

import com.deadmandungeons.connect.commons.ConnectUtils;

import java.nio.charset.StandardCharsets;

public class AudioConnectUtils extends ConnectUtils {

    private static final String HEX_CHARS = "0123456789ABCDEF";
    private static final char FORMATTING_CHAR = '\u00A7';

    public static String encodeFormattingCodes(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        char[] chars = new char[bytes.length * 4];
        for (int i = 0, n = 0; i < bytes.length; i++) {
            int unsignedByte = bytes[i] & 0xFF;
            chars[n++] = FORMATTING_CHAR;
            chars[n++] = HEX_CHARS.charAt(unsignedByte >>> 4);
            chars[n++] = FORMATTING_CHAR;
            chars[n++] = HEX_CHARS.charAt(unsignedByte & 0xF);
        }
        return new String(chars);
    }

    public static String decodeFormattingCodes(String encoded) {
        if (encoded.length() % 4 != 0) {
            return null;
        }
        char[] chars = encoded.toCharArray();
        byte[] bytes = new byte[chars.length / 4];
        for (int i = 0, n = 0; i < chars.length; i += 4, n++) {
            if (chars[i] != FORMATTING_CHAR || chars[i + 2] != FORMATTING_CHAR) {
                return null;
            }
            bytes[n] = (byte) ((Character.digit(chars[i + 1], 16) << 4) + Character.digit(chars[i + 3], 16));
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
