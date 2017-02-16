package com.ccjmu.router;

import com.ccjmu.app;
import com.ccjmu.controller.CacheController;
import com.ccjmu.controller.DemoController;
import com.ccjmu.controller.JSONController;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by yunan on 2017/2/8.
 */
public class Index {
    private static Logger logger = LoggerFactory.getLogger(Index.class);
    private static Router router;
    private static DemoController demoController = new DemoController();
    private static CacheController cacheController = new CacheController();
    private static JSONController jsonController = new JSONController();

    public static void inirouter(){
        logger.info("ini Router");
        router = Router.router(app.getvertx());

        SessionStore sessionStore = ClusteredSessionStore.create(app.getvertx());
        router.route().handler(CorsHandler.create("*").allowedMethods(corsmethod()));
        router.route().handler(BodyHandler.create());
        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(sessionStore));

        router.get("/address").handler(demoController::getformaddress);
        router.get("/getformaddressex").handler(demoController::getformaddressex);
        router.get("/sqltest").handler(demoController::sqltest);
        router.get("/sqlstest").handler(demoController::sqlstest);

        router.post("/create").handler(cacheController::createcacheorder);
        router.post("/adaptorderby").handler(cacheController::adaptorderbycacheid);
        router.post("/adaptarea").handler(cacheController::adaptareabycacheid);

        //router.post("/jsoninsert").handler(jsonController::insertJsonData);
        //router.post("/jsonupdate").handler(jsonController::insertJsonData);
        //router.post("/jsonsearch").handler(jsonController::searchJsonData);
        //
        //router.post("/mass").handler(jsonController::createmass);

        // 基础查询
        // 集合查询
        router.get("/v1/basesearch/:searchtype").handler(jsonController::searcheByType);
        // 集合对象查询
        router.get("/v1/basesearch/:searchtype/:uuid").handler(jsonController::searcheByType);
        // 子集查询
        router.get("/v1/basesearch/:searchtype/:uuid/:subtype").handler(jsonController::searcheByType);
        // 子集对象查询
        router.get("/v1/basesearch/:searchtype/:uuid/:subtype/:subid").handler(jsonController::searcheByType);

        // 创建对象
        router.post("/v1/:datatype").handler(jsonController::createByType);

        // 全量更新数据(json格式不支持全量更新)
        //router.put("/v1/:datatype/:uuid").handler(jsonController::createByType);

        // 字段增量更新
        router.put("/v1/:datatype/:uuid").handler(jsonController::updateByType);
    }

    public static Router getrouter(){
        return  router;
    }

    /**
     * 添加跨域方法
     */
    private static Set<HttpMethod> corsmethod(){
        Set<HttpMethod> allowmethod;
        allowmethod = new HashSet<>();
        allowmethod.add(HttpMethod.POST);
        allowmethod.add(HttpMethod.GET);
        allowmethod.add(HttpMethod.OPTIONS);
        allowmethod.add(HttpMethod.PATCH);
        return allowmethod;
    }

}
