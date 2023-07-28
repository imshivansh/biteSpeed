package com.example.fluxkart.service;

import com.example.fluxkart.entity.Contact;
import com.example.fluxkart.model.ContactDTO;
import com.example.fluxkart.model.ContactPayLoad;
import com.example.fluxkart.repository.IdentifyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class IdentifyServiceImpl implements IIdentifyService{

    @Autowired
    IdentifyRepository identifyRepository;
    @Override
    public ResponseEntity<?> consolidateContactAndReturn(ContactPayLoad contactPayLoad) {
        log.info("inside service of consolidateContact method");
        String email = contactPayLoad.getEmail();
        ContactDTO contactDTO;
        String phoneNumber = contactPayLoad.getPhoneNumber();
        List<Contact> userWithPhoneNumber = new ArrayList<>();
        Contact userWithPhoneNumberHasPrimaryPrecedence=null;
        List<Contact> userWithEmail =new ArrayList<>();
        Contact userWithEmailHasPrimaryPrecedence=null;
        ResponseEntity<?>response = checkIfOneOfParameterIsNullAndReturnResponse(email,phoneNumber,contactPayLoad,userWithEmail,userWithPhoneNumber,userWithEmailHasPrimaryPrecedence,userWithPhoneNumberHasPrimaryPrecedence);
         if(!Objects.equals(response.getBody(), "Not applicable for current method"))return response;

         userWithEmail = identifyRepository.findByEmail(contactPayLoad.getEmail());
         userWithPhoneNumber = identifyRepository.findByPhoneNumber(contactPayLoad.getPhoneNumber());
        userWithPhoneNumberHasPrimaryPrecedence = updatePrimaryPrecedenceWhenPayLoadIsValidToBeInserted("phoneNumber",userWithEmail,userWithPhoneNumber);
        userWithEmailHasPrimaryPrecedence = updatePrimaryPrecedenceWhenPayLoadIsValidToBeInserted("email",userWithEmail,userWithPhoneNumber);


        if(userWithEmailHasPrimaryPrecedence!=null && userWithPhoneNumberHasPrimaryPrecedence!=null  && !userWithEmailHasPrimaryPrecedence.getId().
                equals(userWithPhoneNumberHasPrimaryPrecedence.getId())) {
           contactDTO =updateDBAndReturnDTOWhenEmailAndPhoneAreDifferentPrimaryPrecedence(userWithPhoneNumberHasPrimaryPrecedence,userWithEmailHasPrimaryPrecedence);
           //precedences found that has same primary Contact but they both are secondary contacts

        }else if(userWithEmailHasPrimaryPrecedence!=null && userWithPhoneNumberHasPrimaryPrecedence!=null){
            contactDTO = getContactWithPrimaryPrecedence(userWithEmailHasPrimaryPrecedence);
            //contact with phone number exist but not with email

        }else if(userWithPhoneNumber.size()>0 && userWithEmail.size()==0 && userWithPhoneNumberHasPrimaryPrecedence!=null){
           contactDTO = updateDBAndReturnDTOWhenPhoneNumberExistButEmailDoesNot(contactPayLoad,userWithPhoneNumberHasPrimaryPrecedence,userWithPhoneNumber);
            //contact with email exist but not with phoneNumber

          }else if (userWithEmail.size()>0 && userWithPhoneNumber.size()==0 && userWithEmailHasPrimaryPrecedence!=null) {
            updateDBAndReturnDTOWhenEmailExistButPhoneNumberDoesNot(contactPayLoad,userWithEmailHasPrimaryPrecedence,userWithEmail);
         contactDTO =getContactWithEmailAndPhoneNumberWhenEmailExist(userWithEmailHasPrimaryPrecedence);

         //creating new contact
        }else{
            contactDTO = createNewContactWhenNoOneExist(contactPayLoad);

        }

        return ResponseEntity.ok(contactDTO);
    }

    private ContactDTO createNewContactWhenNoOneExist(ContactPayLoad contactPayLoad) {
        log.info("creating new contact because no previous details were found for given contactPayload");

        String phone_number = contactPayLoad.getPhoneNumber();
        String Email = contactPayLoad.getEmail();
        String link_precedence = "Primary";
        LocalDateTime created_at= LocalDateTime.now();
        Contact contact =  Contact.builder().phoneNumber(phone_number).email(Email)
                .linkPrecedence(link_precedence).createdAt(created_at).updatedAt(created_at).deletedAt(null).build();
        Contact savedContact = identifyRepository.save(contact);
        return getContactWhenNoOneExist(savedContact);
    }

    private ContactDTO updateDBAndReturnDTOWhenEmailExistButPhoneNumberDoesNot(ContactPayLoad contactPayLoad, Contact userWithEmailHasPrimaryPrecedence, List<Contact> userWithEmail) {
        log.info("updating DB  with the help of Email,because Given phone number does not exist in DB");

        Integer linked_id = userWithEmailHasPrimaryPrecedence.getId();
        String primaryEmail = userWithEmailHasPrimaryPrecedence.getEmail();
        String link_precedence = "Secondary";
        LocalDateTime created_at = LocalDateTime.now();
        Contact contact =  Contact.builder().phoneNumber(contactPayLoad.getPhoneNumber()).email(primaryEmail).linkedId(linked_id)
                .linkPrecedence(link_precedence).createdAt(created_at).updatedAt(created_at).deletedAt(null).build();
         identifyRepository.save(contact);
        return getContactWithEmailAndPhoneNumberWhenEmailExist(userWithEmailHasPrimaryPrecedence);
    }

    private ContactDTO updateDBAndReturnDTOWhenPhoneNumberExistButEmailDoesNot(ContactPayLoad contactPayLoad, Contact userWithPhoneNumberHasPrimaryPrecedence, List<Contact> userWithPhoneNumber) {
        log.info("updating DB with the help of phoneNumber,because Given Email does not exist in DB");

        String phone_number = contactPayLoad.getPhoneNumber();
        Integer linked_id = userWithPhoneNumberHasPrimaryPrecedence.getId();
        String link_precedence = "Secondary";
        LocalDateTime created_at = LocalDateTime.now();

        Contact contact =  Contact.builder().phoneNumber(phone_number).email(contactPayLoad.getEmail()).linkedId(linked_id)
                .linkPrecedence(link_precedence).createdAt(created_at).updatedAt(created_at).deletedAt(null).build();
        identifyRepository.save(contact);
        return ContactWithEmailAndPhoneNumberWhenPhoneNumberExist(userWithPhoneNumberHasPrimaryPrecedence,userWithPhoneNumber);

    }

    private ContactDTO updateDBAndReturnDTOWhenEmailAndPhoneAreDifferentPrimaryPrecedence(Contact userWithPhoneNumberHasPrimaryPrecedence, Contact userWithEmailHasPrimaryPrecedence) {
        log.info("updating primary contact in DB when both Email and PhoneNumber belongs to different Primary contact ");

        if (userWithEmailHasPrimaryPrecedence.getCreatedAt().isBefore(userWithPhoneNumberHasPrimaryPrecedence.getCreatedAt())) {
            List<Contact>allContactsHavingPhonePrecedence = identifyRepository.findByLinkedId(userWithPhoneNumberHasPrimaryPrecedence.getId());
            LocalDateTime modifiedTime =LocalDateTime.now();
            //changing the linkedId of the secondary precedence who were dependant on their primary precedence and which is
            // about to secondary precedence of the another primary precedence which got created before
            updateSecondaryPrecedenceOfPrimary(allContactsHavingPhonePrecedence,userWithEmailHasPrimaryPrecedence,modifiedTime);

            userWithPhoneNumberHasPrimaryPrecedence.setLinkPrecedence("Secondary");
            userWithPhoneNumberHasPrimaryPrecedence.setLinkedId(userWithEmailHasPrimaryPrecedence.getId());
            userWithPhoneNumberHasPrimaryPrecedence.setUpdatedAt(modifiedTime);

            identifyRepository.save(userWithPhoneNumberHasPrimaryPrecedence);
            return getContactWithEmailAndPhoneNumberWhenBothAreFound(userWithEmailHasPrimaryPrecedence, userWithPhoneNumberHasPrimaryPrecedence);
           //when contact with Phone number registered before email
        } else {
            List<Contact>allContactsHavingEmailPrecedence = identifyRepository.findByLinkedId(userWithEmailHasPrimaryPrecedence.getId());
            LocalDateTime modifiedTime =LocalDateTime.now();
            updateSecondaryPrecedenceOfPrimary(allContactsHavingEmailPrecedence,userWithPhoneNumberHasPrimaryPrecedence,modifiedTime);

            userWithEmailHasPrimaryPrecedence.setLinkPrecedence("Secondary");
            userWithEmailHasPrimaryPrecedence.setLinkedId(userWithPhoneNumberHasPrimaryPrecedence.getId());
            userWithEmailHasPrimaryPrecedence.setUpdatedAt(modifiedTime);
            identifyRepository.save(userWithEmailHasPrimaryPrecedence);
           return getContactWithEmailAndPhoneNumberWhenBothAreFound(userWithPhoneNumberHasPrimaryPrecedence, userWithEmailHasPrimaryPrecedence);
        }
    }

    private void updateSecondaryPrecedenceOfPrimary(List<Contact> allContactsHavingPrecedence, Contact primaryPrecedence,LocalDateTime modifiedTime) {
log.info("updating secondary contact details because their parent itself being changed to Secondary contact");
        for(Contact c:allContactsHavingPrecedence){
            c.setLinkedId(primaryPrecedence.getId());
            c.setUpdatedAt(modifiedTime);
        }
    }

    private Contact updatePrimaryPrecedenceWhenPayLoadIsValidToBeInserted(String primaryPrecedenceType,List<Contact>userWithEmail,List<Contact>userWithPhoneNumber) {
        log.info("inside update function to get the primary precedence when contactPayload is valid");

        Contact  phonePrimaryPrecedence = userWithPhoneNumber.stream().filter(c -> c.getLinkPrecedence().equals("Primary")).findAny().orElse(null);
        Contact emailPrimaryPrecedence = userWithEmail.stream().filter(c -> c.getLinkPrecedence().equals("Primary")).findAny().orElse(null);
        if(primaryPrecedenceType.equals("phoneNumber")){
            log.info("getting phoneNumber primary precedence when contactPayload is valid");
            if(phonePrimaryPrecedence==null && userWithPhoneNumber.size()>0){
                phonePrimaryPrecedence = identifyRepository.findById(userWithPhoneNumber.get(0).getLinkedId()).orElse(null);
            }
            return phonePrimaryPrecedence;
        }else{
            log.info("getting email primary precedence when contactPayload is valid");
              if(emailPrimaryPrecedence==null && userWithEmail.size()>0){
                emailPrimaryPrecedence = identifyRepository.findById(userWithEmail.get(0).getLinkedId()).orElse(null);
              }
            return emailPrimaryPrecedence;
        }

    }


    private ResponseEntity<?> checkIfOneOfParameterIsNullAndReturnResponse(String email, String phoneNumber, ContactPayLoad contactPayLoad,List<Contact> userWithEmail, List<Contact> userWithPhoneNumber, Contact userWithEmailHasPrimaryPrecedence, Contact userWithPhoneNumberHasPrimaryPrecedence) {
        log.info("checking if any one or both parameter of contact payload is null or not and making changes according to it");
       ContactDTO contactDTO=new ContactDTO();
        if(email==null && phoneNumber!=null){
            log.info("inside null check function and giving results with the help of phone number");

            userWithPhoneNumber = identifyRepository.findByPhoneNumber(contactPayLoad.getPhoneNumber());
            userWithPhoneNumberHasPrimaryPrecedence = userWithPhoneNumber.stream().filter(c -> c.getLinkPrecedence().equals("Primary")).findAny().orElse(null);
            return getUserWithPhoneNumHasPrimaryPrecedence(contactDTO,userWithPhoneNumberHasPrimaryPrecedence,userWithPhoneNumber);

        }else if(email!=null && phoneNumber==null){
            log.info("inside null check function and giving results with the help of email");

            userWithEmail = identifyRepository.findByEmail(contactPayLoad.getEmail());
            userWithEmailHasPrimaryPrecedence = userWithEmail.stream().filter(c -> c.getLinkPrecedence().equals("Primary")).findAny().orElse(null);
            return getUserWithEmailHasPrimaryPrecedence(contactDTO,userWithEmailHasPrimaryPrecedence,userWithEmail);
        }else if (email==null && phoneNumber==null){
            log.info("inside null check function and returning Bad response, because of both are null credentials");

            return ResponseEntity.ok("Bad Credentials");
        }else{
            log.info("null check completed,no issues found program can move ahead to do further operation.");

            return ResponseEntity.ok("Not applicable for current method");

        }

    }

    private ResponseEntity<?> getUserWithEmailHasPrimaryPrecedence(ContactDTO contactDTO, Contact userWithEmailHasPrimaryPrecedence, List<Contact> userWithEmail) {
        log.info("getting primary contact precedence having email as primary precedence");

        if(userWithEmailHasPrimaryPrecedence==null && userWithEmail.size()>0){
            userWithEmailHasPrimaryPrecedence = identifyRepository.findById(userWithEmail.get(0).getLinkedId()).orElse(null);
        }
        if(userWithEmailHasPrimaryPrecedence!=null){
            contactDTO =getContactWithEmailAndPhoneNumberWhenEmailExist(userWithEmailHasPrimaryPrecedence);
            return ResponseEntity.ok(contactDTO);
        }else{
            return ResponseEntity.ok("Bad credentials");
        }
    }

    private ResponseEntity<?> getUserWithPhoneNumHasPrimaryPrecedence(ContactDTO contactDTO, Contact userWithPhoneNumberHasPrimaryPrecedence, List<Contact> userWithPhoneNumber) {
        log.info("getting primary contact precedence having phone number as primary precedence");

        if(userWithPhoneNumberHasPrimaryPrecedence==null && userWithPhoneNumber.size()>0){
            userWithPhoneNumberHasPrimaryPrecedence = identifyRepository.findById(userWithPhoneNumber.get(0).getLinkedId()).orElse(null);
        }
        if(userWithPhoneNumberHasPrimaryPrecedence!=null){
            contactDTO =ContactWithEmailAndPhoneNumberWhenPhoneNumberExist(userWithPhoneNumberHasPrimaryPrecedence,userWithPhoneNumber);
            return ResponseEntity.ok(contactDTO);
        }else{
            return ResponseEntity.ok("Bad credentials");
        }
    }

    private ContactDTO getContactWithPrimaryPrecedence(Contact primaryPrecedence) {
        log.info("inside general function to get and show contact info with primary precedence because here both payload has same primary contact info");

        ContactDTO contactDTO = new ContactDTO();
        contactDTO.setPrimaryContactId(primaryPrecedence.getId());

        contactDTO.addEmail(primaryPrecedence.getEmail());
        List<Contact>contacts = identifyRepository.findByLinkedId(primaryPrecedence.getId());
        contactDTO.addAllEmails(contacts.stream().map(Contact::getEmail).filter(email-> !email.equals(primaryPrecedence.getEmail())).distinct().collect(Collectors.toList()));

        contactDTO.addPhoneNumber(primaryPrecedence.getPhoneNumber());
        contactDTO.addAllPhoneNumbers(contacts.stream().map(Contact::getPhoneNumber).filter(phoneNumber -> !phoneNumber.equals(primaryPrecedence.getPhoneNumber())).distinct().collect(Collectors.toList()));
        contactDTO.setSecondaryContactIds(contacts.stream().map(Contact::getId).collect(Collectors.toList()));

       return contactDTO;
    }

    private ContactDTO getContactWhenNoOneExist(Contact savedContact) {
        log.info("getting contact information to be shown when new data is being created");

        ContactDTO contactDTO =  new ContactDTO();
        contactDTO.setPrimaryContactId(savedContact.getId());
        contactDTO.addEmail(savedContact.getEmail());
        contactDTO.addPhoneNumber(savedContact.getPhoneNumber());
        return contactDTO;
    }

    private ContactDTO ContactWithEmailAndPhoneNumberWhenPhoneNumberExist(Contact userWithPhoneNumberHasPrimaryPrecedence, List<Contact> userWithPhoneNumber) {
        log.info("getting contact information to be shown when phone number exist but email does not");

        ContactDTO contactDTO = new ContactDTO();
        String precedence = "Secondary";
        List<Contact>contacts = identifyRepository.findByLinkPrecedenceAndLinkedId(precedence,userWithPhoneNumberHasPrimaryPrecedence.getId());


        contactDTO.setPrimaryContactId(userWithPhoneNumberHasPrimaryPrecedence.getId());
        contactDTO.addPhoneNumber(userWithPhoneNumberHasPrimaryPrecedence.getPhoneNumber());
        List<String>phoneNumbers = contacts.stream().map(Contact::getPhoneNumber).filter(phoneNumber ->!phoneNumber.equals(userWithPhoneNumberHasPrimaryPrecedence.getPhoneNumber())).distinct().toList();
        if(phoneNumbers.size()>0)contactDTO.addAllPhoneNumbers(phoneNumbers);

        contactDTO.addEmail(userWithPhoneNumberHasPrimaryPrecedence.getEmail());
        List<String>emails = contacts.stream().map(Contact::getEmail).filter(email ->!email.equals(userWithPhoneNumberHasPrimaryPrecedence.getEmail())).distinct().toList();
        contactDTO.addAllEmails(emails);

        List<Integer>secondaryIds = contacts.stream().filter(c->c.getLinkPrecedence().equals("Secondary")).map(Contact::getId).toList();
        contactDTO.setSecondaryContactIds(secondaryIds);


        return contactDTO;

    }
    private  ContactDTO getContactWithEmailAndPhoneNumberWhenEmailExist(Contact userWithEmailHasPrimaryPrecedence){
        log.info("getting contact information to be shown when email exist but phone number does not");

        ContactDTO contactDTO = new ContactDTO();
        String precedence = "Secondary";
        List<Contact>contacts = identifyRepository.findByLinkPrecedenceAndLinkedId(precedence,userWithEmailHasPrimaryPrecedence.getId());


        contactDTO.setPrimaryContactId(userWithEmailHasPrimaryPrecedence.getId());

        contactDTO.addEmail(userWithEmailHasPrimaryPrecedence.getEmail());
        //there can be different or same secondary emails
        List<String>emails = contacts.stream().map(Contact::getEmail).filter(email ->!email.equals(userWithEmailHasPrimaryPrecedence.getEmail())).distinct().toList();
        if(emails.size()>0)contactDTO.addAllEmails(emails);

        contactDTO.addPhoneNumber(userWithEmailHasPrimaryPrecedence.getPhoneNumber());
        List<String>phoneNumbers = contacts.stream().map(Contact::getPhoneNumber).filter(phoneNumber ->!phoneNumber.equals(userWithEmailHasPrimaryPrecedence.getPhoneNumber())).distinct().toList();
        if(phoneNumbers.size()>0)contactDTO.addAllPhoneNumbers(phoneNumbers);

        List<Integer>secondaryIds = contacts.stream().filter(c->c.getLinkPrecedence().equals("Secondary")).map(Contact::getId).toList();
        contactDTO.setSecondaryContactIds(secondaryIds);

        return contactDTO;

    }

    private ContactDTO getContactWithEmailAndPhoneNumberWhenBothAreFound(Contact primaryContact, Contact secondaryContact) {
        log.info("getting contact information to be shown when both are found and they belong to different primary contact");

        ContactDTO contactDTO = new ContactDTO();

        contactDTO.setPrimaryContactId(primaryContact.getId());

        contactDTO.addSecondaryIds(secondaryContact.getId());

        contactDTO.addPhoneNumber(primaryContact.getPhoneNumber());
        contactDTO.addPhoneNumber(secondaryContact.getPhoneNumber());

        contactDTO.addEmail(primaryContact.getEmail());
        contactDTO.addEmail(secondaryContact.getEmail());
        return  contactDTO;

    }
}

