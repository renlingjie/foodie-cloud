package com.rlj.search.pojo.vo;

import java.util.List;

//最新的6个商品的VO
public class NewItemsVO {
    private Integer rootCatId;
    private String rootCatName;
    private String slogan;
    private String catImage;
    private String bgColor;
    private Integer fatherId;
    private List<SimpleItemVO> simpleItemList;

    public NewItemsVO() {
    }

    public NewItemsVO(Integer rootCatId, String rootCatName, String slogan, String catImage, String bgColor, Integer fatherId, List<SimpleItemVO> simpleItemList) {
        this.rootCatId = rootCatId;
        this.rootCatName = rootCatName;
        this.slogan = slogan;
        this.catImage = catImage;
        this.bgColor = bgColor;
        this.fatherId = fatherId;
        this.simpleItemList = simpleItemList;
    }

    public Integer getRootCatId() {
        return rootCatId;
    }

    public void setRootCatId(Integer rootCatId) {
        this.rootCatId = rootCatId;
    }

    public String getRootCatName() {
        return rootCatName;
    }

    public void setRootCatName(String rootCatName) {
        this.rootCatName = rootCatName;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String getCatImage() {
        return catImage;
    }

    public void setCatImage(String catImage) {
        this.catImage = catImage;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    public Integer getFatherId() {
        return fatherId;
    }

    public void setFatherId(Integer fatherId) {
        this.fatherId = fatherId;
    }

    public List<SimpleItemVO> getSimpleItemList() {
        return simpleItemList;
    }

    public void setSimpleItemList(List<SimpleItemVO> simpleItemList) {
        this.simpleItemList = simpleItemList;
    }

    @Override
    public String toString() {
        return "NewItemsVO{" +
                "rootCatId=" + rootCatId +
                ", rootCatName='" + rootCatName + '\'' +
                ", slogan='" + slogan + '\'' +
                ", catImage='" + catImage + '\'' +
                ", bgColor='" + bgColor + '\'' +
                ", fatherId=" + fatherId +
                ", simpleItemList=" + simpleItemList +
                '}';
    }
}
