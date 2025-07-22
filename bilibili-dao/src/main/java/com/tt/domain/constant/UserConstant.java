package com.tt.domain.constant;

/**
 * ClassName: UserConstant
 * Package: com.tt.domain.constant
 * Description:
 *
 * @Create 2025/3/18 15:07
 */
public interface UserConstant {
    public static final String DEFAULT_NICK = "小萌新";
    public static final String GENDER_MALE = "0"; //用"0"来存储比用"男"来存储省空间
    public static final String GENDER_FEMALE = "1";
    public static final String GENDER_UNKNOW = "2";
    public static final String DEFAULT_BIRTH = "2005-05-17";
    /*新关注的up主，若无指定关注分组，则将其以默认的形式关联到默认分组里面*/
    public static final String USER_FOLLOWING_GROUP_TYPE_DEFAULT = "2";
    public static final String USER_FOLLOWING_GROUP_TYPE_USER = "3";   //用户自建的分组的默认type字段的值
    public static final String USER_FOLLOWING_GROUP_NAME_DEFAULT = "默认分组";
    //全部关注的常量，展示所有分组?
    public static final String USER_FOLLOWING_GROUP_ALL_NAME = "全部关注";

}
