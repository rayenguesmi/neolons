package com.neo.app.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/context")
public class ContextController {


    @GetMapping("/all")
    public Map<String,String> getAllContext(){
        Map<String,String> response = new HashMap<>();
        response.put("Author","Eya");
        response.put("Time",new Date().toString());
      return response;
    }
}
