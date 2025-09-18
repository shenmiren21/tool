package curl_util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Curl命令生成工具类
 * 用于生成curl命令字符串，方便调试和测试API接口
 *
 * @author shenmiren21
 */
public class CurlUtil {

    private static final Logger logger = Logger.getLogger(CurlUtil.class.getName());
    private static final String LOG_PREFIX = "[CurlUtil]";
    
    /**
     * HTTP方法枚举
     */
    public enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
    }

    /**
     * 生成GET请求的curl命令
     * 
     * @param url 请求URL
     * @param params 查询参数
     * @param headers 请求头
     * @return curl命令字符串
     */
    public static String generateGetCurl(String url, Map<String, Object> params, Map<String, String> headers) {
        return generateCurlCommand(HttpMethod.GET, url, params, headers, null);
    }

    /**
     * 生成POST请求的curl命令（JSON格式）
     * 
     * @param url 请求URL
     * @param headers 请求头
     * @param jsonBody JSON请求体
     * @return curl命令字符串
     */
    public static String generatePostJsonCurl(String url, Map<String, String> headers, String jsonBody) {
        return generateCurlCommand(HttpMethod.POST, url, null, headers, jsonBody);
    }

    /**
     * 生成POST请求的curl命令（表单格式）
     * 
     * @param url 请求URL
     * @param headers 请求头
     * @param formData 表单数据
     * @return curl命令字符串
     */
    public static String generatePostFormCurl(String url, Map<String, String> headers, Map<String, Object> formData) {
        return generateCurlCommand(HttpMethod.POST, url, formData, headers, null);
    }

    /**
     * 生成curl命令字符串（您原始的方法，优化版本）
     * 
     * @param url 请求URL
     * @param params 请求参数
     * @param headers 请求头
     * @return curl命令字符串
     */
    public static String generateCurlCommand(String url, Map<String, Object> params, Map<String, String> headers) {
        StringBuilder curl = new StringBuilder();
        curl.append("curl -X GET");

        // 构建完整URL（包含查询参数）
        if (params != null && !params.isEmpty()) {
            StringBuilder queryParams = new StringBuilder();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (queryParams.length() > 0) {
                    queryParams.append("&");
                }
                try {
                    String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name());
                    String value = URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8.name());
                    queryParams.append(key).append("=").append(value);
                } catch (UnsupportedEncodingException e) {
                    queryParams.append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
            url = url + (url.contains("?") ? "&" : "?") + queryParams.toString();
        }

        curl.append(" \"").append(url).append("\"");

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                curl.append(" \\\n  -H \"").append(header.getKey()).append(": ").append(header.getValue()).append("\"");
            }
        }

        curl.append(" \\\n  -v");
        return curl.toString();
    }

    /**
     * 生成curl命令字符串（通用方法）
     * 
     * @param method HTTP方法
     * @param url 请求URL
     * @param params 请求参数（GET请求作为查询参数，POST请求作为表单数据）
     * @param headers 请求头
     * @param jsonBody JSON请求体（仅用于POST/PUT等请求）
     * @return curl命令字符串
     */
    public static String generateCurlCommand(HttpMethod method, String url, Map<String, Object> params, 
                                           Map<String, String> headers, String jsonBody) {
        try {
            StringBuilder curl = new StringBuilder();
            curl.append("curl -X ").append(method.name());

            // 处理URL和参数
            String finalUrl = buildUrlWithParams(url, params, method);
            curl.append(" \"").append(finalUrl).append("\"");

            // 添加请求头
            addHeaders(curl, headers, jsonBody != null);

            // 添加请求体
            addBody(curl, params, jsonBody, method);

            // 添加详细输出选项
            curl.append(" \\\n  -v");

            logger.info(LOG_PREFIX + " 生成curl命令成功");
            return curl.toString();

        } catch (Exception e) {
            logger.severe(LOG_PREFIX + " 生成curl命令失败: " + e.getMessage());
            return "# curl命令生成失败: " + e.getMessage();
        }
    }

    /**
     * 构建带参数的URL
     */
    private static String buildUrlWithParams(String url, Map<String, Object> params, HttpMethod method) {
        if (params == null || params.isEmpty() || method != HttpMethod.GET) {
            return url;
        }

        StringBuilder queryParams = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (queryParams.length() > 0) {
                queryParams.append("&");
            }
            try {
                String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name());
                String value = URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8.name());
                queryParams.append(key).append("=").append(value);
            } catch (UnsupportedEncodingException e) {
                // UTF-8 should always be supported
                queryParams.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }

        return url + (url.contains("?") ? "&" : "?") + queryParams.toString();
    }

    /**
     * 添加请求头
     */
    private static void addHeaders(StringBuilder curl, Map<String, String> headers, boolean hasJsonBody) {
        // 如果有JSON请求体，自动添加Content-Type
        if (hasJsonBody) {
            curl.append(" \\\n  -H \"Content-Type: application/json\"");
        }

        // 添加自定义请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                curl.append(" \\\n  -H \"").append(header.getKey()).append(": ").append(header.getValue()).append("\"");
            }
        }
    }

    /**
     * 添加请求体
     */
    private static void addBody(StringBuilder curl, Map<String, Object> params, String jsonBody, HttpMethod method) {
        if (jsonBody != null && !jsonBody.trim().isEmpty()) {
            // JSON请求体
            String escapedJson = jsonBody.replace("\"", "\\\"");
            curl.append(" \\\n  -d \"").append(escapedJson).append("\"");
        } else if (params != null && !params.isEmpty() && method != HttpMethod.GET) {
            // 表单数据
            StringBuilder formData = new StringBuilder();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (formData.length() > 0) {
                    formData.append("&");
                }
                try {
                    String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name());
                    String value = URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8.name());
                    formData.append(key).append("=").append(value);
                } catch (UnsupportedEncodingException e) {
                    formData.append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
            curl.append(" \\\n  -d \"").append(formData.toString()).append("\"");
        }
    }

    /**
     * 生成带签名的curl命令（适用于签名验证系统）
     * 
     * @param url 请求URL
     * @param appId 应用ID
     * @param signature 签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param iv 初始化向量
     * @param jsonBody JSON请求体
     * @return curl命令字符串
     */
    public static String generateSignedCurl(String url, String appId, String signature, 
                                          Long timestamp, String nonce, String iv, String jsonBody) {
        StringBuilder curl = new StringBuilder();
        curl.append("curl -X POST \"").append(url).append("\"");
        
        // 添加签名相关请求头
        curl.append(" \\\n  -H \"Content-Type: application/json\"");
        curl.append(" \\\n  -H \"appId: ").append(appId).append("\"");
        curl.append(" \\\n  -H \"signature: ").append(signature).append("\"");
        curl.append(" \\\n  -H \"timestamp: ").append(timestamp).append("\"");
        curl.append(" \\\n  -H \"nonce: ").append(nonce).append("\"");
        curl.append(" \\\n  -H \"iv: ").append(iv).append("\"");
        
        // 添加请求体
        if (jsonBody != null && !jsonBody.trim().isEmpty()) {
            String escapedJson = jsonBody.replace("\"", "\\\"");
            curl.append(" \\\n  -d \"").append(escapedJson).append("\"");
        }
        
        curl.append(" \\\n  -v");
        return curl.toString();
    }

    /**
     * 生成简单的GET请求curl命令
     * 
     * @param url 请求URL
     * @return curl命令字符串
     */
    public static String generateSimpleGetCurl(String url) {
        return generateGetCurl(url, null, null);
    }

    /**
     * 生成简单的POST请求curl命令
     * 
     * @param url 请求URL
     * @param jsonBody JSON请求体
     * @return curl命令字符串
     */
    public static String generateSimplePostCurl(String url, String jsonBody) {
        return generatePostJsonCurl(url, null, jsonBody);
    }

    /**
     * 生成测试脚本
     * 
     * @param baseUrl 基础URL
     * @param appId 应用ID
     * @param signature 签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param iv 初始化向量
     * @param requestBody 请求体
     * @return bash脚本内容
     */
    public static String generateTestScript(String baseUrl, String appId, String signature,
                                          Long timestamp, String nonce, String iv, String requestBody) {
        StringBuilder script = new StringBuilder();
        script.append("#!/bin/bash\n");
        script.append("# API测试脚本\n");
        script.append("# 生成时间: ").append(new java.util.Date()).append("\n\n");
        
        script.append("echo \"开始API测试...\"\n\n");
        
        String curl = generateSignedCurl(baseUrl, appId, signature, timestamp, nonce, iv, requestBody);
        script.append(curl).append("\n\n");
        
        script.append("echo \"API测试完成\"\n");
        
        return script.toString();
    }

    /**
     * 打印格式化的curl命令（调试用）
     * 
     * @param title 标题
     * @param curlCommand curl命令·
     */
    public static void printFormattedCurl(String title, String curlCommand) {
        System.out.println("=== " + title + " ===");
        System.out.println(curlCommand);
        System.out.println("=".repeat(title.length() + 8));
        System.out.println();
    }

    /**
     * 验证URL格式
     * 
     * @param url URL字符串
     * @return 是否为有效URL
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        return url.startsWith("http://") || url.startsWith("https://");
    }

    /**
     * 安全地添加请求头（避免敏感信息泄露）
     * 
     * @param headers 原始请求头
     * @return 安全的请求头（敏感信息被掩码）
     */
    public static Map<String, String> maskSensitiveHeaders(Map<String, String> headers) {
        if (headers == null) {
            return null;
        }
        
        Map<String, String> safeHeaders = new java.util.HashMap<>(headers);
        
        // 掩码敏感信息
        for (Map.Entry<String, String> entry : safeHeaders.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (key.contains("authorization") || key.contains("token") || 
                key.contains("password") || key.contains("secret")) {
                safeHeaders.put(entry.getKey(), "***");
            }
        }
        
        return safeHeaders;
    }
}
