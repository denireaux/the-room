package com.the_room;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    @GetMapping("/")
    public String home() {
        String homeMessage = "The chat app is running.\nYou can check the health by visiting /health\nOther endpoints -> /chat.send, /chat.hello";
        return homeMessage;
    }
}
