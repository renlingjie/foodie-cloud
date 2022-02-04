package com.rlj.item.pojo.vo;

import java.util.Date;

//用于展示商品评价的VO
public class ItemCommentVO {
    private Integer commentLevel;
    private String content;
    private String specName;
    private Date createdTime;
    private String userFace;
    private String nickName;

    public ItemCommentVO() {
    }

    public ItemCommentVO(Integer commentLevel, String content, String specName, Date createdTime, String userFace, String nickName) {
        this.commentLevel = commentLevel;
        this.content = content;
        this.specName = specName;
        this.createdTime = createdTime;
        this.userFace = userFace;
        this.nickName = nickName;
    }

    public Integer getCommentLevel() {
        return commentLevel;
    }

    public void setCommentLevel(Integer commentLevel) {
        this.commentLevel = commentLevel;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSpecName() {
        return specName;
    }

    public void setSpecName(String specName) {
        this.specName = specName;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getUserFace() {
        return userFace;
    }

    public void setUserFace(String userFace) {
        this.userFace = userFace;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public String toString() {
        return "ItemCommentVO{" +
                "commentLevel=" + commentLevel +
                ", content='" + content + '\'' +
                ", specName='" + specName + '\'' +
                ", createdTime=" + createdTime +
                ", userFace='" + userFace + '\'' +
                ", nickName='" + nickName + '\'' +
                '}';
    }
}
