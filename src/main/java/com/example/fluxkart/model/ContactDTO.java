package com.example.fluxkart.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@Builder
public class ContactDTO {
    private int primaryContactId;
    private List<String>emails;
    private List<String>phoneNumbers;
    private List<Integer>secondaryContactIds;

    public ContactDTO(){
        this.emails=new ArrayList<>();
        this.phoneNumbers=new ArrayList<>();
        this.secondaryContactIds=new ArrayList<>();

    }

    public void addSecondaryIds(Integer secondaryId){
        this.secondaryContactIds.add(secondaryId);
    }
    public void addEmail(String email){
        this.emails.add(email);
    }
    public void addAllEmails(List<String>email){
        this.emails.addAll(email);
    }
     public void addPhoneNumber(String phoneNumber){this.phoneNumbers.add(phoneNumber);}
    public void addAllPhoneNumbers(List<String>phoneNumbers){
        this.phoneNumbers.addAll(phoneNumbers);
    }

}
