package com.ccjmu.comm;

/**
 * 通用操作结果
 * Created by yunan on 2017/2/8.
 */
public class ActionResult {
    // 操作结果标记位
    private String resultflag="";
    // 操作结果数据
    private String datastr="";
    // 错误信息数据
    private String errstr="";
    // 附加字段
    private String tag="";

    public static ActionResult GetActionResult(boolean issucceed,String contentstr,String taginfo) {
        if (issucceed) return new ActionResult("true", contentstr, "", taginfo);
        else return new ActionResult("false", "",contentstr, taginfo);
    }

    public ActionResult(String resultflag, String datastr, String errstr, String tag) {
        this.resultflag = resultflag;
        this.datastr = datastr;
        this.errstr = errstr;
        this.tag = tag;
    }

    public String getResultflag() {
        return resultflag;
    }

    public void setResultflag(String resultflag) {
        this.resultflag = resultflag;
    }

    public String getDatastr() {
        return datastr;
    }

    public void setDatastr(String datastr) {
        this.datastr = datastr;
    }

    public String getErrstr() {
        return errstr;
    }

    public void setErrstr(String errstr) {
        this.errstr = errstr;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
