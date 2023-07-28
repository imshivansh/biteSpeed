package com.example.fluxkart.repository;

import com.example.fluxkart.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdentifyRepository extends JpaRepository<Contact,Integer> {

    public Boolean existsByPhoneNumber(String phoneNumber);
    public  Boolean existsByEmail(String email);

    List<Contact> findByEmail(String email);


    List<Contact> findByPhoneNumber(String phoneNumber);

    List<Contact> findByLinkedId(Integer id);

    List<Contact> findByLinkPrecedenceAndLinkedId(String secondary, Integer id);
}
