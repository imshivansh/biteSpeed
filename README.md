# biteSpeed

Hi folks this is the Readme for fluxkart helps detect duplicate contact to provide a customized experience to their user.
In this project, we have taken certain scenarios into consideration.
Here, we have contactPayload that has "email" and "phoneNumber" and the request would hit our backend with this payload.
The Endpoint can be accessed at the below given commands or we can also use Postman to upload the contactpayLoad in Json format directly and hit the request
at below given biteSpeed URL and it would return a formatted JSON response.
```
curl -X POST -H "Content-Type: application/json" -d '{"email": "Shivansh", "phoneNumber": "123"}' http://bitespeed-env-1.eba-g8ymuzid.eu-north-1.elasticbeanstalk.com/identify
```

Here users can make a post request with their contact payload and System has to do the following actions depending upon inputs.
Point to be noted: All the below-given tasks  Have been implemented by having one Contact entity in our Database but this can be further improved by having Different different tables for Primary and Secondary contacts that can further simplify the coding and improve the performance.
 
1. If the user is new, it means we have no contact details about the user in our Database and we will create a new one, and will be stored in our database as
   primary precedence and the information related to him will be returned having attributes such as primaryId, phone, and email along with empty 
   secondary id.

2. If we get the payload and each attribute of it such as "phoneNumber and email" separately existing in our DB as primary contact/precedence
   then our database will have changed as follows:
   In DB the data with one of the attributes which got created first will remain primary and the other primary data which has been created later will become secondary data
   and we will denote it in our DB as having precedence Secondary and will connect it with primary precedent data to do that we will have an attribute called linkedId's which is initially
   null for all primary attributes but now it will have the id of the first primary precedent contact.

   Before doing it we also have to change the linkedId's of secondary contacts which were dependent on the Second primary attribute, which is now going to be a secondary attribute, and all the related data will have the
   id of the main primary contact as their linkedId and we will return the response having primary, phone, and email of primary and secondary contacts along the secondary contact Ids.

   Also, if we got the attribute that is different but not the primary contact instead they are the secondary contacts and have the same primary contact, then we will return the primary contact data along
   with email, phone number, and secondary of data that are secondary to it, which would include the information about the payload data as well.

3. If we get the data that exists by email but does not by phone, then we will try to find the primary precedence of the email, which could already exist and there 
   is a possibility
   that email itself could be primary precedence or could be secondary itself, if it is secondary then our program will find the primary of the secondary email and 
  create the new data with the phone number having secondary
   precedence and will have linkedId of the primarily existing data and will return the response with primaryId (being the id of the primary contact),unique email, 
   and phone number of the secondary contacts along with their id.

4. Similar goes with PhoneNumber, if we get any contact payload that has an existing phone number but email does not exist then, it will find the 
   primary precedent contact details through that number and create and save new data in our DB having the linkedId of primary contact and
   Contact precedence as "Secondary" and we will return a response that would have the primaryId of the primary existing contact, email and
   phoneNumber of primary and all the secondary contact that has the primary contact's Id as their linkedId along with all the secondary Ids.

5. If our payload has a null attribute then it will return Bad credentials as a response and if one attribute is null and the other one exist then 
   it will return all the data related to that existing contact such as the primary id of the primary contact of the existing payload attribute along with the 
  email  and phone numbers of the primary and all the secondary attributes along with secondary Ids.

6. Point to remember Applicable to all cases, if there is the same email or phone number that are been created as secondary contact because of having one of the 
   unique attributes,
   will not be repeated in our response instead unique ones  will be returned though they can be tracked with  secondaryIds, since they are unique for each.

   -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

I have built this application using Spring Boot,(Core Java, Java 8), Maven Build tool, Spring Data JPA, MYSQL 8, Lombok, Spring Web and After building it I created a jar file of the application and then hosted it on Amazon AWS using ElasticBeansStalk.
