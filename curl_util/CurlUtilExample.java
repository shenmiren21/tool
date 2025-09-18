package curl_util;

import java.util.HashMap;
import java.util.Map;

/**
 * CurlUtil使用示例
 * 展示各种curl命令生成场景
 *
 * @author shenmiren21
 */
public class CurlUtilExample {

    public static void main(String[] args) {
        System.out.println("=== CurlUtil 使用示例 ===\n");

        // 示例1: 简单GET请求
        simpleGetExample();

        // 示例2: 带参数的GET请求
        getWithParamsExample();

        // 示例3: POST JSON请求
        postJsonExample();

        // 示例4: POST表单请求
        postFormExample();

        // 示例5: 带签名的请求
        signedRequestExample();

        // 示例6: 生成测试脚本
        generateTestScriptExample();

        // 示例7: 使用原始方法（兼容性）
        originalMethodExample();
    }

    /**
     * 示例1: 简单GET请求
     */
    private static void simpleGetExample() {
        String url = "https://api.example.com/users";
        String curl = CurlUtil.generateSimpleGetCurl(url);
        CurlUtil.printFormattedCurl("简单GET请求", curl);
    }

    /**
     * 示例2: 带参数的GET请求
     */
    private static void getWithParamsExample() {
        String url = "https://api.example.com/users";
        
        Map<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("size", 10);
        params.put("status", "active");
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer your-token");
        headers.put("Accept", "application/json");
        
        String curl = CurlUtil.generateGetCurl(url, params, headers);
        CurlUtil.printFormattedCurl("带参数的GET请求", curl);
    }

    /**
     * 示例3: POST JSON请求
     */
    private static void postJsonExample() {
        String url = "https://api.example.com/users";
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer your-token");
        headers.put("Accept", "application/json");
        
        String jsonBody = "{\"name\":\"张三\",\"email\":\"zhangsan@example.com\",\"age\":25}";
        
        String curl = CurlUtil.generatePostJsonCurl(url, headers, jsonBody);
        CurlUtil.printFormattedCurl("POST JSON请求", curl);
    }

    /**
     * 示例4: POST表单请求
     */
    private static void postFormExample() {
        String url = "https://api.example.com/login";
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        
        Map<String, Object> formData = new HashMap<>();
        formData.put("username", "admin");
        formData.put("password", "123456");
        formData.put("remember", true);
        
        String curl = CurlUtil.generatePostFormCurl(url, headers, formData);
        CurlUtil.printFormattedCurl("POST表单请求", curl);
    }

    /**
     * 示例5: 带签名的请求
     */
    private static void signedRequestExample() {
        String url = "https://api.example.com/secure-endpoint";
        String appId = "your-app-id";
        String signature = "generated-signature-hash";
        Long timestamp = System.currentTimeMillis();
        String nonce = "random-nonce-value";
        String iv = "initialization-vector";
        String jsonBody = "{\"data\":\"encrypted-content\"}";
        
        String curl = CurlUtil.generateSignedCurl(url, appId, signature, timestamp, nonce, iv, jsonBody);
        CurlUtil.printFormattedCurl("带签名的请求", curl);
    }

    /**
     * 示例6: 生成测试脚本
     */
    private static void generateTestScriptExample() {
        String baseUrl = "https://api.example.com/test";
        String appId = "test-app-id";
        String signature = "test-signature";
        Long timestamp = System.currentTimeMillis();
        String nonce = "test-nonce";
        String iv = "test-iv";
        String requestBody = "{\"test\":\"data\"}";
        
        String script = CurlUtil.generateTestScript(baseUrl, appId, signature, timestamp, nonce, iv, requestBody);
        CurlUtil.printFormattedCurl("生成的测试脚本", script);
    }

    /**
     * 示例7: 使用原始方法（兼容性）
     */
    private static void originalMethodExample() {
        String url = "https://api.example.com/legacy";
        
        Map<String, Object> params = new HashMap<>();
        params.put("id", 123);
        params.put("type", "legacy");
        
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Legacy-Header", "legacy-value");
        
        String curl = CurlUtil.generateCurlCommand(url, params, headers);
        CurlUtil.printFormattedCurl("原始方法（兼容性）", curl);
    }

    /**
     * 工具方法示例
     */
    public static void utilityMethodsExample() {
        System.out.println("=== 工具方法示例 ===\n");
        
        // URL验证
        System.out.println("URL验证:");
        System.out.println("https://api.example.com 是否有效: " + CurlUtil.isValidUrl("https://api.example.com"));
        System.out.println("invalid-url 是否有效: " + CurlUtil.isValidUrl("invalid-url"));
        System.out.println();
        
        // 敏感信息掩码
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer secret-token");
        headers.put("X-API-Key", "secret-key");
        headers.put("Content-Type", "application/json");
        
        Map<String, String> maskedHeaders = CurlUtil.maskSensitiveHeaders(headers);
        System.out.println("原始请求头: " + headers);
        System.out.println("掩码后请求头: " + maskedHeaders);
    }

    /**
     * 高级用法示例
     */
    public static void advancedUsageExample() {
        System.out.println("=== 高级用法示例 ===\n");
        
        // 使用通用方法生成不同类型的请求
        String url = "https://api.example.com/resource";
        
        Map<String, Object> params = new HashMap<>();
        params.put("filter", "active");
        params.put("sort", "created_at");
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token");
        
        // PUT请求
        String putCurl = CurlUtil.generateCurlCommand(CurlUtil.HttpMethod.PUT, url, params, headers, "{\"status\":\"updated\"}");
        CurlUtil.printFormattedCurl("PUT请求", putCurl);
        
        // DELETE请求
        String deleteCurl = CurlUtil.generateCurlCommand(CurlUtil.HttpMethod.DELETE, url + "/123", null, headers, null);
        CurlUtil.printFormattedCurl("DELETE请求", deleteCurl);
        
        // PATCH请求
        String patchCurl = CurlUtil.generateCurlCommand(CurlUtil.HttpMethod.PATCH, url + "/123", null, headers, "{\"name\":\"新名称\"}");
        CurlUtil.printFormattedCurl("PATCH请求", patchCurl);
    }
}