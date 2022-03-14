package com.rlj.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author Renlingjie
 * @name 控制中心控制用户注册登录功能的开启或关闭
 * @date 2022-03-11
 */
@Configuration
@RefreshScope  //运行期间可以刷新
public class UserApplicationConfig {
    @Value("${userservice.registration.disabled}")
    private boolean disableRegistration;

    public boolean isDisableRegistration() {
        return disableRegistration;
    }

    public void setDisableRegistration(boolean disableRegistration) {
        this.disableRegistration = disableRegistration;
    }
}
