package org.zzpj.gymapp.userservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Example {


    @Value("${spring.application.name}")
    private String name;

    @GetMapping("/api/helloWorld")
    public String helloWorld(){
        return "Hello World" + name;
    }

}
