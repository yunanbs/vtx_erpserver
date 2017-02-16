package com.ccjmu.controller;

import com.ccjmu.comm.ActionResult;
import com.ccjmu.comm.ServerConst;
import com.ccjmu.db.DBHelper;
import com.ccjmu.service.JsonServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by yunan on 2017/2/15.
 */
public class JSONController
{
    private JsonServer jsonServer = new JsonServer();

    //region testmath
    //public void insertJsonData(RoutingContext ctx)
    //{
    //    JsonObject reqdata = ctx.getBodyAsJson();
    //    JsonObject dataobj = reqdata.getJsonObject("data");
    //    JsonObject headobj = reqdata.getJsonObject("head");
    //    JsonObject rightobj = reqdata.getJsonObject("right");
    //    String operator = reqdata.getString("operator");
    //    String datatype = reqdata.getString("datatype");
    //    String tablename = reqdata.getString("tablename");
    //    //jsonServer.jsonInsert(tablename, dataobj, headobj, rightobj, datatype, operator).thenAccept(res -> ctx.response().end(Json.encodePrettily(Json.encodePrettily(res))));
    //
    //}
    //
    //public void searchJsonData(RoutingContext ctx)
    //{
    //    String sql = ctx.getBodyAsJson().getString("sql");
    //    jsonServer.jsonSearch(sql).thenAccept(res ->
    //            {
    //                ctx.response().end(res.getDatastr());
    //            }
    //    );
    //}
    //
    //public void createmass(RoutingContext ctx)
    //{
    //    jsonServer.createMassData(Integer.valueOf(ctx.getBodyAsJson().getString("rows")))
    //            .thenAccept(res -> ctx.response().end("ok"));
    //}
    //endregion

    public void searcheByType(RoutingContext ctx)
    {
        String type = ctx.request().getParam("searchtype");
        String uuid = ctx.request().getParam("uuid")==null?"":ctx.request().getParam("uuid");
        String subtype = ctx.request().getParam("subtype")==null?"":ctx.request().getParam("subtype");
        String subid = ctx.request().getParam("subid")==null?"":ctx.request().getParam("subid");

        CompletableFuture<ResultSet> dataset = jsonServer.baseJSONSearch(type,uuid,subtype,subid);
        dataset.thenAccept(res -> ctx.response().end(res.getRows().toString()));
    }

    public void createByType(RoutingContext ctx)
    {
        String type = ctx.request().getParam("datatype");
        JsonObject srcObj = ctx.getBodyAsJson();
        JsonObject dataObj = srcObj.getJsonObject("datajson")==null?new JsonObject():ctx.getBodyAsJson().getJsonObject("datajson");
        JsonObject headObj = srcObj.getJsonObject("headjson")==null?new JsonObject():ctx.getBodyAsJson().getJsonObject("headjson");
        JsonObject rightObj = srcObj.getJsonObject("rightjson")==null?new JsonObject():ctx.getBodyAsJson().getJsonObject("rightjson");

        CompletableFuture<ActionResult> dataset = jsonServer.insertJSONData(type,dataObj,headObj,rightObj,srcObj.getString("type"),srcObj.getString("status")==null?"1":srcObj.getString("status"),srcObj.getString("operator"));
        dataset.thenAccept(res -> ctx.response().end(Json.encodePrettily(res)));
    }

    public void updateByType(RoutingContext ctx){
        String type = ctx.request().getParam("datatype");
        String uuid = ctx.request().getParam("uuid");
        CompletableFuture<ActionResult> dataset = jsonServer.updateJSONData(type,uuid,ctx.getBodyAsJson());
        dataset.thenAccept(res -> ctx.response().end(Json.encodePrettily(res)));
    }



    
}
