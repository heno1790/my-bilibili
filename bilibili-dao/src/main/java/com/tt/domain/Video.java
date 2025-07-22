package com.tt.domain;

import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;

/**
 * ClassName: Video
 * Package: com.tt.domain
 * Description:
 *
 * @Create 2025/6/12 11:25
 */
//标注这个类对象的信息在搜索引擎中对应的标签，这样才能存到正确的地方，或者从正确的地方查出数据（必须要有的注解）
//@Document(indexName = "videos")
public class Video {
    //标识该字段在ES中以主键id的方式进行存储
    private Long id;
    private Long userId;
    private String url;//视频链接（对应上传文件返回的path）
    private String thumbnail;//封面链接
    private String title;
    private String type;
    private String duration;
    private String area; //所在分区
    private String description;//视频简介

    private Date createTime;
    private Date updateTime;
    private List<VideoTag> videoTagList;//冗余字段,视频的标签列表

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUrl() {
        return url;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public void setVideoTagList(List<VideoTag> videoTagList) {
        this.videoTagList = videoTagList;
    }

    public String getDuration() {
        return duration;
    }

    public String getArea() {
        return area;
    }

    public String getDescription() {
        return description;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public List<VideoTag> getVideoTagList() {
        return videoTagList;
    }
}
