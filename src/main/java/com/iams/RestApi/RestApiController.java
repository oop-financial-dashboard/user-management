package com.iams.RestApi;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/oop")
public class RestApiController {

    @GetMapping
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("you are authenticated!");
    }


}
