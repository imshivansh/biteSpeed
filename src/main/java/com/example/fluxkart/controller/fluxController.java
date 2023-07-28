package com.example.fluxkart.controller;

import com.example.fluxkart.model.ContactPayLoad;
import com.example.fluxkart.service.IIdentifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/")
public class fluxController {
    @Autowired
    private IIdentifyService identifyService;

    @PostMapping("identify")
    public ResponseEntity<?> identifyUser(@RequestBody ContactPayLoad contactPayLoad){
        log.info("inside identify user controller");
        return identifyService.consolidateContactAndReturn(contactPayLoad);
    }


}
