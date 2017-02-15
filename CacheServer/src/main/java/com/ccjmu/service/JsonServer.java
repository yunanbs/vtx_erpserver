package com.ccjmu.service;

import com.ccjmu.comm.ActionResult;
import com.ccjmu.comm.Utils;
import com.ccjmu.db.DBHelper;
import com.sun.org.apache.regexp.internal.REUtil;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by yunan on 2017/2/15.
 */
public class JsonServer
{
    public static CompletableFuture<ActionResult> jsonInsert(String tablename, JsonObject dataobj, JsonObject headobj, JsonObject rightobj, String logtype, String operator){
        JsonObject toinsert = new JsonObject();
        dataobj.put("uuid",Utils.getuuid());
        toinsert.put("logdata",dataobj.toString());
        toinsert.put("headdata",headobj==null?"{}":headobj.toString());
        toinsert.put("dataright",createHeadObj(rightobj,operator).toString());
        toinsert.put("logtype",logtype.toString());
        toinsert.put("logstatus","1");
        toinsert.put("operator",operator);

        List<String> insersqls = Utils.getbaseinsertsql(tablename,new JsonArray().add(toinsert));

        return  DBHelper.dosqlswithtrans(insersqls);
    }

    public static CompletableFuture<ActionResult> jsonSearch(String sql){
        CompletableFuture<ActionResult> resutl = new CompletableFuture<>();
        CompletableFuture<SQLConnection> sqlcon = DBHelper.getcon();
        sqlcon.thenComposeAsync(res->DBHelper.Querysql(res,sql)).thenAccept(res->
                {
                    try
                    {
                        sqlcon.get().close();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    resutl.complete(ActionResult.GetActionResult(true, res.getRows().toString(),""));
                }
                );
        return  resutl;
    }

    private  static JsonObject createHeadObj(JsonObject sourceobj,String operator){
        JsonObject result =sourceobj==null?new JsonObject():sourceobj;
        result.put("owner",operator);
        return result;
    }

    public  static CompletableFuture<ActionResult> createMassData(int rows){
        CompletableFuture<ActionResult> result = new CompletableFuture<>();
        DBHelper.dosqlswithtrans(createmasssqls(rows)).thenAccept(res->result.complete(res));
        return result;
    }

    private  static List<String> createmasssqls(int rows){
        long st = System.currentTimeMillis();
        JsonArray testobjs = new JsonArray();
        for(int i=0;i<rows;i++){
            JsonObject tmp = new JsonObject();
            tmp.put("massid",String.format("id %s",String.valueOf(i)));
            tmp.put("uuid",Utils.getuuid());

            JsonObject toinsert = new JsonObject();
            toinsert.put("logdata",tmp.toString());
            toinsert.put("headdata","{}");
            toinsert.put("dataright",createHeadObj(null,"test").toString());
            toinsert.put("logtype","test");
            toinsert.put("logstatus","1");
            toinsert.put("operator","test");
            testobjs.add(toinsert);
        }
        List<String> sqls = Utils.getbaseinsertsql("bigdatatest",testobjs);
        System.out.println(System.currentTimeMillis()-st);
        return  sqls;
    }

}
