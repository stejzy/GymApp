package org.zzpj.gymapp.scheduleservice.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "org.zzpj.gymapp.scheduleservice.cucumber",
        plugin = {"pretty", "html:target/cucumber-reports.html"}
)
public class CucumberTest {
}
