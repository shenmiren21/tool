package com.haedu.common.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Random;

public class SM4SignUtils {


    static {
        // 注册Bouncy Castle Provider
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 生成SM4加密签名
     *
     * @param appId     应用ID
     * @param appSecret 应用密钥
     * @param key       16字节的加密密钥（UTF-8编码）
     * @return 十六进制格式的加密签名
     */
    public static String generateSign(String appId, String appSecret, String key) {
        try {
            // 生成8位随机数字
            String random = generateRandomNumbers(8);
            // 拼接明文文本
            String text = appId + appSecret + "+" + random;

            // 验证并转换密钥
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            validateKey(keyBytes);

            // 初始化SM4加密器（ECB模式，PKCS7填充）
            Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS7Padding", "BC");
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "SM4");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            // 执行加密
            byte[] encryptedData = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

            // 转换为十六进制字符串
            return bytesToHex(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("签名生成失败", e);
        }
    }


    public static void verifySign(String hexSignature, String expectedAppId, String expectedAppSecret, String key) {
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            validateKey(keyBytes);

            Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS7Padding", "BC");
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "SM4");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decryptedBytes = cipher.doFinal(hexStringToBytes(hexSignature));
            String decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);
            System.out.println("解密后明文: " + decryptedText);

            String[] parts = decryptedText.split("\\+");
            if (parts.length != 2) {
                throw new RuntimeException("签名格式错误");
            }

            String appIdAndSecret = parts[0];
            String random = parts[1];

            if (!appIdAndSecret.startsWith(expectedAppId)) {
                throw new RuntimeException("AppId不匹配");
            }
            if (!appIdAndSecret.substring(expectedAppId.length()).equals(expectedAppSecret)) {
                throw new RuntimeException("AppSecret不匹配");
            }

            // 这里可以对random做防重放验证
            System.out.println("签名验证通过，随机数: " + random);
        } catch (Exception e) {
            throw new RuntimeException("签名验证失败", e);
        }
    }

    /**
     * 生成指定位数的数字随机字符串
     */
    private static String generateRandomNumbers(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 验证密钥长度有效性
     */
    private static void validateKey(byte[] keyBytes) {
        if (keyBytes.length != 16) {
            throw new IllegalArgumentException("密钥必须为16字节（128位）");
        }
    }

    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }


    private static byte[] hexStringToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }


    // 使用示例
    public static void main(String[] args) {
        String appId = "1941335199199662080";
        String appSecret = "0iahd6amvpdkdvmz";
        String key = "ln4s6e25ox32gjlq"; // 实际使用时需要确保证16字节长度
        String signature = generateSign(appId, appSecret, key);
        System.out.println("生成的签名：" + signature);
    }

}
