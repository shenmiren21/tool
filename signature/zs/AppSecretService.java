package signature.zs;

/**
 * AppSecret服务接口
 * 用于根据AccessId获取对应的AppSecret
 */
public interface AppSecretService {
    
    /**
     * 根据AccessId获取对应的AppSecret
     * @param accessId 应用标识
     * @return 应用密钥，如果不存在则返回null
     */
    String getAppSecret(String accessId);
}