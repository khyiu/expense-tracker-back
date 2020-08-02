package be.kuritsu.gt.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("hello")
    @Secured("ROLE_ADMINS")
    public String hello() {
        return "hello " + SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
