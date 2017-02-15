package com.ccjmu.comm;

import java.util.ResourceBundle;

/**
 * 系统常量
 * Created by yunan on 2017/2/8.
 */
public class ServerConst {
    private final static ResourceBundle resourceBundle = ResourceBundle.getBundle("server");

    // vertx配置
    public final static int VERTX_POOLSIZE = Integer.parseInt(resourceBundle.getString("vertx.poolsize"));
    public final static int VERTX_LOOPSIZE = Integer.parseInt(resourceBundle.getString("vertx.loopsize"));

    // Server配置
    public final static int SERVER_PORT = Integer.parseInt(resourceBundle.getString("server.port"));

    // 数据库配置
    public final static String DB_URL = resourceBundle.getString("db.url");
    public final static String DB_USER = resourceBundle.getString("db.user");
    public final static String DB_PASSWORD = resourceBundle.getString("db.password");
    public final static int DB_MAX_POOL_SIZE = Integer.parseInt(resourceBundle.getString("db.max_pool_size"));
    public final static String DB_DRIVE_CLASS = resourceBundle.getString("db.drive_class");

    // webapi配置
    public final static String WEBAPI_GAODE_HOST = resourceBundle.getString("webapi.gaode.host");
    public final static int WEBAPI_GAODE_PORT = Integer.parseInt(resourceBundle.getString("webapi.gaode.port"));
    public final static String WEBAPI_GAODE_URL = resourceBundle.getString("webapi.gaode.url");


    // sql配置
    // 基础插入语句
    public final static String SQL_BASEINSERT = resourceBundle.getString("sql.baseinsert");
    // 基础更新语句
    public final static String SQL_BASEUPDATE = resourceBundle.getString("sql.baseupdate");
    // 基础查询语句
    public final static String SQL_BASEQUERY = resourceBundle.getString("sql.basequery");




    // 更新缓存区物料表cacheorderid
    public final static String SQL_UPDATE_CACHEORDERID = resourceBundle.getString("sql.update.cacheorderid");
    // 适配缓存区物料表语句
    public final static String SQL_ADAPT_CACHEGOODS = resourceBundle.getString("sql.adapt.cachegoods");
    // 适配缓存区订单信息
    public final static String SQL_ADAPT_CACHEORDER = resourceBundle.getString("sql.adapt.cacheorder");

}
