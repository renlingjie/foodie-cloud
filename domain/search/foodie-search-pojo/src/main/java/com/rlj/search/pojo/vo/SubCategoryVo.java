package com.rlj.search.pojo.vo;
//三级分类的VO
public class SubCategoryVo {
    private Integer subId;
    private String subName;
    private String subType;
    private Integer subFatherId;

    public SubCategoryVo() {
    }

    public SubCategoryVo(Integer subId, String subName, String subType, Integer subFatherId) {
        this.subId = subId;
        this.subName = subName;
        this.subType = subType;
        this.subFatherId = subFatherId;
    }

    public Integer getSubId() {
        return subId;
    }

    public void setSubId(Integer subId) {
        this.subId = subId;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public Integer getSubFatherId() {
        return subFatherId;
    }

    public void setSubFatherId(Integer subFatherId) {
        this.subFatherId = subFatherId;
    }

    @Override
    public String toString() {
        return "SubCategoryVo{" +
                "subId=" + subId +
                ", subName='" + subName + '\'' +
                ", subType='" + subType + '\'' +
                ", subFatherId=" + subFatherId +
                '}';
    }
}
