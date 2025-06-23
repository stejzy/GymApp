package org.zzpj.gymapp.authservice.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(value = "user-service", path = "/profile")
public interface UserClient {
    @PostMapping
    void createProfile(@RequestBody Map<String, Object> body);
}
