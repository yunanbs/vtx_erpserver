package com.ccjmu.controller;

import com.ccjmu.comm.ActionResult;
import com.ccjmu.db.DBHelper;
import com.ccjmu.service.JsonServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

/**
 * Created by yunan on 2017/2/15.
 */
public class JSONController
{
    private JsonServer js = new JsonServer();

    public void insertJsonData (RoutingContext ctx){
    JsonObject reqdata = ctx.getBodyAsJson();
    JsonObject dataobj = reqdata.getJsonObject("data");
    JsonObject headobj = reqdata.getJsonObject("head");
    JsonObject rightobj =reqdata.getJsonObject("right");
    String operator = reqdata.getString("operator");
    String datatype = reqdata.getString("datatype");
    String tablename = reqdata.getString("tablename");
    JsonServer.jsonInsert(tablename,dataobj,headobj,rightobj,datatype,operator).thenAccept(res-> ctx.response().end(Json.encodePrettily(Json.encodePrettily(res))));

}

    public void searchJsonData(RoutingContext ctx){
        String sql = ctx.getBodyAsJson().getString("sql");
        JsonServer.jsonSearch(sql).thenAccept(res-> {
                    ctx.response().end(res.getDatastr());
                }
                );
    }

    public void createmass(RoutingContext ctx){
        JsonServer.createMassData(Integer.valueOf(ctx.getBodyAsJson().getString("rows")))
                .thenAccept(res->ctx.response().end("ok"));
    }
}
