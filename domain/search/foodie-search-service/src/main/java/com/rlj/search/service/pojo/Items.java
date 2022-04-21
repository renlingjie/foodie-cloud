package com.rlj.search.service.pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

//indexName就是我们同步使用的索引的名称--foodie-items-ik。createIndex表示没有索引的情况下是否创建一个
//索引，默认是会创建的（类似于JPA一个类创建后加上@Table可以创建一个表），这里我们已经创建了，虽不影响，但置为false
// 大坑!!6.3.0自动创建的"mappings"下面的是"doc"，而7.9.3则是"_doc"!!需要在这里指定type为doc，否则会用_doc
@Document(indexName = "foodie-items-ik",createIndex = false,type = "doc")
public class Items {
    @Id
    //设置为true表示要进行存储，同时是以text类型存储，同时不进行倒排索引
    @Field(store = true,type = FieldType.Text,index = false)
    private String itemId;  //这里的一条field文档（记录）的ID实际上是表中的itemId

    //允许按照itemName进行倒排索引
    @Field(store = true,type = FieldType.Text,index = true)
    private String itemName;

    @Field(store = true,type = FieldType.Text,index = false)
    private String imgUrl;

    @Field(store = true,type = FieldType.Integer)
    private Integer price;
    @Field(store = true,type = FieldType.Integer)
    private Integer sellCounts;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getSellCounts() {
        return sellCounts;
    }

    public void setSellCounts(Integer sellCounts) {
        this.sellCounts = sellCounts;
    }

    @Override
    public String toString() {
        return "Items{" +
                "itemId='" + itemId + '\'' +
                ", itemName='" + itemName + '\'' +
                ", imgUrl=" + imgUrl +
                ", price=" + price +
                ", sellCounts=" + sellCounts +
                '}';
    }
}
