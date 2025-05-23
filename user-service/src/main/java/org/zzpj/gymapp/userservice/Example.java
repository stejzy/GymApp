package org.zzpj.gymapp.userservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class Example {

    @Value("${spring.application.name}")
    private String name;

    @GetMapping("/show")
    public String helloWorld(){
        return "Test" + name;
    }

}
