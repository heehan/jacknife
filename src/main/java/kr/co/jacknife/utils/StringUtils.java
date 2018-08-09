package kr.co.jacknife.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class StringUtils {
    public static String toPHPSHA1(String str) {
        StringBuilder md5Builder = new StringBuilder();
        try {
            String eip = null;
            byte[] bip = toSHA1(str.getBytes("UTF-8"));
            for (int i = 0; i < bip.length; i++)
            {
                eip = "" + Integer.toHexString((int) bip[i] & 0x000000ff);
                if (eip.length() < 2) eip = "0" + eip;
                md5Builder.append(eip);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return md5Builder.toString();
    }

    public static byte[] toSHA1(byte[] convertme) throws NoSuchAlgorithmException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(convertme);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for(final byte b: a) {
            sb.append(String.format("%02x ", b&0xff));
        }
        return sb.toString();
    }

    public static byte[] sha256(String input) {

        MessageDigest mDigest = null;
        try {
            mDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] result = mDigest.digest(input.getBytes());
        return result;
    }

    // php5 의 md5 와 같은결과를  return 합니다.
    public static String phpmd5(String inputValue)
    {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuilder md5Builder = new StringBuilder();

        String tst = inputValue;
        String eip = null;

        byte[] bip = md5.digest(tst.getBytes());

        for (int i = 0; i < bip.length; i++) {
            eip = "" + Integer.toHexString((int) bip[i] & 0x000000ff);
            if (eip.length() < 2) eip = "0" + eip;
            md5Builder.append(eip);
        }
        return md5Builder.toString();
    }

    public static String sha256Base64Encoded(String input) {
        byte[] sha256Data = sha256(input);
        return Base64.getEncoder().encodeToString(sha256Data);
    }


}
