package com.gict.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 解密m3u8
 */
public class M3U8DecryptUtil {

    /**
     * 解密m3u8使用的AES
     *
     * @param keyBytes      密钥的二进制
     * @param mediaFilePath 分片文件的路径
     * @return
     */
    public static byte[] decrypt(byte[] keyBytes, String mediaFilePath) {
        try {
            // 读取密钥文件内容
//            byte[] keyBytes = Files.readAllBytes(Paths.get(keyFilePath));

            // 读取分片文件内容
            byte[] mediaBytes = Files.readAllBytes(Paths.get(mediaFilePath));

            // 创建密钥对象
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

            // 创建解密器
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(new byte[16]));

            // 执行解密
            return cipher.doFinal(mediaBytes);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("解密失败");
        }
        return null;
    }

}
