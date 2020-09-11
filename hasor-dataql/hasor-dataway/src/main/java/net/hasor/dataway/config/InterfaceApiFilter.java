/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dataway.config;
import com.alibaba.fastjson.JSON;
import net.hasor.core.Inject;
import net.hasor.core.spi.SpiTrigger;
import net.hasor.dataway.daos.ApiDataAccessLayer;
import net.hasor.dataway.daos.EntityDef;
import net.hasor.dataway.daos.FieldDef;
import net.hasor.dataway.service.ApiCallService;
import net.hasor.dataway.spi.ApiInfo;
import net.hasor.dataway.spi.CallSource;
import net.hasor.utils.StringUtils;
import net.hasor.web.Invoker;
import net.hasor.web.InvokerChain;
import net.hasor.web.InvokerConfig;
import net.hasor.web.InvokerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 负责处理 API 的执行
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-03-20
 */
class InterfaceApiFilter implements InvokerFilter {
    protected static Logger             logger = LoggerFactory.getLogger(InterfaceApiFilter.class);
    @Inject
    private          ApiCallService     callService;
    @Inject
    private          SpiTrigger         spiTrigger;
    @Inject
    private          ApiDataAccessLayer dataAccessLayer;
    private          String             apiBaseUri;

    public InterfaceApiFilter(String apiBaseUri) {
        this.apiBaseUri = apiBaseUri;
    }

    @Override
    public void init(InvokerConfig config) {
        config.getAppContext().justInject(this);
    }

    @Override
    public Object doInvoke(Invoker invoker, InvokerChain chain) throws Throwable {
        HttpServletRequest httpRequest = invoker.getHttpRequest();
        HttpServletResponse httpResponse = invoker.getHttpResponse();
        String requestURI = invoker.getRequestPath();
        String httpMethod = httpRequest.getMethod().toUpperCase().trim();
        if (!requestURI.startsWith(this.apiBaseUri)) {
            return chain.doNext(invoker);
        }
        //
        DatawayUtils.resetLocalTime();
        String mimeType = invoker.getMimeType("json");
        httpRequest.setCharacterEncoding("UTF-8");
        httpResponse.setCharacterEncoding("UTF-8");
        CorsUtils.setup(invoker);
        //
        // .查询接口数据
        ApiInfo apiInfo = new ApiInfo();
        apiInfo.setCallSource(CallSource.External);
        String apiPath = URLDecoder.decode(requestURI, "UTF-8");
        String script = null;
        try {
            Map<FieldDef, String> object = this.dataAccessLayer.getObjectBy(EntityDef.RELEASE, FieldDef.PATH, apiPath);
            apiInfo.setReleaseID(object.get(FieldDef.ID));
            apiInfo.setApiID(object.get(FieldDef.API_ID));
            apiInfo.setMethod(object.get(FieldDef.METHOD));
            apiInfo.setApiPath(object.get(FieldDef.PATH));
            apiInfo.setOptionMap(JSON.parseObject(object.get(FieldDef.OPTION)));
            script = object.get(FieldDef.SCRIPT);
        } catch (Exception e) {
            Object result = DatawayUtils.exceptionToResult(e).getResult();
            LoggerUtils loggerUtils = LoggerUtils.create()  //
                    .addLog("httpMethod", httpMethod)       //
                    .addLog("apiPath", apiPath)             //
                    .addLog("result", result)               //
                    .logException(e);
            logger.error("requestFailed - " + loggerUtils.toJson(), e);
            return DatawayUtils.responseData(this.spiTrigger, apiInfo, mimeType, invoker, result);
        }
        //
        // .准备参数
        Map<String, Object> jsonParam;
        if ("GET".equalsIgnoreCase(httpMethod)) {
            jsonParam = new HashMap<>();
            Enumeration<String> parameterNames = httpRequest.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                jsonParam.put(paramName + "Arrays", httpRequest.getParameterValues(paramName));
                jsonParam.put(paramName, httpRequest.getParameter(paramName));
            }
        } else {
            String jsonBody = invoker.getJsonBodyString();
            if (StringUtils.isNotBlank(jsonBody)) {
                jsonParam = JSON.parseObject(jsonBody);
            } else {
                jsonParam = new HashMap<>();
            }
        }
        apiInfo.setParameterMap(jsonParam);
        //
        // .执行调用
        String finalScript = script;
        Object objectMap = this.callService.doCallWithoutError(apiInfo, param -> finalScript);
        return DatawayUtils.responseData(this.spiTrigger, apiInfo, mimeType, invoker, objectMap);
    }
}