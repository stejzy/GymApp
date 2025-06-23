package org.zzpj.gymapp.authservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzpj.gymapp.authservice.exception.UnauthorizedException;
import org.zzpj.gymapp.authservice.exception.UserAlreadyExistsException;
import org.zzpj.gymapp.authservice.exception.UserNotFoundException;

@RestController
@RequestMapping("/test")
public class TestExceptionController {

    @GetMapping("/user-not-found")
    public void throwUserNotFoundException() {
        throw new UserNotFoundException();
    }

    @GetMapping("/unauthorized")
    public void throwUnauthorizedException() {
        throw new UnauthorizedException();
    }

    @GetMapping("/user-already-exists")
    public void throwUserAlreadyExistsException() {
        throw new UserAlreadyExistsException("User already exists");
    }

    @GetMapping("/illegal-argument")
    public void throwIllegalArgumentException() {
        throw new IllegalArgumentException("Invalid argument");
    }

    @GetMapping("/generic-exception")
    public void throwGenericException() {
        throw new RuntimeException("Something went wrong");
    }
} 