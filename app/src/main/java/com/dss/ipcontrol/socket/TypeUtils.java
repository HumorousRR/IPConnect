package com.dss.ipcontrol.socket;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TypeUtils {
    public static String toHexString(byte[] ba) {
        return toHexString(ba, ba.length);
    }

    public static String toHexString(byte[] ba, int nCount) {
        if (ba == null) {
            throw new NullPointerException("Passing array is null");
        }

        if (nCount > ba.length || nCount <= 0) {
            Log.e("Util_ERR", "nCount(" + nCount + ") shouldn't be larger than array length(" + ba.length + ")!");
            return null;
        }

        StringBuilder str = new StringBuilder();
        for (int i = 0; i < nCount; i++) {
            str.append(String.format("0x%x  ", ba[i]));
        }
        return str.toString();
    }

    private final static String[] hexSymbols = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public static String toHexFromByte(final byte b) {
        int BITS_PER_HEX_DIGIT = 4;

        byte leftSymbol = (byte) ((b >>> BITS_PER_HEX_DIGIT) & 0x0f);
        byte rightSymbol = (byte) (b & 0x0f);

        return (hexSymbols[leftSymbol] + hexSymbols[rightSymbol]);
    }

    public static String toHexFromBytes(final byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return ("");
        }

        // there are 2 hex digits per byte
        StringBuilder hexBuffer = new StringBuilder(bytes.length * 2);

        // for each byte, convert it to hex and append it to the buffer
        for (int i = 0; i < bytes.length; i++) {
            hexBuffer.append(toHexFromByte(bytes[i]));
        }

        return (hexBuffer.toString());
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static int[] merge(int[] a, int[] b) {
        Set<Integer> set = new HashSet<>(new ArrayList(Arrays.asList(a)));
        set.addAll(new ArrayList(Arrays.asList(b))); // skips duplicate as per Set implementation
        return set.stream().mapToInt(Integer::intValue).toArray();
    }

    public static byte[] toByteArray(List<Integer> list) {

//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ObjectOutputStream oos = null;
//
//        try {
//            oos = new ObjectOutputStream(bos);
//            oos.writeObject(list);
//            return bos.toByteArray();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        if (list != null && !list.isEmpty()) {
            byte[] out = new byte[list.size()];

            for (int i = 0; i < list.size(); i++) {
                out[i] = (byte) (list.get(i) & 0xFF);
            }

            return out;
        }

        return null;
    }

    public static boolean isArgumentPresent(String target, String... args) {
        if (args != null) {
            for (String arg : args) {
                if (arg.equalsIgnoreCase(target))
                    return true;
            }
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static byte[] toAsciiBytes(String s) {
        return s.getBytes(StandardCharsets.US_ASCII);
    }
}
