package com.employeerank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    @GetMapping({"/", "/login"})
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "login"; // same page, JS handles tab switch
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/leaderboard")
    public String leaderboard() {
        return "leaderboard";
    }

    @GetMapping("/jobs")
    public String jobs() {
        return "jobs";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/scores")
    public String scores() {
        return "scores";
    }

    @GetMapping("/results")
    public String results() {
        return "results";
    }

    @GetMapping("/company")
    public String company() {
        return "company";
    }

    @GetMapping("/public/{username}")
    public String publicProfile(@PathVariable String username) {
        return "public-profile";
    }
}
