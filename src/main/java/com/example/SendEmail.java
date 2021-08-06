// package com.example;
// import java.util.Date;
// import java.util.Properties;
// import javax.mail.Authenticator;
// import javax.mail.Message;
// import javax.mail.MessagingException;
// import javax.mail.PasswordAuthentication;
// import javax.mail.Session;
// import javax.mail.Transport;
// //import javax.mail.internet.AddressException;
// import javax.mail.internet.InternetAddress;
// import javax.mail.internet.MimeMessage;
// //import java.util.*;
// public class SendEmail {
//     public String receiverEmail;
//     public void set_receiverEmail(String email){
//         receiverEmail = email;
//     }
//     public String get_receiverEmail(){
//         return receiverEmail;
//     }
//     //code inspired from stackoverflow:
//     //https://stackoverflow.com/questions/3649014/send-email-using-java
//     public void sendalertEmail(){
//         final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
//         //String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
//         Properties props = System.getProperties();
//         props.setProperty("mail.smtp.host", "smtp.gmail.com");
//         props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
//         props.setProperty("mail.smtp.socketFactory.fallback", "false");
//         props.setProperty("mail.smtp.port", "465");
//         props.setProperty("mail.smtp.socketFactory.port", "465");
//         props.put("mail.smtp.auth", "true");
//         props.put("mail.debug", "true");
//         props.put("mail.store.protocol", "pop3");
//         props.put("mail.transport.protocol", "smtp");
//         final String username = "DineAlert@gmail.com";
//         final String password = "CMPT276Team10";
//         try{
//             Session session = Session.getDefaultInstance(props,
//                     new Authenticator(){
//                         protected PasswordAuthentication getPasswordAuthentication() {
//                             return new PasswordAuthentication(username, password);
//                         }});

//             Message msg = new MimeMessage(session);

//             msg.setFrom(new InternetAddress("DineAlert@gmail.com"));
//             msg.setRecipients(Message.RecipientType.TO,
//                     ////receiver email address
//                     InternetAddress.parse(receiverEmail,false));
//             msg.setSubject("Hello");
//             msg.setText("This is a test from DineAlert");
//             msg.setSentDate(new Date());
//             Transport.send(msg);
//             System.out.println("Message sent.");
//         }catch (MessagingException e){
//             System.out.println("Error, cause: " + e);
//         }
//     }

//     public static void main(String args[]){
//         SendEmail send = new SendEmail();
//         send.set_receiverEmail("zta23@sfu.ca");
//         send.sendalertEmail();
//     }
// }
