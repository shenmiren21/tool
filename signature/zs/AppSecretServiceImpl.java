package signature.zs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * AppSecret服务实现类
 * 提供从数据库或配置中获取AppSecret的示例实现
 */
@Service
public class AppSecretServiceImpl implements AppSecretService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // 示例：内存缓存（生产环境建议使用Redis等分布式缓存）
    private static final Map<String, String> APP_SECRET_CACHE = new HashMap<>();
    
    static {
        // 示例数据（实际应该从数据库加载）
        APP_SECRET_CACHE.put("test_app_001", "test_secret_001");
        APP_SECRET_CACHE.put("test_app_002", "test_secret_002");
    }
    
    @Override
    public String getAppSecret(String accessId) {
        // 方式1：从内存缓存获取（适用于少量固定的应用）
        String secret = APP_SECRET_CACHE.get(accessId);
        if (secret != null) {
            return secret;
        }
        
        // 方式2：从数据库查询（推荐方式）
        return getAppSecretFromDatabase(accessId);
    }
    
    /**
     * 从数据库查询AppSecret
     * @param accessId 应用标识
     * @return 应用密钥
     */
    private String getAppSecretFromDatabase(String accessId) {
        try {
            String sql = "SELECT app_secret FROM app_config WHERE access_id = ? AND status = 1";
            return jdbcTemplate.queryForObject(sql, String.class, accessId);
        } catch (Exception e) {
            // 查询异常或未找到记录
            return null;
        }
    }
    
    /**
     * 刷新缓存（可用于动态更新AppSecret）
     * @param accessId 应用标识
     * @param appSecret 应用密钥
     */
    public void refreshCache(String accessId, String appSecret) {
        APP_SECRET_CACHE.put(accessId, appSecret);
    }
    
    /**
     * 移除缓存
     * @param accessId 应用标识
     */
    public void removeFromCache(String accessId) {
        APP_SECRET_CACHE.remove(accessId);
    }
}