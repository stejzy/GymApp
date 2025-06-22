package org.zzpj.gymapp.scheduleservice.cucumber;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.stereotype.Component;

@Component
public class StepDefinitionsContext {
    @Autowired
    public TestRestTemplate restTemplate;
    public String baseUrl;
}
