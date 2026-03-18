package org.lingchat.lingchatuserservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
//@EnableFeignClients
public class LingchatUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingchatUserServiceApplication.class, args);
    }

}
