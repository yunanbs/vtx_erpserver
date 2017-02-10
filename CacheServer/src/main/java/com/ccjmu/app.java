package com.ccjmu;

import com.ccjmu.comm.ServerConst;
import com.ccjmu.db.DBHelper;
import com.ccjmu.router.Index;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * Created by yunan on 2017/2/8.
 */
public class app extends AbstractVerticle{
    private static Vertx vertx;
    private static Logger logger= LoggerFactory.getLogger(app.class);

    public static void main(String...args){
        long st = System.currentTimeMillis();
        logger.warn("start app......");
        HazelcastClusterManager cmgr =new HazelcastClusterManager();
        Vertx.clusteredVertx(
                new VertxOptions().setWorkerPoolSize(ServerConst.VERTX_POOLSIZE).setClusterManager(cmgr).setClustered(true),
                res->{
                    if(res.failed())
                        logger.error("start cluster fail："+res.cause().getMessage());
                    else
                    {
                        logger.info("ini vertx");
                        vertx = res.result();
                        vertx.deployVerticle(app.class.getName(),new DeploymentOptions().setInstances(ServerConst.VERTX_LOOPSIZE));
                        DBHelper.inidbpool();
                        Index.inirouter();
                        logger.info("Start HttpServer");
                        vertx.createHttpServer(new HttpServerOptions().setPort(ServerConst.SERVER_PORT)).requestHandler(Index.getrouter()::accept).listen();
                        logger.info(String.format("Start app succeed at port %s use %s ms",String.valueOf(ServerConst.SERVER_PORT),String.valueOf(System.currentTimeMillis()-st)));
                    }
                }
        );
    }

    public static Vertx getvertx(){
        return  vertx;
    }


}
