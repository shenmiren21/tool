package com.chestnut.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Service调用工具类
 * 通用的Service方法调用和JSON转换工具
 * 
 * @author shenmiren21
 */
@Slf4j
public class ServiceInvokeUtils {
    
    /**
     * 通过Service类型调用方法并返回键值对
     * 
     * @param <T> Service类型
     * @param serviceClass Service类型
     * @param methodName 方法名
     * @param args 方法参数
     * @return 键值对Map
     */
    public static <T> Map<String, Object> invokeServiceForMap(Class<T> serviceClass, String methodName, Object... args) {
        if (serviceClass == null || StringUtils.isEmpty(methodName)) {
            log.error("[ServiceInvokeUtils] 参数不能为空: serviceClass={}, methodName={}", serviceClass, methodName);
            return new HashMap<>();
        }
        
        try {
            log.info("[ServiceInvokeUtils] 开始调用Service: {}, 方法: {}", serviceClass.getSimpleName(), methodName);
            
            T service = SpringUtils.getBean(serviceClass);
            if (service == null) {
                log.error("[ServiceInvokeUtils] 无法获取Service实例: {}", serviceClass.getName());
                return new HashMap<>();
            }
            
            String jsonResult = invokeServiceForJson(service, methodName, args);
            return convertJsonToMap(jsonResult);
            
        } catch (Exception e) {
            log.error("[ServiceInvokeUtils] 调用Service失败: {}.{}, 错误: {}", 
                    serviceClass.getSimpleName(), methodName, e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    /**
     * 通过Service Bean名称调用方法并返回键值对
     * 
     * @param serviceBeanName Service Bean名称
     * @param methodName 方法名
     * @param args 方法参数
     * @return 键值对Map
     */
    public static Map<String, Object> invokeServiceForMap(String serviceBeanName, String methodName, Object... args) {
        if (StringUtils.isEmpty(serviceBeanName) || StringUtils.isEmpty(methodName)) {
            log.error("[ServiceInvokeUtils] 参数不能为空: serviceBeanName={}, methodName={}", serviceBeanName, methodName);
            return new HashMap<>();
        }
        
        try {
            log.info("[ServiceInvokeUtils] 开始调用Service Bean: {}, 方法: {}", serviceBeanName, methodName);
            
            Object service = SpringUtils.getBean(serviceBeanName);
            if (service == null) {
                log.error("[ServiceInvokeUtils] 无法获取Service实例: {}", serviceBeanName);
                return new HashMap<>();
            }
            
            String jsonResult = invokeServiceForJson(service, methodName, args);
            return convertJsonToMap(jsonResult);
            
        } catch (Exception e) {
            log.error("[ServiceInvokeUtils] 调用Service失败: {}.{}, 错误: {}", 
                    serviceBeanName, methodName, e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    /**
     * 通过Service类型调用方法并返回指定类型的结果
     * 
     * @param <T> Service类型
     * @param <R> 返回结果类型
     * @param serviceClass Service类型
     * @param methodName 方法名
     * @param returnType 返回类型Class
     * @param args 方法参数
     * @return 指定类型的结果
     */
    public static <T, R> R invokeServiceForType(Class<T> serviceClass, String methodName, Class<R> returnType, Object... args) {
        if (serviceClass == null || StringUtils.isEmpty(methodName) || returnType == null) {
            log.error("[ServiceInvokeUtils] 参数不能为空: serviceClass={}, methodName={}, returnType={}", 
                    serviceClass, methodName, returnType);
            return null;
        }
        
        try {
            log.info("[ServiceInvokeUtils] 开始调用Service: {}, 方法: {}, 返回类型: {}", 
                    serviceClass.getSimpleName(), methodName, returnType.getSimpleName());
            
            T service = SpringUtils.getBean(serviceClass);
            if (service == null) {
                log.error("[ServiceInvokeUtils] 无法获取Service实例: {}", serviceClass.getName());
                return null;
            }
            
            String jsonResult = invokeServiceForJson(service, methodName, args);
            return convertJsonToType(jsonResult, returnType);
            
        } catch (Exception e) {
            log.error("[ServiceInvokeUtils] 调用Service失败: {}.{}, 错误: {}", 
                    serviceClass.getSimpleName(), methodName, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 通过Service Bean名称调用方法并返回指定类型的结果
     * 
     * @param <R> 返回结果类型
     * @param serviceBeanName Service Bean名称
     * @param methodName 方法名
     * @param returnType 返回类型Class
     * @param args 方法参数
     * @return 指定类型的结果
     */
    public static <R> R invokeServiceForType(String serviceBeanName, String methodName, Class<R> returnType, Object... args) {
        if (StringUtils.isEmpty(serviceBeanName) || StringUtils.isEmpty(methodName) || returnType == null) {
            log.error("[ServiceInvokeUtils] 参数不能为空: serviceBeanName={}, methodName={}, returnType={}", 
                    serviceBeanName, methodName, returnType);
            return null;
        }
        
        try {
            log.info("[ServiceInvokeUtils] 开始调用Service Bean: {}, 方法: {}, 返回类型: {}", 
                    serviceBeanName, methodName, returnType.getSimpleName());
            
            Object service = SpringUtils.getBean(serviceBeanName);
            if (service == null) {
                log.error("[ServiceInvokeUtils] 无法获取Service实例: {}", serviceBeanName);
                return null;
            }
            
            String jsonResult = invokeServiceForJson(service, methodName, args);
            return convertJsonToType(jsonResult, returnType);
            
        } catch (Exception e) {
            log.error("[ServiceInvokeUtils] 调用Service失败: {}.{}, 错误: {}", 
                    serviceBeanName, methodName, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 调用Service方法并返回JSON字符串
     * 
     * @param service Service实例
     * @param methodName 方法名
     * @param args 方法参数
     * @return JSON字符串
     */
    public static String invokeServiceForJson(Object service, String methodName, Object... args) {
        if (service == null || StringUtils.isEmpty(methodName)) {
            log.error("[ServiceInvokeUtils] 参数不能为空: service={}, methodName={}", service, methodName);
            return null;
        }
        
        try {
            log.debug("[ServiceInvokeUtils] 准备调用方法: {}", methodName);
            
            Class<?>[] paramTypes = getParameterTypes(args);
            Method method = findMethod(service.getClass(), methodName, paramTypes);
            
            if (method == null) {
                log.error("[ServiceInvokeUtils] 未找到方法: {}.{}", service.getClass().getSimpleName(), methodName);
                return null;
            }
            
            Object result = ReflectionUtils.invokeMethod(method, service, args);
            
            if (result == null) {
                log.warn("[ServiceInvokeUtils] 方法返回结果为null: {}.{}", service.getClass().getSimpleName(), methodName);
                return null;
            }
            
            String jsonResult = result.toString();
            log.info("[ServiceInvokeUtils] 方法调用成功，返回JSON长度: {}", jsonResult.length());
            log.debug("[ServiceInvokeUtils] 返回JSON内容: {}", jsonResult);
            
            return jsonResult;
            
        } catch (Exception e) {
            log.error("[ServiceInvokeUtils] 调用方法失败: {}.{}, 错误: {}", 
                    service.getClass().getSimpleName(), methodName, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 将JSON字符串转换为键值对Map
     * 
     * @param jsonString JSON字符串
     * @return 键值对Map
     */
    public static Map<String, Object> convertJsonToMap(String jsonString) {
        if (StringUtils.isEmpty(jsonString)) {
            log.warn("[ServiceInvokeUtils] JSON字符串为空");
            return new HashMap<>();
        }
        
        try {
            log.debug("[ServiceInvokeUtils] 开始转换JSON为Map: {}", jsonString);
            
            Map<String, Object> resultMap = JacksonUtils.fromMap(jsonString);
            
            if (resultMap == null) {
                log.warn("[ServiceInvokeUtils] JSON转换结果为null");
                return new HashMap<>();
            }
            
            log.info("[ServiceInvokeUtils] JSON转换成功，包含 {} 个键值对", resultMap.size());
            
            if (log.isDebugEnabled()) {
                resultMap.forEach((key, value) -> 
                    log.debug("[ServiceInvokeUtils] 参数: {} = {} ({})", 
                            key, value, value != null ? value.getClass().getSimpleName() : "null")
                );
            }
            
            return resultMap;
            
        } catch (Exception e) {
            log.error("[ServiceInvokeUtils] JSON转换为Map失败: {}, 错误: {}", jsonString, e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    /**
     * 将JSON字符串转换为指定类型的对象
     * 
     * @param <T> 目标类型
     * @param jsonString JSON字符串
     * @param targetType 目标类型Class
     * @return 指定类型的对象
     */
    public static <T> T convertJsonToType(String jsonString, Class<T> targetType) {
        if (StringUtils.isEmpty(jsonString) || targetType == null) {
            log.warn("[ServiceInvokeUtils] JSON字符串或目标类型为空: jsonString={}, targetType={}", 
                    jsonString != null ? "非空" : "空", targetType);
            return null;
        }
        
        try {
            log.debug("[ServiceInvokeUtils] 开始转换JSON为类型: {} -> {}", jsonString, targetType.getSimpleName());
            
            T result = JacksonUtils.fromJson(jsonString, targetType);
            
            if (result == null) {
                log.warn("[ServiceInvokeUtils] JSON转换结果为null");
                return null;
            }
            
            log.info("[ServiceInvokeUtils] JSON转换成功，目标类型: {}", targetType.getSimpleName());
            
            return result;
            
        } catch (Exception e) {
            log.error("[ServiceInvokeUtils] JSON转换为指定类型失败: {} -> {}, 错误: {}", 
                    jsonString, targetType.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 查找方法（支持参数类型匹配）
     * 
     * @param clazz 类
     * @param methodName 方法名
     * @param paramTypes 参数类型
     * @return 方法对象
     */
    private static Method findMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes) {
        if (clazz == null || StringUtils.isEmpty(methodName)) {
            log.error("[ServiceInvokeUtils] 查找方法参数不能为空: clazz={}, methodName={}", clazz, methodName);
            return null;
        }
        
        try {
            // 首先尝试精确匹配
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            // 如果精确匹配失败，尝试查找同名方法
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName) && 
                    method.getParameterCount() == paramTypes.length) {
                    return method;
                }
            }
            
            // 如果还是找不到，尝试无参方法
            if (paramTypes.length == 0) {
                try {
                    return clazz.getMethod(methodName);
                } catch (NoSuchMethodException ex) {
                    log.debug("[ServiceInvokeUtils] 无参方法也未找到: {}.{}", clazz.getSimpleName(), methodName);
                }
            }
            
            log.error("[ServiceInvokeUtils] 未找到匹配的方法: {}.{}", clazz.getSimpleName(), methodName);
            return null;
        }
    }
    
    /**
     * 获取参数类型数组
     * 
     * @param args 参数数组
     * @return 参数类型数组
     */
    private static Class<?>[] getParameterTypes(Object... args) {
        if (args == null || args.length == 0) {
            return new Class<?>[0];
        }
        
        Class<?>[] paramTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
        }
        return paramTypes;
    }
    
    /**
     * 获取指定键的字符串值
     * 
     * @param map 键值对Map
     * @param key 键名
     * @return 字符串值
     */
    public static String getStringValue(Map<String, Object> map, String key) {
        if (map == null || StringUtils.isEmpty(key)) {
            return null;
        }
        
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * 获取指定键的整数值
     * 
     * @param map 键值对Map
     * @param key 键名
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
        
        if (value instanceof Integer) {
            return (Integer) value;
        }
        
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            log.warn("[ServiceInvokeUtils] 无法将值转换为整数: {} = {}", key, value);
            return null;
        }
    }
    
    /**
     * 获取指定键的布尔值
     * 
     * @param map 键值对Map
     * @param key 键名
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
        
        return Boolean.valueOf(value.toString());
    }
    
    /**
     * 获取指定键的指定类型值
     * 
     * @param <T> 目标类型
     * @param map 键值对Map
     * @param key 键名
     * @param targetType 目标类型Class
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
        
        // 如果类型匹配，直接返回
        if (targetType.isInstance(value)) {
            return (T) value;
        }
        
        // 尝试类型转换
        try {
            if (targetType == String.class) {
                return (T) value.toString();
            } else if (targetType == Integer.class || targetType == int.class) {
                return (T) Integer.valueOf(value.toString());
            } else if (targetType == Long.class || targetType == long.class) {
                return (T) Long.valueOf(value.toString());
            } else if (targetType == Double.class || targetType == double.class) {
                return (T) Double.valueOf(value.toString());
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return (T) Boolean.valueOf(value.toString());
            } else {
                // 对于复杂类型，尝试JSON转换
                String jsonValue = JacksonUtils.toJson(value);
                return JacksonUtils.fromJson(jsonValue, targetType);
            }
        } catch (Exception e) {
            log.warn("[ServiceInvokeUtils] 无法将值转换为指定类型: {} = {} -> {}", 
                    key, value, targetType.getSimpleName());
            return null;
        }
    }
}