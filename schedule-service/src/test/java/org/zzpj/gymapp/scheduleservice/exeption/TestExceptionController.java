package org.zzpj.gymapp.scheduleservice.exeption;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestExceptionController {

    @GetMapping("/not-found")
    public void notFound() {
        throw new EntityNotFoundException("Entity not found");
    }

    @GetMapping("/conflict")
    public void conflict() {
        throw new ScheduleConflictException("Schedule conflict occurred");
    }
}
