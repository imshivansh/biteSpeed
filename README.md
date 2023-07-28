# biteSpeed
Hi folks this is Readme for fluxkart helps detect duplicate contact to provide a customized experience
In this project, we have taken certain scenarios into consideration.
Here, we have contactPayload that has "email" and "phoneNumber" and the request would hit our backend with this payload.
The Endpoint can be accessed at the below given commands or we can also use Postman to directly upload the contactpayLoad in Json format and hit the request
at below given biteSpeed URL.
```
curl -X POST -H "Content-Type: application/json" -d '{"email": "Shivansh", "phoneNumber": "123"}' http://bitespeed-env-1.eba-g8ymuzid.eu-north-1.elasticbeanstalk.com/identify
```

Here users can make a post request with their contact payload and System has to do the following actions depending upon inputs.
1. if the user is new, it means we have no contact details about the user in our Database and we will create a new one and will be stored in our database as
   primary precedence.
2. If we get the payload and each attribute of it such as "phoneNumber and email" are separately existing in our DB as primary contact/precedence
then our database wil have changed as follows:
In DB the data with one of the attributee which got created first will remain primary and the other primary data which has been created later will become secondary data
and we will denote it in our DB having precedence Secondary and will connect it with primary precedented data to do that we will have an attribute called linkedId's which is initially
null for all primary attributes but now it will have the id of the first primary precedented contact.
   Also, before doing it we also have to change the linkedId's of secondary contacts which were dependent on the Second primary attribute, which is now going to be a secondary attribute and all the related data will have the
   id of the main primary contact.
3. If we get the data that exists by email but does not by phone, then we will try to find the primary precedence of the email, which could already exist and there is a possibility
that email itself could be primary precedence or could be secondary itself, if it is secondary then our program will find the primary of the secondary email and create the new data with the phone number having secondary
precedence and will have linkedId of the primarily exsiting data.





I have Built this application using Spring Boot,Java Version 17,Used Spring Data JPA,MYSQL 8,Lombok,Spring web and After building it i have created the ar file of it and then
hosted on Amazon AWS using ElasticBeansStalk.
