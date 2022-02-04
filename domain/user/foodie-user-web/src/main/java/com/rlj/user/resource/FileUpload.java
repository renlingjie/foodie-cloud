package com.rlj.user.resource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component//加入到容器中生效
@ConfigurationProperties(prefix = "file")//指定前缀
@PropertySource("classpath:file-upload-dev.properties")//指定使用类路径(resources)下的哪个配置文件
public class FileUpload {
    //该前缀下的所有文件名在这个类中就有值了，就是配置文件中的值
    private String imageUserFaceLocation;
    private String imageServerUrl;

    public String getImageServerUrl() {
        return imageServerUrl;
    }

    public void setImageServerUrl(String imageServerUrl) {
        this.imageServerUrl = imageServerUrl;
    }

    public String getImageUserFaceLocation() {
        return imageUserFaceLocation;
    }

    public void setImageUserFaceLocation(String imageUserFaceLocation) {
        this.imageUserFaceLocation = imageUserFaceLocation;
    }
}
