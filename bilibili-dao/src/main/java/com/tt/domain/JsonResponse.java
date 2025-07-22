package com.tt.domain;

/**
 * ClassName: JsonResponse
 * Package: com.tt.domain
 * Description:
 *
 * @Create 2025/3/17 16:58
 */
public class JsonResponse<T> {
    private String code; //状态码
    private String msg;  //提示信息
    private T data; //返回的数据

    //只返回状态，不返回数据的构造方法
    public JsonResponse(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    //返回数据和状态的的构造方法
    public JsonResponse(T data) {
        this.data = data;
        msg = "成功";
        code = "0";
    }

    //统一给前端返回对象,下面这些静态方法是直接返回给前端的API方法
    public static JsonResponse<String> success() {
        return new JsonResponse<>(null);
    }

    public static JsonResponse<String> success(String data) {
        return new JsonResponse<>(data);
    }

    //这里其实泛型是啥都行，因为这个fail方法不用返回data给前端(data没有赋值）
    public static JsonResponse<String> fail() {
        return new JsonResponse<>("1", "失败");
    }

    public static JsonResponse<String> fail(String code, String msg) {
        return new JsonResponse<>(code, msg);
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
