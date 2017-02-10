package com.ccjmu.webapi;

import com.ccjmu.app;
import com.ccjmu.comm.ActionResult;
import com.ccjmu.comm.ServerConst;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


import java.util.concurrent.CompletableFuture;

/**
 * 高德地图api
 * Created by yunan on 2017/2/8.
 */
public class GaodeAPI {
    private Logger logger = LoggerFactory.getLogger(GaodeAPI.class);
    // 获取标准地址
    public CompletableFuture<ActionResult> getformaddress(String sourceaddresss, String tag){
        logger.info(String.format("get sourceaddress:%s tag:%s",sourceaddresss,tag));
        CompletableFuture<ActionResult> resfuture = new CompletableFuture();
        HttpClient apiclient = app.getvertx().createHttpClient();
        HttpClientRequest req=apiclient.request(HttpMethod.GET,ServerConst.WEBAPI_GAODE_PORT,ServerConst.WEBAPI_GAODE_HOST,String.format(ServerConst.WEBAPI_GAODE_URL,sourceaddresss));
        req.handler(res->res.handler(buffer->{
            resfuture.complete(ActionResult.GetActionResult(true,buffer.toString(),tag));
            logger.info(String.format("return ar"));
        }));
        req.end();
        return  resfuture;
    }
}
