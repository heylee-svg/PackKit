package com.dhg.packkit.utils;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * md5工具
 * Created by lxh on 15-10-14.
 */
public class MD5Util {
    /**
     * 将字符串生成32位的大写ＭＤ５值
     *
     * @param s
     * @return　返回大写的MD5值
     */
    public static String upperCaseMD5(String s) {
        String md5Value = MD5(s.getBytes());
        if (null != md5Value) {
            return md5Value.toUpperCase();
        }
        return null;
    }

    /**
     * 将字符串生成32位的小写ＭＤ５值
     *
     * @param s
     * @return　返回小写的MD5值
     */
    public static String lowerCaseMD5(String s) {
        if (TextUtils.isEmpty(s)) {
            return null;
        }
        String md5Value = MD5(s.getBytes());
        if (null != md5Value) {
            return md5Value.toLowerCase();
        }
        return null;
    }

    /**
     * 将byte[]生成32位的ＭＤ５值
     *
     * @param data
     * @return
     */
    public static String MD5(byte[] data) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(data);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取文件的ｍｄ5
     *
     * @param file
     * @return
     */
    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }


}

