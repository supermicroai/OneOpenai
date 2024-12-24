package com.supersoft.oneapi.rest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.supersoft.oneapi.common.OneapiSingleResult;
import com.supersoft.oneapi.util.OneapiReflectionUtils;
import com.supersoft.oneapi.util.OneapiServiceLocator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 *
 */
@Controller
@Slf4j
public class MainController {

    /**
     * 健康检查，系统部署需要
     */
    @GetMapping("/checkpreload.htm")
    public @ResponseBody String checkPreload() {
        return "success";
    }

    /**
     * 通用facade接口调用服务
     * @param service
     * @param method
     * @param body
     * @return
     */
    @PostMapping("/service/{service}/{method}")
    public @ResponseBody Object invoke(HttpServletRequest request,
                                       @PathVariable("service") String service,
                                       @PathVariable("method") String method,
                                       @RequestBody String body) {
        if (StringUtils.isBlank(service)) {
            return OneapiSingleResult.fail("Service not found");
        }
        Class serviceClass = OneapiReflectionUtils.getClass(service);
        if (serviceClass == null) {
            return OneapiSingleResult.fail("Service not found");
        }
        if (StringUtils.isBlank(method)) {
            return OneapiSingleResult.fail("Method not found");
        }
        Method serviceMethod = OneapiReflectionUtils.getMethod(serviceClass, method);
        if (serviceMethod == null) {
            return OneapiSingleResult.fail("Method not found");
        }
        Object serviceInstance = OneapiServiceLocator.getBean(serviceClass);
        JSONArray array = JSON.parseArray(body);
        Type[] types = serviceMethod.getGenericParameterTypes();
        if (array == null || types.length != array.size()) {
            return OneapiSingleResult.fail("Invalid argument count");
        }
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Object arg = array.get(i);
            args[i] = JSON.parseObject(JSON.toJSONString(arg), types[i]);
        }
        try {
            return serviceMethod.invoke(serviceInstance, args);
        } catch (Exception e) {
            log.error("Failed to invoke service method", e);
            return OneapiSingleResult.fail("Failed to invoke service method");
        }
    }

}
