package com.example.fluxkart.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "Contact")
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Contact {
    /*
    {
	id                   Int
  phoneNumber          String?
  email                String?
  linkedId             Int? // the ID of another Contact linked to this one
  linkPrecedence       "secondary"|"primary" // "primary" if it's the first Contact in the link
  createdAt            DateTime
  updatedAt            DateTime
  deletedAt            DateTime?
}
    */

     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Integer id ;
     @Column(name = "phoneNumber", nullable = false)
     private  String phoneNumber;
     @Column(name = "email", nullable = false)
     private String email;
     @Column(name = "linkedId", nullable = true)
     private Integer linkedId;
     @Column(name = "linkPrecedence", nullable = false)
     private String linkPrecedence;
     @Column(name = "createdAt", nullable = false)
     private LocalDateTime createdAt;
     @Column(name = "updatedAt", nullable = false)
     private  LocalDateTime updatedAt;
     private  LocalDateTime deletedAt;




     @Override
     public boolean equals(Object o) {
          if (this == o) return true;
          if (o == null || getClass() != o.getClass()) return false;
          Contact contact = (Contact) o;
          return linkedId == contact.linkedId && Objects.equals(id, contact.id) && Objects.equals(phoneNumber, contact.phoneNumber) && Objects.equals(email, contact.email) && linkPrecedence == contact.linkPrecedence && Objects.equals(createdAt, contact.createdAt) && Objects.equals(updatedAt, contact.updatedAt) && Objects.equals(deletedAt, contact.deletedAt);
     }

     @Override
     public int hashCode() {
          return Objects.hash(id, phoneNumber, email, linkedId, linkPrecedence, createdAt, updatedAt, deletedAt);
     }
}
