package com.supersoft.oneapi.util;

/**
 * 布尔值工具类
 * 统一处理不同数据库的布尔值转换
 */
public class BooleanUtils {
    
    /**
     * 将Boolean转换为Integer
     * true -> 1, false -> 0, null -> 0
     * 
     * @param value Boolean值
     * @return Integer值
     */
    public static Integer toInteger(Boolean value) {
        if (value == null) {
            return 0;
        }
        return value ? 1 : 0;
    }
    
    /**
     * 将Integer转换为Boolean
     * 1 -> true, 0 -> false, 其他 -> false
     * 
     * @param value Integer值
     * @return Boolean值
     */
    public static Boolean toBoolean(Integer value) {
        if (value == null) {
            return false;
        }
        return value == 1;
    }
    
    /**
     * 检查Integer值是否为true（启用状态）
     * 
     * @param value Integer值
     * @return true if value == 1
     */
    public static boolean isTrue(Integer value) {
        return value != null && value == 1;
    }
    
    /**
     * 检查Integer值是否为false（禁用状态）
     * 
     * @param value Integer值
     * @return true if value == 0 or null
     */
    public static boolean isFalse(Integer value) {
        return value == null || value == 0;
    }
    
    /**
     * 检查Integer值是否为启用状态（1）
     * 
     * @param value Integer值
     * @return true if value == 1
     */
    public static boolean isEnabled(Integer value) {
        return isTrue(value);
    }
    
    /**
     * 检查Integer值是否为禁用状态（0）
     * 
     * @param value Integer值
     * @return true if value == 0 or null
     */
    public static boolean isDisabled(Integer value) {
        return isFalse(value);
    }
}

