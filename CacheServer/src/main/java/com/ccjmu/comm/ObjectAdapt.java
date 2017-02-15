package com.ccjmu.comm;

import io.vertx.core.json.JsonObject;

import java.util.Date;

/**
 * json对象适配
 * Created by yunan on 2017/2/8.
 */
public class ObjectAdapt {
    /**
     * cacacheorder对象适配
     * @param srcobj 原始json数据
     * @return
     */
    public static JsonObject GetOrderObj(JsonObject srcobj){
        return  new JsonObject(){{
            put("erp_sellerid",srcobj.getString("seller_id"));
            put("erp_sellername",srcobj.getString("seller_name"));
            put("erp_orderid",srcobj.getString("trade_sn"));
            put("erp_currency",srcobj.getString("currency"));
            put("erp_pocreator",srcobj.getString("po_creator"));
            put("erp_pocreatetime", srcobj.getString("po_create_time")==null?null:Utils.gettimestring(new Date(Long.valueOf(srcobj.getString("po_create_time")))));
            put("erp_paytype",srcobj.getString("pay_type"));
            put("erp_paytime",srcobj.getString("pay_time"));
            put("erp_payterms",srcobj.getString("pay_terms"));
            put("erp_ordertotalprice",srcobj.getString("order_total_price"));
            put("erp_freight",srcobj.getString("freight"));
            put("erp_contactmail",srcobj.getString("contact_mail"));
            put("erp_contactwechat",srcobj.getString("contact_wechat"));
            put("erp_contactphone",srcobj.getString("contact_phone"));
            put("erp_remark",srcobj.getString("remark"));
            put("jmu_buyerid",srcobj.getString("buyer_id"));
            put("jmu_clientid",srcobj.getString("cid"));
        }};
    }

    /**
     * 适配cacheordergoods对象
     * @param srcobj 原始json数据
     * @return
     */
    public static JsonObject GetOrdergoosObj(JsonObject srcobj){
        return  new JsonObject(){{
            put("erp_goodsid",srcobj.getString("goods_id"));
            put("erp_goodssn",srcobj.getString("goods_sn"));
            put("erp_goodsname",srcobj.getString("goods_name"));
            put("erp_unit",srcobj.getString("goods_unit"));
            put("erp_price",srcobj.getString("goods_price"));
            put("erp_amount",srcobj.getString("buy_amount"));
            put("erp_totalprice",srcobj.getString("goods_total_price"));
            put("erp_taxrate",srcobj.getString("tax_rate"));
            put("erp_expectrecetime",srcobj.getString("expect_rece_time")==null?null:Utils.gettimestring(new Date(Long.valueOf(srcobj.getString("expect_rece_time")))));
            put("erp_realrecetime",srcobj.getString("real_rece_time")==null?null:Utils.gettimestring(new Date(Long.valueOf(srcobj.getString("real_rece_time")))));
            put("erp_shippingsn",srcobj.getString("shipping_sn"));
            put("erp_shippingname",srcobj.getString("shipping_name"));
            put("receaddr",srcobj.getString("rece_addr"));
            put("recename",srcobj.getString("rece_name"));
            put("recephone",srcobj.getString("rece_phone"));
            put("rececompanyname",srcobj.getString("rece_company_name"));
            put("recearea",srcobj.getString("rece_area"));

            put("jmu_price",srcobj.getString("goods_price"));
            put("jmu_amount",srcobj.getString("buy_amount"));
            put("jmu_totalprice",srcobj.getString("goods_total_price"));
        }};
    }
}
