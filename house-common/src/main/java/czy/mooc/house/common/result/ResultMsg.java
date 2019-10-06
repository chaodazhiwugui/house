package czy.mooc.house.common.result;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/*
  封装返回给前端的信息
 */
public class ResultMsg {


    private static final String errorMsgKey = "errorMsg";

    private static final String successMsgKey = "successMsg";

    private String errorMsg;

    private String successMsg;

    public boolean isSuccess() {
        return errorMsg == null;
    }


    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getSuccessMsg() {
        return successMsg;
    }

    public void setSuccessMsg(String successMsg) {
        this.successMsg = successMsg;
    }

    public static ResultMsg errorMsg(String msg) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setErrorMsg(msg);
        return resultMsg;
    }

    public static ResultMsg successMsg(String msg) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setSuccessMsg(msg);
        return resultMsg;
    }


    //生成Map存放successMsg和errorMsg
    public Map<String, String> asMap() {
        Map<String, String> map = Maps.newHashMap();
        map.put(successMsgKey, successMsg);
        map.put(errorMsgKey, errorMsg);
        return map;
    }


    //将successMsg或errorMsg转换成url参数
    //类似于 successMsg=%E6%BF%80%E6%B4%BB%E6%88%90%E5%8A%9F
    public String asUrlParams() {
        Map<String, String> map = asMap();
        Map<String, String> newMap = Maps.newHashMap();
        map.forEach((k, v) -> {
            if (v != null)
                try {
                    newMap.put(k, URLEncoder.encode(v, "utf-8"));
                } catch (UnsupportedEncodingException e) {

                }
        });
        return Joiner.on("&").useForNull("").withKeyValueSeparator("=").join(newMap);
    }
}
