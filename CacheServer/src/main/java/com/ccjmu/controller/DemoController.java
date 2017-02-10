package com.ccjmu.controller;

import com.ccjmu.comm.ActionResult;
import com.ccjmu.comm.Utils;
import com.ccjmu.db.DBHelper;
import com.ccjmu.webapi.GaodeAPI;
import io.vertx.core.Future;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by yunan on 2017/2/8.
 */
public class DemoController {

    private static Logger logger = LoggerFactory.getLogger(DemoController.class);
    private GaodeAPI gaodeAPI = new GaodeAPI();

    public void getformaddress(RoutingContext ctx){
        String path = ctx.currentRoute().getPath();
        logger.info(String.format("%s",path));
        long st=System.currentTimeMillis();
        String sourceadd = ctx.request().getParam("address");
        CompletableFuture<ActionResult> gaode = gaodeAPI.getformaddress(sourceadd,"test");
        List<String> results = new ArrayList<>();

        gaode.whenComplete((s,e)->{
            if(e!=null){
                ctx.response().end(e.getMessage());
                logger.error(String.format("route : %s fail %s",path,e.getMessage()));
            }else{
                String ss = Utils.getareanamefrompoi(s.getDatastr()+s.getTag());
                ctx.response().end(ss);
                logger.info(String.format("route : %s succeed %s ms",path,String.valueOf(System.currentTimeMillis()-st)));
            }
        });

    }

    public void getformaddressex(RoutingContext ctx){
        String path = ctx.currentRoute().getPath();
        logger.info(String.format("%s",path));
        long st=System.currentTimeMillis();
        //String sourceadd = ctx.request().getParam("address");
//        List<String> adds = new ArrayList<>();
//        adds.add("上海友谊路1016号");
//        adds.add("杨高北路38885");
        List<String> result = new ArrayList<>();
        CompletableFuture<ActionResult> gaode1 = gaodeAPI.getformaddress("上海市 杨浦区 长江南路888号","test1");
        CompletableFuture<ActionResult> gaode2 = gaodeAPI.getformaddress("上海市 杨浦区 长江南路888号","test1");

        CompletableFuture<List<String>> gaode3 = new CompletableFuture<>();

        CompletableFuture.allOf(gaode1,gaode2).whenCompleteAsync((s,e)->{
            if(e!=null){
                ctx.response().end(e.getMessage());
                logger.error(String.format("route : %s fail %s",path,e.getMessage()));
            }else{
                try {

                    result.add(gaode1.get().getDatastr());
                    result.add(gaode2.get().getDatastr());
                    gaode3.complete(result);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (ExecutionException e1) {
                    e1.printStackTrace();
                }
            }
        });

        gaode3.whenCompleteAsync((res,e)->ctx.response().end(Json.encodePrettily(result)));

    }

    public void sqltest(RoutingContext ctx){
        String sql = "select count(*) from goods_maps";
        CompletableFuture<SQLConnection> conn = DBHelper.getcon();
        conn.thenComposeAsync(con->DBHelper.Querysql(con,sql)).whenCompleteAsync(
                (res,e)->{
                    ActionResult ar = null;
                    try {
                        conn.get().close();
                        if(e!=null)
                            ar = ActionResult.GetActionResult(false,e.getMessage(),"");
                        else
                            ar = ActionResult.GetActionResult(true,Json.encodePrettily(res),"");
                    } catch (Exception ex) {
                        ar = ActionResult.GetActionResult(false,e.getMessage(),"");
                    }
                    finally {
                        ctx.response().end(Json.encodePrettily(ar));
                    }
                }
        );
    }

    public void sqlstest(RoutingContext ctx){
        List<String> sqls = new ArrayList<>();
        sqls.add("insert into yutest set name=1");
        sqls.add("insert into yutest set name=2");
        sqls.add("insert into yutest set name=3,checkid='test'");

        try {
            final CompletableFuture<SQLConnection> future = DBHelper.getcon().thenComposeAsync(res->DBHelper.starttrans(res));
            future.thenComposeAsync(res->DBHelper.batch(res,sqls))
                    .whenComplete(
                            (res,e)->{
                                ActionResult ar=null;
                                if(e!=null){
                                    ar = ActionResult.GetActionResult(false,e.getMessage(),"");
                                    future.thenComposeAsync(sres->DBHelper.rollbacktrans(sres)).thenAccept(sres->sres.close());
                                }else{
                                    ar = ActionResult.GetActionResult(true,Json.encodePrettily(res),"");
                                    future.thenComposeAsync(sres->DBHelper.committrans(sres)).thenAccept(sres->sres.close());
                                }
                                ctx.response().end(Json.encodePrettily(ar));
                            }
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
