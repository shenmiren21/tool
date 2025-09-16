package com.chestnut.api.utils;

import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service调用工具类
 * 通用的Service方法调用和JSON转换工具
 *
 * @author shenmiren21
 */
@Slf4j
public class ServiceInvokeUtils {

    // 日志常量
    private static final String LOG_PREFIX = "[ServiceInvokeUtils]";
    private static final String PARAM_NOT_FOUND_ERROR = LOG_PREFIX + " 参数不能为空";
    private static final String METHOD_NOT_FOUND_ERROR = LOG_PREFIX + " 未找到方法";
    private static final String INVOCATION_FAILED_ERROR = LOG_PREFIX + " 调用方法失败";
    private static final String SERVICE_INSTANCE_NULL_ERROR = LOG_PREFIX + " 无法获取Service实例";
    private static final String METHOD_INVOCATION_START_INFO = LOG_PREFIX + " 开始调用Service";
    private static final String METHOD_INVOCATION_PREPARE_DEBUG = LOG_PREFIX + " 准备调用方法";
    private static final String METHOD_INVOCATION_SUCCESS_INFO = LOG_PREFIX + " 方法调用成功";
    private static final String JSON_RESULT_NULL_WARN = LOG_PREFIX + " JSON调用结果为空";
    private static final String PARAM_CONVERSION_FAILED_WARNING = LOG_PREFIX + " 参数转换出错";
    
    // 新增缺失的常量
    private static final String TAG_RESULT_NULL_WARN = LOG_PREFIX + " 转换结果为空";
    private static final String JSON_CONVERT_SUCCESS_INFO = LOG_PREFIX + " JSON转换成功";
    private static final String JSON_CONVERT_FAILED_ERROR = LOG_PREFIX + " JSON转换失败";

    private static final String JSON_OBJECT_START = "{";
    private static final String JSON_OBJECT_END = "}";

    /**
     * 通用方法调用
     * @param <T> 返回值类型
     * @param <R> Service类型
     * @param returnType 返回值类型
     * @param serviceClass Service类
     * @param methodName 方法名
     * @param args 方法参数
     * @return 返回指定类型的值
     */
    public static <T, R> T invokeService(Class<T> returnType, Class<R> serviceClass, String methodName, Object... args) {
        if (serviceClass == null || StringUtils.isEmpty(methodName)) {
            log.error(PARAM_NOT_FOUND_ERROR + ": serviceClass={}, methodName={}", serviceClass, methodName);
            return null;
        }

        try {
            log.info(METHOD_INVOCATION_START_INFO + ": {}, 方法: {}", serviceClass.getSimpleName(), methodName);

            // 修复泛型类型问题
            R service = SpringUtils.getBean(serviceClass);
            if (service == null) {
                log.error(SERVICE_INSTANCE_NULL_ERROR + ": {}", serviceClass.getName());
                return null;
            }

            String jsonResult = invokeServiceForJson(service, methodName, args);
            return convertJsonToType(jsonResult, returnType);

        } catch (Exception e) {
            log.error(INVOCATION_FAILED_ERROR + ": {}.{}, 错误: {}",
                    serviceClass.getSimpleName(), methodName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将JSON字符串转换为指定类型的对象
     * @param <T> 目标类型
     * @param jsonString JSON字符串
     * @param targetType 目标类型Class
     * @return 指定类型的对象
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertJsonToType(String jsonString, Class<T> targetType) {
        if (StringUtils.isEmpty(jsonString) || targetType == null) {
            log.warn(JSON_RESULT_NULL_WARN + ": jsonString={}, targetType={}",
                    jsonString != null ? "非空" : "空", targetType);
            return null;
        }

        try {
            log.debug(LOG_PREFIX + " 开始转换JSON为类型: {} -> {}", jsonString, targetType.getSimpleName());

            // 特殊处理：如果目标类型是String，且输入是JSON对象
            if (targetType == String.class) {
                String trimmed = jsonString.trim();
                if (trimmed.startsWith(JSON_OBJECT_START) && trimmed.endsWith(JSON_OBJECT_END)) {
                    try {
                        Map<String, Object> map = convertJsonToMap(jsonString);
                        if (map != null && !map.isEmpty()) {
                            String formattedJson = JacksonUtils.to(map);
                            log.info(LOG_PREFIX + " String类型JSON格式化成功");
                            return (T) formattedJson;
                        }
                    } catch (Exception e) {
                        log.debug(LOG_PREFIX + " JSON对象格式化失败，使用原始字符串: {}", e.getMessage());
                    }
                }
                return (T) jsonString;
            }

            T result = JacksonUtils.from(jsonString, targetType);

            if (result == null) {
                log.warn(TAG_RESULT_NULL_WARN);
                return null;
            }

            log.info(JSON_CONVERT_SUCCESS_INFO + "，目标类型: {}", targetType.getSimpleName());
            return result;

        } catch (Exception e) {
            log.error(JSON_CONVERT_FAILED_ERROR + ": {} -> {}, 错误: {}",
                    jsonString, targetType.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将JSON字符串转换为Map对象
     * @param jsonString JSON字符串
     * @return Map对象
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> convertJsonToMap(String jsonString) {
        if (StringUtils.isEmpty(jsonString)) {
            log.warn(LOG_PREFIX + " JSON字符串为空");
            return new HashMap<>();
        }

        try {
            return JacksonUtils.from(jsonString, Map.class);
        } catch (Exception e) {
            log.error(LOG_PREFIX + " JSON转Map失败: {}, 错误: {}", jsonString, e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 调用Service方法并返回JSON字符串
     * @param service Service实例
     * @param methodName 方法名
     * @param args 方法参数
     * @return JSON字符串
     */
    private static String invokeServiceForJson(Object service, String methodName, Object[] args) {
        if (service == null || StringUtils.isEmpty(methodName)) {
            log.error(PARAM_NOT_FOUND_ERROR + ": service={}, methodName={}", service, methodName);
            return null;
        }

        try {
            log.debug(METHOD_INVOCATION_PREPARE_DEBUG + ": {}.{}", service.getClass().getSimpleName(), methodName);

            // 获取参数类型
            Class<?>[] paramTypes = getParameterTypes(args);
            
            // 查找方法
            Method method = findMethod(service.getClass(), methodName, paramTypes);
            if (method == null) {
                log.error(METHOD_NOT_FOUND_ERROR + ": {}.{}", service.getClass().getSimpleName(), methodName);
                return null;
            }

            // 调用方法
            Object result = ReflectionUtils.invokeMethod(method, service, args);
            
            if (result == null) {
                log.warn(JSON_RESULT_NULL_WARN + ": {}.{}", service.getClass().getSimpleName(), methodName);
                return null;
            }

            //转换为JSON字符串 智能处理返回结果
            String jsonResult = (result instanceof String) ? (String) result : JacksonUtils.to(result);
            log.info(METHOD_INVOCATION_SUCCESS_INFO + ": {}.{}", service.getClass().getSimpleName(), methodName);
            return jsonResult;

        } catch (Exception e) {
            log.error(INVOCATION_FAILED_ERROR + ": {}.{}, 错误: {}",
                    service.getClass().getSimpleName(), methodName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 查找方法（支持参数类型匹配）
     * @param clazz 类
     * @param methodName 方法名
     * @param paramTypes 参数类型
     * @return 方法对象
     */
    private static Method findMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes) {
        if (clazz == null || StringUtils.isEmpty(methodName)) {
            log.error(LOG_PREFIX + " 查找方法参数不能为空: clazz={}, methodName={}", clazz, methodName);
            return null;
        }

        try {
            // 首先尝试精确匹配
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            // 改进：添加参数类型兼容性检查
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName) &&
                        isParameterTypesCompatible(method.getParameterTypes(), paramTypes)) {
                    return method;
                }
            }

            // 如果还是找不到，尝试无参方法
            if (paramTypes.length == 0) {
                try {
                    return clazz.getMethod(methodName);
                } catch (NoSuchMethodException ex) {
                    log.debug(LOG_PREFIX + " 无参方法也未找到: {}.{}", clazz.getSimpleName(), methodName);
                }
            }

            log.error(LOG_PREFIX + " 未找到匹配的方法: {}.{}", clazz.getSimpleName(), methodName);
            return null;
        }
    }

    /**
     * 检查参数类型兼容性
     * @param methodParams 方法定义的参数类型
     * @param paramTypes 实际参数类型
     * @return 是否兼容
     */
    private static boolean isParameterTypesCompatible(Class<?>[] methodParams, Class<?>[] paramTypes) {
        if (methodParams.length != paramTypes.length) {
            return false;
        }

        for (int i = 0; i < methodParams.length; i++) {
            if (!isAssignable(methodParams[i], paramTypes[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查类型是否可赋值
     * @param target 目标类型
     * @param source 源类型
     * @return 是否可赋值
     */
    private static boolean isAssignable(Class<?> target, Class<?> source) {
        if (target == null || source == null) {
            return false;
        }
        
        // 精确匹配
        if (target.equals(source)) {
            return true;
        }
        
        // 继承关系检查
        if (target.isAssignableFrom(source)) {
            return true;
        }
        
        // 基本类型和包装类型的兼容性检查
        if (target.isPrimitive() || source.isPrimitive()) {
            return isPrimitiveCompatible(target, source);
        }
        
        return false;
    }

    /**
     * 检查基本类型兼容性
     * @param target 目标类型
     * @param source 源类型
     * @return 是否兼容
     */
    private static boolean isPrimitiveCompatible(Class<?> target, Class<?> source) {
        Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();
        primitiveWrapperMap.put(boolean.class, Boolean.class);
        primitiveWrapperMap.put(byte.class, Byte.class);
        primitiveWrapperMap.put(char.class, Character.class);
        primitiveWrapperMap.put(double.class, Double.class);
        primitiveWrapperMap.put(float.class, Float.class);
        primitiveWrapperMap.put(int.class, Integer.class);
        primitiveWrapperMap.put(long.class, Long.class);
        primitiveWrapperMap.put(short.class, Short.class);
        
        return primitiveWrapperMap.get(target) == source || primitiveWrapperMap.get(source) == target;
    }

    /**
     * 获取参数类型数组
     * @param args 参数数组
     * @return 参数类型数组
     */
    private static Class<?>[] getParameterTypes(Object... args) {
        if (args == null || args.length == 0) {
            return new Class<?>[0];
        }
        
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
        }
        return paramTypes;
    }

    /**
     * 安全获取字符串值
     * @param map 数据Map
     * @param key 键
     * @return 字符串值
     */
    public static String getStringValue(Map<String, Object> map, String key) {
        if (map == null || StringUtils.isEmpty(key)) {
            return null;
        }
        
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        
        return value.toString();
    }

    /**
     * 安全获取整数值
     * @param map 数据Map
     * @param key 键
     * @return 整数值
     */
    public static Integer getIntValue(Map<String, Object> map, String key) {
        if (map == null || StringUtils.isEmpty(key)) {
            return null;
        }
        
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.warn(PARAM_CONVERSION_FAILED_WARNING + ": {} -> Integer, 值: {}", key, value);
            return null;
        }
    }

    /**
     * 安全获取布尔值
     * @param map 数据Map
     * @param key 键
     * @return 布尔值
     */
    public static Boolean getBooleanValue(Map<String, Object> map, String key) {
        if (map == null || StringUtils.isEmpty(key)) {
            return null;
        }
        
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        
        String strValue = value.toString().toLowerCase();
        return "true".equals(strValue) || "1".equals(strValue);
    }

    /**
     * 安全获取长整型值
     * @param map 数据Map
     * @param key 键
     * @return 长整型值
     */
    public static Long getLongValue(Map<String, Object> map, String key) {
        if (map == null || StringUtils.isEmpty(key)) {
            return null;
        }
        
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        
        try {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn(PARAM_CONVERSION_FAILED_WARNING + ": {} -> Long, 值: {}", key, value);
            return null;
        }
    }

    /**
     * 安全获取指定类型的值
     * @param <T> 目标类型
     * @param map 数据Map
     * @param key 键
     * @param targetType 目标类型
     * @return 指定类型的值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValue(Map<String, Object> map, String key, Class<T> targetType) {
        if (map == null || StringUtils.isEmpty(key) || targetType == null) {
            return null;
        }
        
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        
        return convertValue(value, targetType);
    }

    /**
     * 值类型转换
     * @param <T> 目标类型
     * @param value 原始值
     * @param targetType 目标类型
     * @return 转换后的值
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertValue(Object value, Class<T> targetType) {
        if (value == null || targetType == null) {
            return null;
        }
        
        // 如果类型匹配，直接返回
        if (targetType.isInstance(value)) {
            return (T) value;
        }
        
        try {
            // 字符串类型转换
            if (targetType == String.class) {
                return (T) value.toString();
            }
            
            // 数字类型转换
            if (targetType == Integer.class || targetType == int.class) {
                if (value instanceof Number) {
                    return (T) Integer.valueOf(((Number) value).intValue());
                }
                return (T) Integer.valueOf(value.toString());
            }
            
            if (targetType == Long.class || targetType == long.class) {
                if (value instanceof Number) {
                    return (T) Long.valueOf(((Number) value).longValue());
                }
                return (T) Long.valueOf(value.toString());
            }
            
            if (targetType == Boolean.class || targetType == boolean.class) {
                if (value instanceof Boolean) {
                    return (T) value;
                }
                String strValue = value.toString().toLowerCase();
                return (T) Boolean.valueOf("true".equals(strValue) || "1".equals(strValue));
            }
            
            // 其他类型通过JSON转换
            String jsonValue = JacksonUtils.to(value);
            return JacksonUtils.from(jsonValue, targetType);
            
        } catch (Exception e) {
            log.warn(PARAM_CONVERSION_FAILED_WARNING + ": {} -> {}, 值: {}, 错误: {}", 
                    value.getClass().getSimpleName(), targetType.getSimpleName(), value, e.getMessage());
            return null;
        }
    }
}
