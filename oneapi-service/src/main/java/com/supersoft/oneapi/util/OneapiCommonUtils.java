package com.supersoft.oneapi.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class OneapiCommonUtils {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            // ignore
        }
    }

    public static double shortDouble(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        BigDecimal rounded = bd.setScale(2, RoundingMode.HALF_UP);
        return rounded.doubleValue();
    }

    public static void sleepSeconds(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (Exception e) {
            // ignore
        }
    }

    public static int random(int max) {
        return secureRandom.nextInt(max);
    }

    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 今天的日期
     *
     * @return
     */
    public static String today() {
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return currentDate.format(formatter);
    }

    public static String toString(Object obj) {
        return obj == null ? null : obj.toString();
    }

    public static boolean isDev() {
        String profile = OneapiServiceLocator.getSpringProperty("spring.profiles.active");
        return "dev".equals(profile);
    }

    public static Boolean enableLog() {
        if (isDev()) {
            return true;
        }
        return OneapiConfigUtils.getConfigWithDef("log.enable", Boolean.FALSE);
    }


    public static String getEnv() {
        return OneapiServiceLocator.getSpringProperty("spring.profiles.active");
    }
}
