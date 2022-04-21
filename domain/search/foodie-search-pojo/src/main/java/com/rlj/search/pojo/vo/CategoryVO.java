package com.rlj.search.pojo.vo;

import java.util.List;

//二级分类的VO
public class CategoryVO {
    private Integer id;
    private String name;
    private String type;
    private Integer fatherId;
    //三级分类VO的List
    private List<SubCategoryVo> subCatList;

    public CategoryVO() {
    }

    public CategoryVO(Integer id, String name, String type, Integer fatherId, List<SubCategoryVo> subCatList) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.fatherId = fatherId;
        this.subCatList = subCatList;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getFatherId() {
        return fatherId;
    }

    public void setFatherId(Integer fatherId) {
        this.fatherId = fatherId;
    }

    public List<SubCategoryVo> getSubCatList() {
        return subCatList;
    }

    public void setSubCatList(List<SubCategoryVo> subCatList) {
        this.subCatList = subCatList;
    }

    @Override
    public String toString() {
        return "CategoryVO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", fatherId=" + fatherId +
                ", subCatList=" + subCatList +
                '}';
    }
}
