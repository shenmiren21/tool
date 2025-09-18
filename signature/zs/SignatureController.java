package signature.zs;

import com.example.utils.ServerSignatureValidator;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 签名验证控制器示例
 * 展示如何在Spring Boot中使用签名验证
 */
@RestController
@RequestMapping("/api")
public class SignatureController {
    
    @Autowired
    private ServerSignatureValidator signatureValidator;
    
    /**
     * 需要签名验证的API接口示例
     * @param request HTTP请求
     * @param requestBody 请求体参数
     * @return 响应结果
     */
    @PostMapping("/secure-endpoint")
    public ResponseEntity<?> secureEndpoint(HttpServletRequest request, @RequestBody Map<String, Object> requestBody) {
        
        // 1. 提取请求头
        Map<String, String> headers = extractHeaders(request);
        
        // 2. 验证签名
        ServerSignatureValidator.SignatureValidationResult validationResult = 
                signatureValidator.validateSignature(headers, requestBody);
        
        // 3. 处理验证结果
        if (!validationResult.isSuccess()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", validationResult.getMessage(),
                "code", "SIGNATURE_VALIDATION_FAILED"
            ));
        }
        
        // 4. 签名验证成功，处理业务逻辑
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "请求处理成功");
        response.put("accessId", validationResult.getAccessId());
        response.put("data", processBusinessLogic(requestBody));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 提取请求头参数
     * @param request HTTP请求
     * @return 请求头Map
     */
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        
        return headers;
    }
    
    /**
     * 业务逻辑处理（示例）
     * @param requestParams 请求参数
     * @return 处理结果
     */
    private Object processBusinessLogic(Map<String, Object> requestParams) {
        // 这里实现具体的业务逻辑
        return Map.of(
            "processedAt", System.currentTimeMillis(),
            "receivedParams", requestParams,
            "result", "业务处理完成"
        );
    }
    
    /**
     * 测试接口（无需签名验证）
     * @return 测试响应
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of(
            "message", "服务正常运行",
            "timestamp", System.currentTimeMillis()
        ));
    }
}