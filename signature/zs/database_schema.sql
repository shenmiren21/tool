-- 应用配置表
CREATE TABLE app_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    access_id VARCHAR(64) NOT NULL UNIQUE COMMENT '应用标识（AppKey）',
    app_secret VARCHAR(128) NOT NULL COMMENT '应用密钥（AppSecret）',
    app_name VARCHAR(100) NOT NULL COMMENT '应用名称',
    description TEXT COMMENT '应用描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_access_id (access_id),
    INDEX idx_status (status)
) COMMENT '应用配置表';

-- 插入示例数据
INSERT INTO app_config (access_id, app_secret, app_name, description) VALUES
('test_app_001', 'test_secret_001', '测试应用1', '用于测试的应用配置'),
('test_app_002', 'test_secret_002', '测试应用2', '用于测试的应用配置'),
('prod_app_001', 'prod_secret_001_very_long_and_secure', '生产应用1', '生产环境应用配置');

-- 签名验证日志表（可选，用于审计）
CREATE TABLE signature_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    access_id VARCHAR(64) NOT NULL COMMENT '应用标识',
    request_ip VARCHAR(45) COMMENT '请求IP',
    request_uri VARCHAR(255) COMMENT '请求URI',
    request_method VARCHAR(10) COMMENT '请求方法',
    validation_result TINYINT COMMENT '验证结果：1-成功，0-失败',
    failure_reason VARCHAR(255) COMMENT '失败原因',
    request_timestamp BIGINT COMMENT '请求时间戳',
    nonce VARCHAR(64) COMMENT '随机数',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_access_id (access_id),
    INDEX idx_created_time (created_time),
    INDEX idx_validation_result (validation_result)
) COMMENT '签名验证日志表';