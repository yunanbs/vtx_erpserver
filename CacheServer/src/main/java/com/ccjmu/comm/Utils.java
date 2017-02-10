package com.ccjmu.comm;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.SimpleFormatter;

/**
 * Created by yunan on 2017/2/8.
 */
public class Utils {

    // 获取高德api返回地址中的行政区名称
    public static String getareanamefrompoi(String srcaddress){
        String result = "";
        JsonArray srcobj = new JsonObject(srcaddress).getJsonArray("pois");
        if(srcobj.size()>0)
            result = srcobj.getJsonObject(0).getString("adname");
        return result;

    }

    // Date转字符串
    public static String gettimestring(Date datetime){
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return  sdt.format(datetime);
    }

    // 获取当前时间
    public static String getcurrenttime(){
        return gettimestring(new Date());
    }

    // 基础insert语句
    public static List<String> getbaseinsertsql(String tablename,JsonArray objects){
        List<String> result = new ArrayList<>();
        String mod = ServerConst.SQL_BASEINSERT;
        StringBuilder sb = null;
        String ct = Utils.getcurrenttime();
        for (Object map:objects) {
            sb = new StringBuilder();
            JsonObject sourceobj = (JsonObject)map;
            sourceobj.put("created",ct);
            sourceobj.put("modified",ct);
            for(Map.Entry<String, Object> entry:sourceobj.getMap().entrySet())
            {
                if(entry.getValue()==null)
                    continue;
                sb.append(String.format(" %s = '%s',",entry.getKey(),entry.getValue()));
            }
            if(sb.length()>0)
                sb.deleteCharAt(sb.length()-1);
            result.add(String.format(mod,tablename,sb.toString()));
        }
        return  result;
    }

    // 获取基础条件查询语句
    public static String getbasequerysql(String tablename,JsonObject condition){
        String mod = ServerConst.SQL_BASEQUERY;
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, Object> entry:condition.getMap().entrySet())
        {
            if(entry.getValue()==null)
                continue;
            if(entry.getValue().toString().equals("#null"))
                sb.append(String.format(" %s is null and ",entry.getKey(),entry.getValue()));
            else
                sb.append(String.format(" %s = '%s' and ",entry.getKey(),entry.getValue()));
        }
        if(sb.length()>0)
        {
            sb.delete(sb.length()-5,sb.length()-1);
            sb.insert(0," where ");
        }
        return  String.format(mod,tablename,sb.toString());
    }

    // 获取基础更新语句
    public static String getbaseupdatesql(String tablename,JsonObject updata,JsonObject condition){
        String mod = ServerConst.SQL_BASEUPDATE;
        String updatasql ="";
        String wheresql = "";

        StringBuilder sb = new StringBuilder();
        updata.put("modified",Utils.getcurrenttime());

        for(Map.Entry<String, Object> entry:updata.getMap().entrySet())
        {
            if(entry.getValue()==null)
                continue;
            if(entry.getValue().toString().equals("#null"))
                sb.append(String.format(" %s = null,",entry.getKey(),entry.getValue()));
            else
                sb.append(String.format(" %s = '%s',",entry.getKey(),entry.getValue()));
        }
        if(sb.length()>0)
        {
            sb.deleteCharAt(sb.length()-1);
            updatasql = sb.toString();
            sb = new StringBuilder();
        }

        for(Map.Entry<String, Object> entry:condition.getMap().entrySet())
        {
            if(entry.getValue()==null)
                continue;
            if(entry.getValue().toString().equals("#null"))
                sb.append(String.format(" %s is null,",entry.getKey(),entry.getValue()));
            else
                sb.append(String.format(" %s = '%s' and ",entry.getKey(),entry.getValue()));
        }
        if(sb.length()>0)
        {
            sb.delete(sb.length()-5,sb.length()-1);
            sb.insert(0," where ");
            wheresql = sb.toString();
        }
        return  String.format(mod,tablename,updatasql,wheresql);
    }

    public static String trimall(String src){
        return src.replace(" ","");
    }
}
