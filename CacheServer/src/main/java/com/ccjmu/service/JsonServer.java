package com.ccjmu.service;

import com.ccjmu.comm.ActionResult;
import com.ccjmu.comm.ServerConst;
import com.ccjmu.comm.Utils;
import com.ccjmu.db.DBHelper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by yunan on 2017/2/15.
 * jsontable 操作类
 * 表结构
 * 主键  数据json    头json      数据权限json 数据类型 状态     创建时间     修改时间     操作人uuid
 * id   datajson    headjson    rightjson   type    status  created     modified    operator
 *参数结构
 * {
 *     "datajson":{...},数据项
 *     "headjson":{...},头信息项
 *     "rightjson":{...},权限信息项
 *     "type":"XXXX",数据类型 可用于版本区分
 *     "status":"1",数据状态 1有效 0无效
 *     "operator":"XXXXXXX",操作人员uuid
 * }
 */
public class JsonServer
{
    private static Logger logger = LoggerFactory.getLogger(JsonServer.class);

    public CompletableFuture<ActionResult> jsonSearch(String sql)
    {
        CompletableFuture<ActionResult> resutl = new CompletableFuture<>();
        CompletableFuture<SQLConnection> sqlcon = DBHelper.getcon();
        sqlcon.thenComposeAsync(res -> DBHelper.Querysql(res, sql)).thenAccept(res ->
                {
                    try
                    {
                        sqlcon.get().close();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    resutl.complete(ActionResult.GetActionResult(true, res.getRows().toString(), ""));
                }
        );
        return resutl;
    }

    public CompletableFuture<ActionResult> createMassData(int rows)
    {
        CompletableFuture<ActionResult> result = new CompletableFuture<>();
        DBHelper.dosqlswithtrans(createmasssqls(rows)).thenAccept(res -> result.complete(res));
        return result;
    }

    private List<String> createmasssqls(int rows)
    {
        long st = System.currentTimeMillis();
        JsonArray testobjs = new JsonArray();
        for (int i = 0; i < rows; i++)
        {
            JsonObject tmp = new JsonObject();
            tmp.put("massid", String.format("id %s", String.valueOf(i)));
            tmp.put("uuid", Utils.getuuid());

            JsonObject toinsert = new JsonObject();
            toinsert.put("logdata", tmp.toString());
            toinsert.put("headdata", "{}");
            toinsert.put("dataright", createRightObj(null, "test").toString());
            toinsert.put("logtype", "test");
            toinsert.put("logstatus", "1");
            toinsert.put("operator", "test");
            testobjs.add(toinsert);
        }
        List<String> sqls = Utils.getbaseinsertsql("bigdatatest", testobjs);
        System.out.println(System.currentTimeMillis() - st);
        return sqls;
    }


    /**
     * 插入jsontable数据
     * @param tablename 表名称
     * @param dataobj 数据字段
     * @param headobj 头字段
     * @param rightobj 权限字段
     * @param logtype 数据类型
     * @param logstatus 数据状态
     * @param operator 操作人员（uuid）
     * @return 插入对象的uuid
     */
    public CompletableFuture<ActionResult> insertJSONData(String tablename, JsonObject dataobj, JsonObject headobj, JsonObject rightobj, String logtype, String logstatus,String operator)
    {
        CompletableFuture<ActionResult> result = new CompletableFuture<>();

        // 为对象添加uuid
        String objuuid = Utils.getuuid();
        dataobj.put("uuid", objuuid);

        // 创建插入对象
        JsonObject toinsert = new JsonObject();
        toinsert.put("datajson", dataobj.toString());
        toinsert.put("headjson", headobj == null ? "{}" : headobj.toString());
        toinsert.put("rightjson", createRightObj(rightobj, operator).toString());
        toinsert.put("type", logtype);
        toinsert.put("status", logstatus);
        toinsert.put("operator", operator);

        // 获取插入语句
        List<String> insersqls = Utils.getbaseinsertsql(tablename, new JsonArray().add(toinsert));

        // 插入语句
        DBHelper.dosqlswithtrans(insersqls).whenCompleteAsync(
                (res, e) ->
                {
                    // TODO: 2017/2/16  sql语句本身问题 无法在次捕获 需要考虑如何处理
                    if(e!=null){
                        // 失败 记录失败语句及失败信息
                        logger.error(ServerConst.LOG_FAIL,insersqls.get(0),e.getMessage());
                        result.complete(ActionResult.GetActionResult(false,e.getMessage(),""));
                    }else{
                        // 返回创建对象的uuid
                        result.complete(ActionResult.GetActionResult(true,objuuid,""));
                    }
                }
        );
        return result;
    }

    /**
     * 创建权限对象
     * @param sourceobj
     * @param operator
     *
     * @return
     */
    private JsonObject createRightObj(JsonObject sourceobj, String operator)
    {
        JsonObject result = sourceobj == null ? new JsonObject() : sourceobj;
        result.put("owner", operator);
        return result;
    }

    /**
     * 简单查询
     * @param sql 查询语句
     * @return 结果集合
     */
    public CompletableFuture<ResultSet> baseJSONSearch(String sql)
    {
        CompletableFuture<ResultSet> result = new CompletableFuture<>();
        CompletableFuture<SQLConnection> sqlcon = DBHelper.getcon();
        sqlcon.thenComposeAsync(res -> DBHelper.Querysql(res, sql)).whenCompleteAsync(
                (res, e) ->
                {
                    ResultSet rs = null;
                    try
                    {
                        sqlcon.get().close();
                        if (e != null)
                        {
                            logger.error(String.format(ServerConst.LOG_FAIL, sql, e.getMessage()));
                        } else
                        {
                            rs = res;
                        }
                    } catch (Exception ex)
                    {
                        logger.error(String.format(ServerConst.LOG_FAIL, sql, ex.getMessage()));
                    } finally
                    {
                        result.complete(rs);
                    }
                }
        );
        return result;
    }

    /**
     * 构建查询
     * @param type 对象类型
     * @param uuid 对象uuid
     * @param subtype 子集类型
     * @param subuuid 子集主键
     * @return 结果集合
     */
    public CompletableFuture<ResultSet> baseJSONSearch(String type, String uuid, String subtype, String subuuid)
    {
        String sql = createBaseQuerySql(type, uuid, subtype, subuuid);
        return baseJSONSearch(sql);
    }

    /**
     * 构建查询语句
     * @param type 对象类型
     * @param uuid 对象uuid
     * @param subtype 子集类型
     * @param subuuid 子集主键
     * @return 查询语句
     */
    private String createBaseQuerySql(String type, String uuid, String subtype, String subuuid)
    {
        String result = "";
        String finalType = subtype == "" ? type : subtype;
        List<String> conditions = new ArrayList<>();

        try
        {
            Class<?> serverConst = Class.forName("com.ccjmu.comm.ServerConst");
            result = serverConst.getField(String.format("SQL_JSON_LIST_%s", finalType.toUpperCase())).get(serverConst).toString();
            if (subtype.length() > 0)
            {
                conditions.add(String.format("headjson->'$.*' like '%%%s%%'", uuid));
                conditions.add(String.format("datajson->'$.uuid' like '%%%s%%'", subuuid));
            } else
            {
                conditions.add(String.format("datajson->'$.uuid' like '%%%s%%'", uuid));
            }

            result += String.format(" where %s ", String.join(" and ", conditions));
        } catch (Exception ex)
        {
            logger.error(String.format(ServerConst.LOG_FAIL, finalType, ex.getMessage()));
        }
        return result;
    }

    /**
     * 更新jsontable数据
     * @param type 数据类型
     * @param uuid 数据uuid
     * @param newdata 更新数据json
     * @return
     */
    public CompletableFuture<ActionResult> updateJSONData(String type,String uuid,JsonObject newdata){
        CompletableFuture<ActionResult> result = new CompletableFuture<>();
        String objuuid = uuid;
        List<String> datas = new ArrayList<>();
        datas.addAll(getUpdatedata(newdata));
        datas.addAll(getJSONUpdatedata(newdata,"datajson"));
        datas.addAll(getJSONUpdatedata(newdata,"headjson"));
        datas.addAll(getJSONUpdatedata(newdata,"rightjson"));
        datas.add(String.format("modified='%s'",Utils.getcurrenttime()));

        List<String> updatesql = new ArrayList<>();
        updatesql.add(String.format(ServerConst.SQL_BASEUPDATE,type,String.join(",",datas),String.format("where datajson->'$.uuid'='%s'",objuuid)));

        DBHelper.dosqlswithtrans(updatesql).whenCompleteAsync(
                (res,e)->{
                    if(e!=null){
                        logger.error(ServerConst.LOG_FAIL,updatesql.get(0),e.getMessage());
                        result.complete(ActionResult.GetActionResult(false,e.getMessage(),""));
                    }else{
                        result.complete(ActionResult.GetActionResult(true,objuuid,""));
                    }
                }
        );
        return result;
    }


    private List<String> getUpdatedata(JsonObject newdata){
        List<String> datas = new ArrayList<>();
        String[] chekckfields = new String[]{"type","status","operator"};
        for(String field : chekckfields){
            if(newdata.getString(field)!=null){
                datas.add(String.format("%s='%s'",field,newdata.getString(field)));
            }
        }
        return  datas;
    }

    private List<String> getJSONUpdatedata(JsonObject newdata,String jsonType){
        List<String> datas = new ArrayList<>();
        JsonObject src = newdata.getJsonObject(jsonType);

        if(src==null){
            return datas;
        }

        if(jsonType=="datajson"){
            src.remove("uuid");
        }

        for(String field : src.fieldNames()){
            String jsonVal = src.getString(field);
            if(jsonVal.equals("")){
                datas.add(String.format("%s = JSON_REMOVE(%s,'$.%s')",jsonType,jsonType,field));
            }else{
                datas.add(String.format("%s = JSON_SET(%s,'$.%s','%s')",jsonType,jsonType,field,jsonVal));
            }
        }
        return  datas;
    }
}
