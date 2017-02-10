package com.ccjmu.service;

import com.ccjmu.comm.ActionResult;
import com.ccjmu.comm.ObjectAdapt;
import com.ccjmu.comm.ServerConst;
import com.ccjmu.comm.Utils;
import com.ccjmu.db.DBHelper;
import com.ccjmu.webapi.GaodeAPI;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 缓存区处理服务
 * Created by yunan on 2017/2/8.
 */
public class CacheService {

    private Logger logger = LoggerFactory.getLogger(CacheService.class);
    private GaodeAPI gaodeAPI = new GaodeAPI();

    // 创建缓存订单
    public CompletableFuture<ActionResult> createcacheorder(JsonObject srcobj){
        // TODO: 2017/2/8 校验已存在订单
        // TODO: 2017/2/10 校验输入参数  
        
        List<String> sqls = new ArrayList<>();

        JsonArray cacheorders = new JsonArray();
        cacheorders.add(ObjectAdapt.GetOrderObj(srcobj));

        JsonArray cachegoods = new JsonArray();
        for(Object goods:srcobj.getJsonArray("items")){
            JsonObject goodsobj = (JsonObject)goods;
            cachegoods.add(
                    ObjectAdapt.GetOrdergoosObj(goodsobj)
                            .put("erp_orderid",srcobj.getString("trade_sn"))
                            .put("erp_sellerid",srcobj.getString("seller_id"))
                            .put("jmu_clientid",srcobj.getString("cid"))
            );
        }
        sqls.addAll(Utils.getbaseinsertsql("erp_cache_orders",cacheorders));
        sqls.addAll(Utils.getbaseinsertsql("erp_cache_ordergoods",cachegoods));
        sqls.add(String.format(ServerConst.SQL_UPDATE_CACHEORDERID,srcobj.getString("trade_sn"),srcobj.getString("cid")));

        return DBHelper.dosqlswithtrans(sqls);
    }

    //适配指定的订单数据 -> 物料编码转换 单位转换 数量折算 单价折算 总价计算
    public CompletableFuture<ActionResult> adaptorderbycacheid(String cacheorderid){
        List<String> sqls = new ArrayList<>();

        sqls.add(Utils.getbaseupdatesql(
                "erp_cache_ordergoods",
                new JsonObject().put("adaptstate","-1").put("jmu_goodsid","#null").put("jmu_sellerid","#null").put("jmu_storeid","#null").put("jmu_unit","#null"),
                new JsonObject().put("cacheorderid",cacheorderid)
                )
        );
        sqls.add(String.format(ServerConst.SQL_ADAPT_CACHEGOODS,cacheorderid));
        sqls.add(String.format(ServerConst.SQL_ADAPT_CACHEORDER,cacheorderid));
        return DBHelper.dosqlswithtrans(sqls);
    }

    // 更新缓存区物料信息的送货区域字段 使用高德地图获取送货地址字段
    public CompletableFuture<ActionResult> updatearea(String cacheorderid){
        CompletableFuture<List<String>> sqlsfuture = new CompletableFuture<>();
        final CompletableFuture<SQLConnection> connfuture = DBHelper.getcon();
        connfuture.thenComposeAsync(res->DBHelper.Querysql(res,Utils.getbasequerysql("erp_cache_ordergoods",new JsonObject().put("cacheorderid",cacheorderid))))
                .thenComposeAsync(res->
                        {
                            try
                            {
                                connfuture.get().close();
                            } catch (Exception ex)
                            {
                                logger.error(ex.getMessage());
                            }
                            return getupdataareasql(res);
                        })
                .whenCompleteAsync((res,e)->{
                    sqlsfuture.complete(res);
                });
        return sqlsfuture.thenComposeAsync(DBHelper::dosqlswithtrans);
    }

    // 更新区域信息
    private CompletableFuture<List<String>> getupdataareasql(ResultSet resource){
        CompletableFuture<List<String>> result = new CompletableFuture<>();
        List<CompletableFuture> completableFuturelist = new ArrayList<>();
        for(JsonObject datarow:resource.getRows()){
            if(datarow.getString("receaddr").length()>0) {
                CompletableFuture<ActionResult> sub = gaodeAPI.getformaddress(Utils.trimall(datarow.getString("receaddr")), String.valueOf(datarow.getInteger("id")));
                completableFuturelist.add(sub);
            }
        }
        List<String> todosqls = new ArrayList<>();
        CompletableFuture[] completableFutures=new CompletableFuture[completableFuturelist.size()];
        CompletableFuture[] finalCompletableFutures = completableFuturelist.toArray(completableFutures);
        CompletableFuture.allOf(finalCompletableFutures).whenCompleteAsync((v,ex)->{
            try {

                for(CompletableFuture<ActionResult> sub: finalCompletableFutures){
                    ActionResult tmpar = sub.get();

                    todosqls.add(Utils.getbaseupdatesql(
                            "erp_cache_ordergoods",
                            new JsonObject().put("recearea",Utils.getareanamefrompoi(tmpar.getDatastr())),
                            new JsonObject().put("id",tmpar.getTag())));
                }
                result.complete(todosqls);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return result;
    }
}
