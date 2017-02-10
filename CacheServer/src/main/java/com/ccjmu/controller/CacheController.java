package com.ccjmu.controller;

import com.ccjmu.service.CacheService;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * 缓存区Controller
 * Created by yunan on 2017/2/8.
 */
public class CacheController {

    private CacheService cacheService = new CacheService();

    public void createcacheorder(RoutingContext ctx){
        JsonObject srcobj = ctx.getBodyAsJson();
        cacheService.createcacheorder(srcobj).whenCompleteAsync((ar,e)->ctx.response().end(Json.encodePrettily(ar)));
    }

    public void adaptorderbycacheid(RoutingContext ctx){
        JsonObject srcobj = ctx.getBodyAsJson();
        cacheService.adaptorderbycacheid(srcobj.getString("cacheorderid")).whenCompleteAsync((ar,e)->ctx.response().end(Json.encodePrettily(ar)));
    }

    public void adaptareabycacheid(RoutingContext ctx){
        JsonObject srcobj = ctx.getBodyAsJson();
        cacheService.updatearea(srcobj.getString("cacheorderid")).whenCompleteAsync(
                (ar,e)->ctx.response().end(Json.encodePrettily(ar))
        );
    }


}
