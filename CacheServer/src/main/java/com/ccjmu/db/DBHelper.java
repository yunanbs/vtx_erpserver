package com.ccjmu.db;

import com.ccjmu.app;
import com.ccjmu.comm.ActionResult;
import com.ccjmu.comm.ServerConst;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by yunan on 2017/2/8.
 */
public class DBHelper {
    private static Logger logger = LoggerFactory.getLogger(DBHelper.class);
    private static JDBCClient jdbcpool;

    public static void inidbpool(){
        logger.info("ini DB");
        jdbcpool = JDBCClient.createShared(
                app.getvertx(),
                new JsonObject()
                        .put("url", ServerConst.DB_URL) // 获取url
                        .put("user",ServerConst.DB_USER) // 获取用户名
                        .put("password",ServerConst.DB_PASSWORD) // 获取密码
                        .put("max_pool_size", ServerConst.DB_MAX_POOL_SIZE) // 设置连接池最大连接数
                        .put("drive_class",ServerConst.DB_DRIVE_CLASS) // 设置jdbc驱动名称
                );
    }

    public static CompletableFuture<SQLConnection> getcon(){
        CompletableFuture<SQLConnection> result = new CompletableFuture<>();
        jdbcpool.getConnection(
                res->{
                    if(res.failed())
                        result.completeExceptionally(res.cause());
                    else
                        result.complete(res.result());
                }
        );
        return  result;
    }

    public static CompletableFuture<SQLConnection> starttrans(SQLConnection conn){
        CompletableFuture<SQLConnection> result = new CompletableFuture<>();
        conn.setAutoCommit(false,v->result.complete(conn));
        return result;
    }

    public static CompletableFuture<SQLConnection> committrans(SQLConnection conn){
        CompletableFuture<SQLConnection> result = new CompletableFuture<>();
        conn.commit(v->result.complete(conn));
        return result;
    }

    public static CompletableFuture<SQLConnection> rollbacktrans(SQLConnection conn){
        CompletableFuture<SQLConnection> result = new CompletableFuture<>();
        conn.rollback(v->result.complete(conn));
        return result;
    }

    public static CompletableFuture<ResultSet> Querysql(SQLConnection conn,String sql){
        CompletableFuture<ResultSet> result = new CompletableFuture<>();
        conn.query(
                sql,
                res->{
                    if(res.failed())
                        result.completeExceptionally(res.cause());
                    else
                        result.complete(res.result());
                }
        );
        return result;
    }

    public static CompletableFuture<List<Integer>> batch(SQLConnection conn,List<String> sqls){
        CompletableFuture<List<Integer>> result = new CompletableFuture<>();
        conn.batch(
                sqls,
                res->{
                    if(res.failed())
                        result.completeExceptionally(res.cause());
                    else
                        result.complete(res.result());
                }
        );
        return result;
    }

    public static CompletableFuture<ActionResult> dosqlswithtrans(List<String> sqls){
        CompletableFuture<ActionResult> result=new CompletableFuture();
        final CompletableFuture<SQLConnection> confuture = DBHelper.getcon().thenComposeAsync(DBHelper::starttrans);
        confuture.thenComposeAsync(res->DBHelper.batch(res,sqls)).whenCompleteAsync(
                (res,e)->{
                    ActionResult ar;
                    if(e!=null){
                        ar = ActionResult.GetActionResult(false,e.getMessage(),"");
                        confuture.thenComposeAsync(DBHelper::rollbacktrans).thenAccept(SQLConnection::close);
                    }else{
                        ar = ActionResult.GetActionResult(true, Json.encodePrettily(res),"");
                        confuture.thenComposeAsync(DBHelper::committrans).thenAccept(SQLConnection::close);
                    }
                    result.complete(ar);
                }
        );
        return result;
    }
}
