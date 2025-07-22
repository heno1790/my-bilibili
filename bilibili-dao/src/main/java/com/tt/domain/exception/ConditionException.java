package com.tt.domain.exception;

/**
 * ClassName: ConditionException
 * Package: com.tt.domain.exception
 * Description:
 *
 * @Create 2025/3/17 21:01
 */
public class ConditionException extends RuntimeException{
    private static final long serialVersionUID = 1L; //序列化UID,java对象的序列化反序列化传输时要用到

    private String code;//区别于父类，这个异常类多了一个属性，即状态码

    public ConditionException(String code, String name) {
        super(name);//所有的异常共有的属性，异常的描述信息，或者说是异常的名字
        this.code = code;//指定的异常状态码
    }

    public ConditionException(String name) {
        super(name);
        code= "500";//统一的异常状态码
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
