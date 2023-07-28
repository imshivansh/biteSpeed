package com.example.fluxkart.service;

import com.example.fluxkart.model.ContactPayLoad;
import org.springframework.http.ResponseEntity;

public interface IIdentifyService {

    ResponseEntity<?> consolidateContactAndReturn(ContactPayLoad contactPayLoad);
}
