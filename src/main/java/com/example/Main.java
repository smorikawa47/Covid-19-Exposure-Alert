/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.tomcat.jni.Local;
import org.apache.tomcat.util.buf.UEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.WebUtils;

//import jdk.internal.logger.LocalizedLoggerWrapper;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Collections;
// import java.time.*;
import java.sql.Time;
import java.time.LocalDate;
//import java.time.LocalDate;
import java.sql.Date;

// import javax.mail.Authenticator;
// import javax.mail.Message;
// import javax.mail.MessagingException;
// import javax.mail.PasswordAuthentication;
// import javax.mail.Session;
// import javax.mail.Transport;
// import javax.mail.internet.AddressException;
// import javax.mail.internet.InternetAddress;
// import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@SpringBootApplication
public class Main {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  

  private static Map<String, User> loggedInUsers = new HashMap<String, User>();
  private static List<String> defaultDineAlertTableNames = Arrays.asList("Users", "Diners", "Restaurants", "Dinings");
  private static User loggedInUser = new User();
  private static final String SQL_DINERS_INITIALIZER = "CREATE TABLE IF NOT EXISTS Diners (id serial, username varchar(30), name varchar(30), email varchar(30), password varchar(30), exposed boolean, exposure date, testresult boolean, testresultdate date)";
  private static final String SQL_RESTAURANTS_INITIALIZER = "CREATE TABLE IF NOT EXISTS Restaurants (id serial, username varchar(30), name varchar(30), password varchar(30), premium boolean, exposure date, latitude float, longitude float)";
  private static final String SQL_DININGS_INITITALIZER = "CREATE TABLE IF NOT EXISTS Dinings (id serial, restaurant varchar(30), username varchar(30), name varchar(30), email varchar(30), time time, date date, exposed boolean)";
  private static final String SQL_USERS_INITIALIZER = "CREATE TABLE IF NOT EXISTS Users (id serial, username varchar(30), name varchar(30), email varchar(30), password varchar(30), restaurant boolean)";
  private static final String USERNAME_DOES_NOT_EXIST = "Username not found in system.";
  private static final String PASSWORD_DOES_NOT_MATCH = "Password does not match.";
  private static final String USERNAME_ALREADY_IN_USE = "That username is already in use, please try another.";
  private static final String EMAIL_ALREADY_IN_USE = "That email is already in use, please try another!";
  private static final String COVID_REPORTED = "You have reported a positive COVID-19 test result.";
  private static final String COVID_EXPOSED = "A restaurant which you dined at in the past two weeks may have experienced a COVID-19 exposure!";
  private static final String YOU_HAVE_NOT_DINED = "You haven't dined at any DineAlert restaurants yet!";
  private static final String RESTAURANT_NOT_FOUND = "That restaurant has not registered with DineAlert yet!";
  private static final String YOU_HAVE_NOT_BEEN_EXPOSED = "You have no recent COVID-19 exposures at DineAlert restaurants.";

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  public DataSource getDataSource(){
    return this.dataSource;
  }

  @RequestMapping("/")
  String index(HttpServletResponse response, HttpServletRequest request, Map<String, Object> model) throws SQLException {
    //destroyDatabaseTables(defaultDineAlertTableNames);      // These two utility functions are used to wipe or delete the database. Use them with caution.
    //resetDatabaseTables(defaultDineAlertTableNames);        // To use them, uncomment them, run the app, and navigate to the index.
    createDefaultDineAlertTables();                          // Creates the tables for the app, if they don't yet exist.
    refreshAllDinersExposedBasedOnExposureDate();
    refreshAllDinersTestResultBasedOnTestResultDate();
    refreshLoggedInUserFromCookies(request, response);
    syncDiningExposures();
    //deleteCertainRestaurant("Boats and Hoes");                  
    return "index";
  }

  @RequestMapping("index")
  String index1(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) throws SQLException {
    refreshLoggedInUserFromCookies(request, response);
    return "index";
  }

  @GetMapping("/home")
  public String getAdminHomePage(Map<String, Object> model, Restaurant restaurant){
    model.put("restaurant", restaurant);
    return "home";
  }

  //Log out of the app. Deletes the username cookie and nulls loggedInUser.

  @PostMapping("/logout")
  public String logout(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model){
    System.out.println("Logging out...");
    System.out.println("Deleting cookie...");
    if(savedUsernameExistsInCookies(request, "username")){
      deleteUsernameCookie(response);
    }
    System.out.println("Clearing logginInUsers...");
    loggedInUsers.clear();
    System.out.println("loggedInUsers = " + loggedInUsers.toString());
    User user = new User ();
    System.out.println("Clearing loggedInUser...");
    loggedInUser = user;
    System.out.println("loggedInUser = " + loggedInUser);
    return "redirect:/index";
  }

  @GetMapping("/thankyou")
  public String thankDinerForReport(Map<String, Object> model){
    return "thankyou";
  }

  @GetMapping("/successfulreport")
  public String thankDinerForReport2(Map<String, Object> model){
    return "successfulreport";
  }

  @GetMapping("/reportlogin")
  public String reportLogin(Map<String, Object> model){
    Diner diner = new Diner();
    model.put("diner", diner);
    return "reportlogin";
  }

  @GetMapping("/dinerlogin")
  public String dinerLogin(Map<String, Object> model){
    Diner diner = new Diner();
    model.put("diner", diner);
    return "dinerlogin";
  }

  @PostMapping("/reportloginpage")
  public String reportLoginPage(Map<String, Object> model){
    Diner diner = new Diner();
    model.put("diner", diner);
    return "reportlogin";
  }

  @PostMapping("/dinerloginpage")
  public String dinerLoginPage(Map<String, Object> model){
    Diner diner = new Diner();
    model.put("diner", diner);
    return "dinerlogin";
  }

  @PostMapping(
    path = "/reportlogin",
    consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE}
    )
  public String loginToDinerAccount2(Map<String, Object> model, Diner diner) throws Exception {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      Statement stmt2 = connection.createStatement();
      Diner d = new Diner();
      d.setUsername(diner.getUsername());
      d.setName(diner.getName());
      d.setEmail(diner.getEmail());
      d.setPassword(diner.getPassword());
      d.setExposed(diner.wasExposed());
      System.out.println(d);
      diner.setExposed(true);
      stmt.executeUpdate(SQL_DINERS_INITIALIZER);
      //stmt.executeUpdate("ALTER TABLE Dinings ADD COLUMN IF NOT EXISTS exposed boolean");
      String sql = "SELECT * FROM Diners WHERE username = '" + diner.getUsername() + "'";
      System.out.println(sql);
      ResultSet queriedUser = stmt.executeQuery(sql);

      if(!queriedUser.isBeforeFirst()){
        model.put("message", USERNAME_DOES_NOT_EXIST);
        return "reportlogin";
      }
      queriedUser.next();
      
      String queriedPassword = queriedUser.getString("password");
      System.out.println(queriedPassword);
      System.out.println(diner.getPassword());
      if(!(diner.getPassword().equals(queriedPassword))){
        model.put("message", PASSWORD_DOES_NOT_MATCH);
        return "reportlogin";
      }

      String queriedEmail = queriedUser.getString("email");
      String sql2 = "SELECT * FROM Dinings WHERE email = '" + queriedEmail + "'";
      System.out.println(sql2);
      ResultSet isDined = stmt.executeQuery(sql2);
      if(!isDined.isBeforeFirst()){
        model.put("message", YOU_HAVE_NOT_DINED);
        return "reportlogin";
      }
      isDined.next();

      ResultSet diner2 = stmt.executeQuery("SELECT * FROM Diners WHERE username = '" + diner.getUsername() + "'");
      List<List<String>> recs = new ArrayList<>();
      while(diner2.next()){
        String id = diner2.getString("id");
        String username = diner2.getString("username");
        String name = diner2.getString("name");
        String email = diner2.getString("email");
        String password = diner2.getString("password");
        String exposed = diner2.getString("exposed");
        ArrayList<String> rec = new ArrayList<>();
        rec.add(id);
        rec.add(username);
        rec.add(name);
        rec.add(email);
        rec.add(password);
        rec.add(exposed);
        recs.add(rec);

        String q1 = "UPDATE Dinings SET exposed = '" + diner.wasExposed() + "'";
        String q2 = q1 + " WHERE email = '" + email + "'";
        System.out.println(q2);
        stmt2.executeUpdate(q2);
      }
      model.put("recs", recs);
      return "successfulreport";

    }catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @PostMapping(  
  path = "/covidreport")
  public String reportCovidTestResult(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) throws SQLException {
    System.out.println("Reporting COVID-19...");
    String sql = "";
    refreshLoggedInUserFromCookies(request, response);
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate(SQL_DININGS_INITITALIZER);
      sql = "SELECT * FROM Dinings WHERE username = '" + loggedInUser.getUsername() + "'";
      ResultSet queriedDining = stmt.executeQuery(sql);
      if(!queriedDining.isBeforeFirst()){
        System.out.println("No dinings!");
        // model.put("message", YOU_HAVE_NOT_DINED);
        // model.put("diner", loggedInUser);
        model.put("message", YOU_HAVE_NOT_DINED);
        return getActionFromDinerLoginStatus(request, "redirect:/diningreport", "redirect:/index");
      }
      queriedDining.next();

      ((Diner) loggedInUser).setTestResult(true);
      LocalDate testResultDate = LocalDate.now();
      //System.out.println(exposureDate);
      ((Diner) loggedInUser).setTestResultDate(testResultDate);
      Date sqlTestResultDate = java.sql.Date.valueOf(testResultDate);
      LocalDate twoWeeksBeforeTestResult = testResultDate.plusDays(-14);
      Date sqlTestResultDateMinusTwoWeeks = java.sql.Date.valueOf(twoWeeksBeforeTestResult);
      //System.out.println(sqlExposureDate);

      sql = "UPDATE Dinings SET exposed = '" + ((Diner) loggedInUser).getTestResult() + "' WHERE Username = '" + loggedInUser.getUsername() + "' AND Date > '" + sqlTestResultDateMinusTwoWeeks +"'";
      System.out.println(sql);
      stmt.executeUpdate(sql);
      
      stmt.executeUpdate(SQL_DINERS_INITIALIZER);
      //stmt.executeUpdate("ALTER TABLE Diners ADD COLUMN IF NOT EXISTS exposed boolean");
      sql = "UPDATE Diners SET testresult = '" + ((Diner) loggedInUser).getTestResult() + "' WHERE Username = '" + loggedInUser.getUsername() + "'";
      System.out.println(sql);
      stmt.executeUpdate(sql);
      sql = "UPDATE Diners SET testresultdate = '" + sqlTestResultDate + "' WHERE Username = '" + loggedInUser.getUsername() + "'";
      System.out.println(sql);
      stmt.executeUpdate(sql);

      stmt.executeUpdate(SQL_RESTAURANTS_INITIALIZER);
      Map<String, LocalDate> recentDinings = getRestaurantsAUserHasDinedAtRecently(loggedInUser.getUsername(), testResultDate);
      updateRestaurantExposures(recentDinings);
      updateDinerExposures(recentDinings);
      model.put("message", COVID_REPORTED);
      model.put("diner", loggedInUser);
      return "redirect:/diningreportpage";
      //return getActionFromDinerLoginStatus(request, "redirect:/diningreport", "redirect:/index");
    } catch (Exception e) {
      System.out.println("An SQL Error occured: " + e.getMessage());
      return "error";
    }
  }

  public void refreshAllDinersExposedBasedOnExposureDate(){
    System.out.println("Refresh diner exposure based on when they were exposed...");
    Date twoWeeksAgo = Date.valueOf(LocalDate.now().plusDays(-14));
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "UPDATE Diners SET exposed = true WHERE exposure >= '" + twoWeeksAgo + "'";
      stmt.executeUpdate(sql);
      sql = "UPDATE Diners SET exposed = false WHERE exposure < '" + twoWeeksAgo + "'";
      stmt.executeUpdate(sql);
      sql = "UPDATE Diners SET exposed = false WHERE exposure IS NULL";
      stmt.executeUpdate(sql);
    }
    catch (Exception e) {
      System.out.println("An SQL error occurred trying to refresh Diner exposed status: " + e.getMessage());
    }
  }

  public void refreshAllDinersTestResultBasedOnTestResultDate(){
    System.out.println("Refresh diner testresult based on when they were tested...");
    Date twoWeeksAgo = Date.valueOf(LocalDate.now().plusDays(-14));
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "UPDATE Diners SET testresult = true WHERE testresultdate >= '" + twoWeeksAgo + "'";
      stmt.executeUpdate(sql);
      sql = "UPDATE Diners SET testresult = false WHERE testresultdate < '" + twoWeeksAgo + "'";
      stmt.executeUpdate(sql);
      sql = "UPDATE Diners SET testresult = false WHERE testresultdate IS NULL";
      stmt.executeUpdate(sql);
    }
    catch (Exception e) {
      System.out.println("An SQL error occurred trying to refresh Diner exposed status: " + e.getMessage());
    }
  }

  public void updateExposuresForDiningsAtRestaurant(String restaurantName, LocalDate dateOfExposure){
    System.out.println("Setting dinings at " + restaurantName + " on " + dateOfExposure + " to be exposed to COVID-19.");
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "UPDATE Dinings SET exposed = true WHERE date = '" + dateOfExposure + "' AND restaurant = '" + restaurantName + "'";
      stmt.executeUpdate(sql);
    }
    catch (Exception e) {
      System.out.println("An SQL error occurred trying to refresh Dining exposed status: " + e.getMessage());
    }
  }

  public List<String> getAllDinerUsernames(){
    System.out.println("Getting the usernames of all Diners...");
    List<String> usernames = new ArrayList<String>();
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "SELECT Username FROM Diners";
      ResultSet queriedUsernames = stmt.executeQuery(sql);
      while(queriedUsernames.next()){
        usernames.add(queriedUsernames.getString("username"));
      }
      return usernames;
    } catch (Exception e) {
      System.out.println("An SQL error occurred trying get all Diner usernames: " + e.getMessage());
      return usernames;
    }
  }

  public void updateDinerExposuresFromDinings(){
    List<String> dinerUsernames = getAllDinerUsernames();
    System.out.println("Refreshing Diner exposures dates from their dinings.");
    LocalDate dateWhenExposed = LocalDate.now();

    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      for (String dinerUsername : dinerUsernames){
        if(userHasDined(dinerUsername)){
          Map<String, LocalDate> exposure = mostRecentExposedDiningAttendedByADiner(dinerUsername);
          if(!exposure.isEmpty()){
            System.out.println(dinerUsername + " had recent exposures. Updating their exposed date.");
            for(Map.Entry<String, LocalDate> exposureEntry : exposure.entrySet()){
              String exposedRestaurantName = exposureEntry.getKey();
              dateWhenExposed = exposureEntry.getValue();
              System.out.println("There was an exposure for " + dinerUsername + " at " + exposedRestaurantName + " on " + dateWhenExposed);
              String sql = "UPDATE Diners SET exposure = '" + dateWhenExposed + "' WHERE username = '" + dinerUsername + "'";
              System.out.println(sql);
              stmt.executeUpdate(sql);
              sql = "UPDATE Dinings SET exposed = true WHERE username = '" + dinerUsername + "' AND date = '" + dateWhenExposed + "'";
              System.out.println(sql);
              stmt.executeUpdate(sql); 
            }
          } else {
            System.out.println(dinerUsername + " Has not attended any exposed dinings. Not updating their exposed date.");
          }
          
        } else {
          System.out.println(dinerUsername + " Has not dined. Not updating their exposed date.");
        }
      }
      refreshAllDinersExposedBasedOnExposureDate();
    } catch (Exception e) {
      System.out.println("An SQL error occurred trying to Update Diner exposures from their Dinings: " + e.getMessage());
    }
  }

  public Boolean dinerHasBeenExposed(String username){
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "SELECT exposed FROM diners WHERE username = '" + username + "' AND exposed = true";
      ResultSet res = stmt.executeQuery(sql);
      if(!res.isBeforeFirst()){
        return false;
      }
      res.next();
      Boolean TF = false;
      while(res.next()){
        TF = res.getBoolean("exposed");
      }
      return true;
    }
    catch (Exception e) {
      System.out.println("An SQL Error occured trying to determine if a Diner had been exposed: " + e.getMessage());
      return false;
    }
  }
  
  public void syncDiningExposures(){
    System.out.println("Syncing dining exposures...");
    List<Map<LocalDate, String>> dinings = getAllRecentlyExposedDinings();
    System.out.println("Found " + dinings.size() + " exposed dinings.");
    try (Connection connection = dataSource.getConnection()){
      Statement stmt = connection.createStatement();
      String sql = "";
      for(Map<LocalDate, String> dining : dinings){
        for(Map.Entry<LocalDate, String> diningEntry : dining.entrySet()){
          Date dateDined = Date.valueOf(diningEntry.getKey());
          String restaurant = diningEntry.getValue();
          sql = "UPDATE Dinings SET exposed = true WHERE date = '" + dateDined + "' AND restaurant = '" + restaurant + "'";
          //System.out.println("Syncing dinings: " + sql);
          stmt.executeUpdate(sql);
          sql = "UPDATE Restaurants SET exposure = '" + dateDined + "' WHERE username = '" + restaurant + "'";
          stmt.executeUpdate(sql);
        }
      }
    } catch (Exception e){
      System.out.println("There was an SQL error while attempting to sync dining exposures: " + e.getMessage());
    }
  }

  public Boolean dinerHasARecentPositiveTestResult(String username){
    Date twoWeeksAgo = Date.valueOf(LocalDate.now().plusDays(-14));
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "SELECT exposed FROM diners WHERE username = '" + username + "' AND testresult = true AND testresultdate > '" + twoWeeksAgo + "'";
      ResultSet res = stmt.executeQuery(sql);
      if(!res.isBeforeFirst()){
        return false;
      }
      res.next();
      Boolean TF = false;
      while(res.next()){
        TF = res.getBoolean("exposed");
      }
      return true;
    }
    catch (Exception e) {
      System.out.println("An SQL Error occured trying to determine if a Diner had a positive test result: " + e.getMessage());
      return false;
    }
  }

  public String generateCOVIDAlertMessage(String username){
    Map<String, LocalDate> exposure = getDinersMostRecentExposure(username);
    String restaurantWhereExposed = "";
    LocalDate dateWhenExposed = LocalDate.now();
    for(Map.Entry<String, LocalDate> exposureEntry : exposure.entrySet()){
      restaurantWhereExposed = exposureEntry.getKey();
      dateWhenExposed = exposureEntry.getValue();
    }
    String monthString = dateWhenExposed.getMonth().toString();
    Integer day = dateWhenExposed.getDayOfMonth();
    String weekDay = dateWhenExposed.getDayOfWeek().toString();
    String message = "You may have been exposed to COVID-19 on " + weekDay + ", " + monthString + " " + day + " at " + restaurantWhereExposed + "!";
    System.out.println(message);
    return message;
  }

  public Map<String, LocalDate> getDinersMostRecentExposure(String username){
    List<Map<LocalDate, String>> exposures = getDinersRecentExposures(username);
    Map<String, LocalDate> mostRecentExposure = new HashMap<>();
    if(exposures.isEmpty()){
      return mostRecentExposure;
    }
    List<LocalDate> dates = new ArrayList<LocalDate>();
    for(Map<LocalDate, String> exposure : exposures){
      for(Map.Entry<LocalDate, String> exposureEntry : exposure.entrySet()){
        dates.add(exposureEntry.getKey());
      }
    }
    LocalDate mostRecentExposureDate = Collections.max(dates);
    String mostRecentRestaurantName = "";
    for(Map<LocalDate, String> exposure : exposures){
      mostRecentRestaurantName = exposure.get(mostRecentExposureDate);
    } 
    System.out.println("The most recent exposure was on..." + mostRecentExposureDate + " at " + mostRecentRestaurantName + ".");
    mostRecentExposure.put(mostRecentRestaurantName, mostRecentExposureDate);
    return mostRecentExposure;
  }

  public List<Map<LocalDate, String>> getDinersRecentExposures(String username){
    List<Map<LocalDate, String>> exposures = new ArrayList<Map<LocalDate, String>>();
    LocalDate date = LocalDate.now();
    System.out.println("Finding recent restaurants...");
    try (Connection connection = dataSource.getConnection()){
        Statement stmt = connection.createStatement();
        Date TodayMinusTwoWeeks = Date.valueOf(date.plusDays(-14));
        String sql = "SELECT Restaurant, Date FROM Dinings WHERE Username = '" + username + "' AND Date >= '" + TodayMinusTwoWeeks + "' AND Exposed = true";
        ResultSet queriedRestaurants = stmt.executeQuery(sql);
        System.out.println("Found the following restaurants: " + queriedRestaurants.toString());
        while(queriedRestaurants.next()){
          String restaurant = queriedRestaurants.getString("restaurant");
          LocalDate dateDined = queriedRestaurants.getDate("date").toLocalDate();
          //System.out.println(username + " recently dined at " + restaurant + " on " + dateDined.toString() + "!");
          Map<LocalDate, String> exposure = new HashMap<>();
          exposure.put(dateDined, restaurant);
          exposures.add(exposure);
        }
        System.out.println(exposures);
        return exposures;
      
    } catch (Exception e) {
      System.out.println("An SQL Error occured: " + e.getMessage());
      return exposures;
    }
  }

  public Map<String, LocalDate> mostRecentExposedDiningAttendedByADiner(String username){
    Map<String, LocalDate> mostRecentExposedDining = new HashMap<>();
    if(!userHasDined(username)){
      return mostRecentExposedDining;
    }
    List<Map<LocalDate, String>> dinings = getAllRecentlyExposedDinings();
    System.out.println("Found " + dinings.size() + " exposed dinings.");
    List<Map<LocalDate, String>> allDiningsForDiner = new ArrayList<Map<LocalDate, String>>();
    //Map<String, LocalDate> mostRecentExposedDining = new HashMap<>();
    try (Connection connection = dataSource.getConnection()){
      Statement stmt = connection.createStatement();
      String sql = "";
      for(Map<LocalDate, String> dining : dinings){
        for(Map.Entry<LocalDate, String> diningEntry : dining.entrySet()){
          //System.out.println("Checking if " + username + " attended an exposed dining...");
          String restaurantName = diningEntry.getValue();
          //System.out.println("Exposed restaurant: " + restaurantName);
          Date dateDined = Date.valueOf(diningEntry.getKey());
          //System.out.println("Exposed date: " + diningEntry.getKey());
          sql = "SELECT * FROM Dinings WHERE Username = '" + username + "' AND restaurant = '" + restaurantName + "' AND date = '" + dateDined + "'";
          System.out.println(sql);
          ResultSet queriedDining = stmt.executeQuery(sql);
          if(queriedDining.isBeforeFirst()){
            //System.out.println(username + " did dine at an exposed dining; " + restaurantName + " on " + dateDined + ".");
            Map<LocalDate, String> diningForDiner = new HashMap<>();
            diningForDiner.put(diningEntry.getKey(), restaurantName);
            allDiningsForDiner.add(diningForDiner);
          }
        }
      }
     
      if(!allDiningsForDiner.isEmpty()){
        System.out.println("Finding most recent exposed dining for " + username);
        List<LocalDate> dates = new ArrayList<LocalDate>();
        for(Map<LocalDate, String> dining : allDiningsForDiner){
          for(Map.Entry<LocalDate, String> diningEntry : dining.entrySet()){
            dates.add(diningEntry.getKey());
          }
        }
        LocalDate latestDate = Collections.max(dates);
        String latestRestaurant = "";
        for(Map<LocalDate, String> dining : allDiningsForDiner){
          latestRestaurant = dining.get(latestDate);
        }
        mostRecentExposedDining.put(latestRestaurant, latestDate);
        return mostRecentExposedDining;
      }

      System.out.println(username + " did not dine at an exposed dining.");
      return mostRecentExposedDining;
    } catch (Exception e){
      System.out.println("There was an SQL error attempting to determine if a user dined at an exposed dining: " + e.getMessage());
      return mostRecentExposedDining;
    }
  }

  public List<Map<LocalDate, String>> getAllRecentlyExposedDinings(){
    List<Map<LocalDate, String>> exposures = new ArrayList<Map<LocalDate, String>>();
    LocalDate date = LocalDate.now();
    System.out.println("Finding all dinings which had an exposure...");
    try (Connection connection = dataSource.getConnection()){
        Statement stmt = connection.createStatement();
        Date TodayMinusTwoWeeks = Date.valueOf(date.plusDays(-14));
        String sql = "SELECT Restaurant, Date FROM Dinings WHERE Date >= '" + TodayMinusTwoWeeks + "' AND Exposed = true";
        System.out.println(sql);
        ResultSet queriedRestaurants = stmt.executeQuery(sql);
        //System.out.println("Found the following restaurants: " + queriedRestaurants.toString());
        while(queriedRestaurants.next()){
          String restaurant = queriedRestaurants.getString("restaurant");
          LocalDate dateDined = queriedRestaurants.getDate("date").toLocalDate();
          //System.out.println(restaurant + " had an exposure on " + dateDined.toString() + "!");
          Map<LocalDate, String> exposure = new HashMap<>();
          exposure.put(dateDined, restaurant);
          exposures.add(exposure);
        }
        //System.out.println(exposures);
        return exposures;
      
    } catch (Exception e) {
      System.out.println("An SQL Error occured: " + e.getMessage());
      return exposures;
    }
  }

  public Boolean userHasDined(String username){
    try (Connection connection = dataSource.getConnection()){
      Statement stmt = connection.createStatement();
      String sql = "SELECT * FROM Dinings WHERE username = '" + username + "'";
      ResultSet queriedDinings = stmt.executeQuery(sql);
      if(!queriedDinings.isBeforeFirst()){
        System.out.println(username + " has never reported a Dining with DineAlert.");
        return false;
      }
      System.out.println(username + " has reported Dinings!");
      return true;
    } catch (Exception e){
      System.out.println("An SQL error occurred when attempting to check if a user has dined: " + e.getMessage());
      return false;
    }
  }

  public Map<String, LocalDate> getRestaurantsAUserHasDinedAt(String username){
    Map<String, LocalDate> restaurants = new HashMap<>();
    System.out.println("Finding all restaurants that " + username + " has dined at...");
    if(userHasDined(username)){
      try (Connection connection = dataSource.getConnection()){
        Statement stmt = connection.createStatement();
        String sql = "SELECT Restaurant, Date FROM Dinings WHERE Username = '" + username + "'";
        ResultSet queriedRestaurants = stmt.executeQuery(sql);
        //System.out.println("Found the following restaurants: " + queriedRestaurants.toString());
        while(queriedRestaurants.next()){
          String restaurant = queriedRestaurants.getString("restaurant");
          LocalDate dateDined = queriedRestaurants.getDate("date").toLocalDate();
          System.out.println(username + " has dined at " + restaurant + " on " + dateDined.toString() + "!");
          restaurants.put(restaurant, dateDined);
        }
        return restaurants;
      
      } catch (Exception e) {
        System.out.println("An SQL Error occured: " + e.getMessage());
        return restaurants;
      }
    }
    return restaurants;
  }

  public Map<String, LocalDate> getRestaurantsAUserHasDinedAtRecently(String username, LocalDate dateOfTestResult){
    Map<String, LocalDate> restaurants = new HashMap<>();
    System.out.println("Finding restaurants that " + username + " has dined at at most two weeks before " + dateOfTestResult + "...");
    if(userHasDined(username)){
      try (Connection connection = dataSource.getConnection()){
          Statement stmt = connection.createStatement();
          Date sqlDateOfDiningMinusTwoWeeks = Date.valueOf(dateOfTestResult.plusDays(-14));
          String sql = "SELECT Restaurant, Date FROM Dinings WHERE Username = '" + username + "' AND Date >= '" + sqlDateOfDiningMinusTwoWeeks + "'";
          ResultSet queriedRestaurants = stmt.executeQuery(sql);
          //System.out.println("Found the following restaurants: " + queriedRestaurants.toString());
          while(queriedRestaurants.next()){
            String restaurant = queriedRestaurants.getString("restaurant");
            LocalDate dateDined = queriedRestaurants.getDate("date").toLocalDate();
            System.out.println(username + " recently dined at " + restaurant + " on " + dateDined.toString() + "!");
            restaurants.put(restaurant, dateDined);
          }
          return restaurants;
        
      } catch (Exception e) {
        System.out.println("An SQL Error occured: " + e.getMessage());
        return restaurants;
      }
    }
    return restaurants;
  }

  public void updateRestaurantExposures(Map<String, LocalDate> dinings){
    try(Connection connection = dataSource.getConnection()){
      Statement stmt = connection.createStatement();
      String sql = "";
      for (Map.Entry<String, LocalDate> entry : dinings.entrySet()) {
        String restaurantName = entry.getKey();
        Date sqlDate = Date.valueOf(entry.getValue());
        sql = "UPDATE Restaurants SET exposure = '" + sqlDate + "' WHERE name = '" + restaurantName + "'";
        stmt.executeUpdate(sql);
      } 
    } catch (Exception e){
      System.out.println("An SQL Error occured: " + e.getMessage());
    }
  }

  public Map<String, LocalDate> getDinersWhoRecentlyDinedAtARestaurant(String restaurantName){
    Date twoWeeksAgo = Date.valueOf(LocalDate.now().plusDays(-14));
    Map<String, LocalDate> diners = new HashMap<String, LocalDate>();
    try(Connection connection = dataSource.getConnection()){
      Statement stmt = connection.createStatement();
      String sql = "SELECT Username, date FROM Dinings WHERE Restaurant = '" + restaurantName + "' AND Date >= '" + twoWeeksAgo + "'";
      ResultSet queriedDiners = stmt.executeQuery(sql);
      while(queriedDiners.next()){
        String username = queriedDiners.getString("username");
        LocalDate dateDined = queriedDiners.getDate("date").toLocalDate();
        System.out.println(username + " dined at " + restaurantName + " within the past two weeks, on " + dateDined);
        diners.put(username, dateDined);
      }
      return diners;
    } catch (Exception e){
      System.out.println("An SQL Error occured: " + e.getMessage());
      return diners;
    }
  }

  public void updateDinerExposuresForRestaurant(Map<String, LocalDate> dinings){
    try(Connection connection = dataSource.getConnection()){
      Statement stmt = connection.createStatement();
      String sql = "";
      for (Map.Entry<String, LocalDate> entry : dinings.entrySet()) {
        String username = entry.getKey();
        Date sqlDate = Date.valueOf(entry.getValue());
        sql = "UPDATE Diners SET exposed = true WHERE username = '" + username + "'";
        stmt.executeUpdate(sql);
        sql = "UPDATE Diners SET exposure = '" + sqlDate + "' WHERE username = '" + username + "'";
        stmt.executeUpdate(sql);
      } 
    } catch (Exception e){
      System.out.println("An SQL Error occured: " + e.getMessage());
    }
  }

  public void updateDinerExposures(Map<String, LocalDate> restaurants) {
    for (String restaurant : restaurants.keySet()) {
      Map<String, LocalDate> dinings = getDinersWhoRecentlyDinedAtARestaurant(restaurant);
      updateDinerExposuresForRestaurant(dinings);
    }
  }

  @PostMapping(
    path = "/dinerlogin",
    consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE}
    )
  public String loginToDinerAccount(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model, Diner diner) throws Exception {
    logout(request, response, model);
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      Diner d = new Diner();
      d.setUsername(diner.getUsername());
      d.setName(diner.getName());
      d.setEmail(diner.getEmail());
      d.setPassword(diner.getPassword());
      System.out.println(d);
      stmt.executeUpdate(SQL_DINERS_INITIALIZER);
      String sql = "SELECT * FROM Diners WHERE username = '" + diner.getUsername() + "'";
      System.out.println(sql);
      ResultSet queriedUser = stmt.executeQuery(sql);

      if(!queriedUser.isBeforeFirst()){
        model.put("message", USERNAME_DOES_NOT_EXIST);
        return "dinerlogin";
      }
      queriedUser.next();
      
      String queriedPassword = queriedUser.getString("password");
      if(!(diner.getPassword().equals(queriedPassword))){
        model.put("message", PASSWORD_DOES_NOT_MATCH);
        return "dinerlogin";
      }
      
      Diner dinerToLogin = buildKnownDinerFromDatabase(diner.getUsername());
      login(response, dinerToLogin);
      System.out.println("Adding the diner " + dinerToLogin + " to the model...");
      model.put("diner", d);
      System.out.println("Redirecting...");
      return "redirect:/diningreportpage";

    }  catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @PostMapping("/createdineraccountpage")
  public String prepareNewDinerAccountForm(Map<String, Object> model){
    Diner diner = new Diner();
    model.put("diner", diner);
    return "createdineraccount";
  }

  @GetMapping("/createdineraccount")
  public String prepareToCreateNewDiner(Map<String, Object> model){
    Diner diner = new Diner();
    model.put("diner", diner);
    return "createdineraccount";
  }

  @PostMapping("/createdineraccount")
  public String createDinerAccount(Map<String, Object> model, Diner diner){
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();

      //Check if the requested username exists in the Diners database
      stmt.executeUpdate(SQL_DINERS_INITIALIZER);
      String sql = "SELECT * FROM Diners WHERE username = '" + diner.getUsername() + "'";
      ResultSet dinersWithMatchingName = stmt.executeQuery(sql);
      if(dinersWithMatchingName.isBeforeFirst()){
        model.put("message", USERNAME_ALREADY_IN_USE);
        return "createdineraccount";
      }
      dinersWithMatchingName.next();

       //Check if the requested email exists in the database
       sql = "SELECT * FROM Diners WHERE email = '" + diner.getEmail() + "'";
       ResultSet dinersWithMatchingEmail = stmt.executeQuery(sql);
       if(dinersWithMatchingEmail.isBeforeFirst()){
         model.put("message", EMAIL_ALREADY_IN_USE);
         return "createdineraccount";
       }
       dinersWithMatchingEmail.next();

      //Check if the requested username exists in the Users database
      stmt.executeUpdate(SQL_USERS_INITIALIZER);
      sql = "SELECT * FROM Users WHERE username = '" + diner.getUsername() + "'";
      ResultSet usersWithMatchingName = stmt.executeQuery(sql);
      if(usersWithMatchingName.isBeforeFirst()){
        model.put("message", USERNAME_ALREADY_IN_USE);
        return "createdineraccount";
      }
      usersWithMatchingName.next();

      //Check if the request email exists in the Email database
      sql = "SELECT * FROM Users WHERE email = '" + diner.getEmail() + "'";
      ResultSet usersWithMatchingEmail = stmt.executeQuery(sql);
      if(usersWithMatchingEmail.isBeforeFirst()){
        model.put("message", EMAIL_ALREADY_IN_USE);
        return "createdineraccount";
      }
      usersWithMatchingEmail.next();

     
      sql = "INSERT INTO Diners (username,name,email,password,exposed,testresult) VALUES ('" + diner.getUsername() + "', '" + diner.getName() + "', '" + diner.getEmail() + "', '" + diner.getPassword() + "', " + diner.wasExposed() + ", false)";
      System.out.println(sql);
      stmt.executeUpdate(sql);
      sql = "INSERT INTO Users (username,name,email,password,restaurant) VALUES ('" + diner.getUsername() + "', '" + diner.getName() + "', '" + diner.getEmail() + "', '" + diner.getPassword() + "', " + diner.isRestaurant() + ")";
      System.out.println(sql);
      stmt.executeUpdate(sql);
      //model.put("diner", diner);
      return "redirect:/dinerlogin";
    }  catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }


  @GetMapping("/diningreport")
  public String diningReport(HttpServletRequest request, Map<String, Object> model){
    System.out.println("Getting dining report page...");
    Dining dining = new Dining();
    model.put("dining", dining);
    if(getActionFromDinerLoginStatus(request, "diningreport", "dinerlogin").equalsIgnoreCase("diningreport")){
      String username = loggedInUser.getUsername();
      if(userHasDined(username)){
        System.out.println("Found at least one dining for " + username + ".");
        if(dinerHasBeenExposed(username)){
          System.out.println("Diner was exposed.");
          model.put("message", generateCOVIDAlertMessage(username));
        } else {
          System.out.println("Diner was not exposed.");
          model.put("message", YOU_HAVE_NOT_BEEN_EXPOSED);
        }
      }
    }
    return getActionFromDinerLoginStatus(request, "diningreport", "dinerlogin");
  }

  @GetMapping("/diningreportpage")
  public String diningReportPageGet(HttpServletRequest request, Map <String, Object> model){
    System.out.println("Getting dining report page...");
    Dining dining = new Dining();
    model.put("dining", dining);
    if(getActionFromDinerLoginStatus(request, "diningreport", "dinerlogin").equalsIgnoreCase("diningreport")){
      String username = loggedInUser.getUsername();
      if(userHasDined(username)){
        System.out.println("Found at least one dining for " + username + ".");
        if(dinerHasBeenExposed(username)){
          System.out.println("Diner was exposed.");
          model.put("message", generateCOVIDAlertMessage(username));
        } else {
          System.out.println("Diner was not exposed.");
          model.put("message", YOU_HAVE_NOT_BEEN_EXPOSED);
        }
      }
    }
    return getActionFromDinerLoginStatus(request, "diningreport", "dinerlogin");
  }

  @PostMapping("/diningreportpage")
  public String diningReportPage(HttpServletRequest request, Map <String, Object> model){
    System.out.println("Getting dining report page...");
    Dining dining = new Dining();
    model.put("dining", dining);
    if(getActionFromDinerLoginStatus(request, "diningreport", "dinerlogin").equalsIgnoreCase("diningreport")){
      String username = loggedInUser.getUsername();
      if(userHasDined(username)){
        System.out.println("Found at least one dining for " + username + ".");
        if(dinerHasBeenExposed(username)){
          System.out.println("Diner was exposed.");
          model.put("message", generateCOVIDAlertMessage(username));
        } else {
          System.out.println("Diner was not exposed.");
          model.put("message", YOU_HAVE_NOT_BEEN_EXPOSED);
        }
      }
    }
    return getActionFromDinerLoginStatus(request, "diningreport", "dinerlogin");
  }

  @PostMapping("/diningreport")
  public String reportDining(HttpServletRequest request, Map<String, Object> model, Dining dining){
    Date date = Date.valueOf(dining.getDate());
    Time time = Time.valueOf(dining.getTime());
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate(SQL_DININGS_INITITALIZER);
      String sql = "SELECT * FROM Restaurants WHERE name = '" + dining.getRestaurant() + "'";
      ResultSet queriedRestaurant = stmt.executeQuery(sql);
      if(!queriedRestaurant.isBeforeFirst()){
        System.out.println("Attempting to dine at a restaurant that is not registered with DineAlert!");
        model.put("message", RESTAURANT_NOT_FOUND);
        return getActionFromDinerLoginStatus(request, "redirect:/diningreport", "redirect:/index");
      }
      queriedRestaurant.next();


      sql = "INSERT INTO Dinings (restaurant, username, name,email,time,date,exposed) VALUES ('" + dining.getRestaurant() + "', '" + loggedInUser.getUsername() + "', '" + loggedInUser.getName() + "', '" + loggedInUser.getEmail() + "', '" + time + "', '" + date + "', '" + ((Diner) loggedInUser).wasExposed() +"')";
      System.out.println(sql);
      stmt.executeUpdate(sql);
      syncDiningExposures();
      if(dinerHasBeenExposed(loggedInUser.getUsername()) || dinerHasARecentPositiveTestResult(loggedInUser.getUsername())){
        System.out.println("The user currently reporting a dining, " + loggedInUser.getUsername() + ", has been exposed.");
        updateExposuresForDiningsAtRestaurant(dining.getRestaurant(), dining.getDate()); 
      }
      updateDinerExposuresFromDinings();
      model.put("dining", dining);

      return "redirect:/thankyou";
    }  catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }


  @PostMapping("/adminloginpage")
  public String adminLoginPage(Map<String, Object> model){
    Restaurant restaurant = new Restaurant();
    model.put("restaurant", restaurant);
    return "adminlogin";
  }

  @GetMapping("/adminlogin")
  public String adminLogin(Map<String, Object> model){
    Restaurant restaurant = new Restaurant();
    model.put("restaurant", restaurant);
    return "adminlogin";
  }

  @PostMapping(
    path = "/adminlogin",
    consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE}
    )
  public String loginToAdminAccount(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model, Restaurant restaurant) throws Exception {
    logout(request, response, model);
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      Restaurant r = new Restaurant();
      r.setName(restaurant.getName());
      r.setUsername(restaurant.getUsername());
      r.setPassword(restaurant.getPassword());
      r.setPremiumStatus(restaurant.getPremiumStatus());
      System.out.println(r);
      stmt.executeUpdate(SQL_RESTAURANTS_INITIALIZER);
      String sql = "SELECT * FROM Restaurants WHERE username = '" + restaurant.getUsername() + "'";
      System.out.println(sql);
      ResultSet queriedUser = stmt.executeQuery(sql);

      if(!queriedUser.isBeforeFirst()){
        model.put("message", USERNAME_DOES_NOT_EXIST);
        return "adminlogin";
      }
      queriedUser.next();
      
      String queriedPassword = queriedUser.getString("password");
      System.out.println(queriedPassword);
      System.out.println(restaurant.getPassword());
      if(!(restaurant.getPassword().equals(queriedPassword))){
        model.put("message", PASSWORD_DOES_NOT_MATCH);
        return "adminlogin";
      }

      Restaurant restaurantToLogin = buildKnownRestaurantFromDatabase(restaurant.getUsername());
      login(response, restaurantToLogin);
      // ResultSet restaurants = stmt.executeQuery("SELECT * FROM Restaurants WHERE username = '"+ restaurant.getUsername() +"'");
      // String restaurantName = "";
      // while(restaurants.next()) {
      //   restaurantName = restaurants.getString("name");
      // }
      // stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Dinings (id serial, restaurant varchar(30), name varchar(30), email varchar(30), time time, date date, exposed boolean)");
      // stmt.executeUpdate("ALTER TABLE Dinings ADD COLUMN IF NOT EXISTS exposed boolean");
      // ResultSet diner = stmt.executeQuery("SELECT * FROM Dinings WHERE restaurant = '" +restaurantName+ "'");
      // List<List<String>> recs = new ArrayList<>();
      // while(diner.next()){
      //   String id = diner.getString("id");
      //   String name = diner.getString("name");
      //   String email = diner.getString("email");
      //   String time = diner.getString("time");
      //   String date = diner.getString("date");
      //   String exposed = diner.getString("exposed");
      //   ArrayList<String> rec = new ArrayList<>();
      //   rec.add(id);
      //   rec.add(name);
      //   rec.add(email);
      //   rec.add(time);
      //   rec.add(date);
      //   rec.add(exposed);
      //   recs.add(rec);
      // }
      // model.put("recs", recs);
      System.out.println("Redirecting to admin home page...");
      return "redirect:/adminhomepage";
    }  catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @GetMapping("/home/sendEmail")
  public String sendEmail(){
    System.out.println("testing email function!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    //SendEmail send = new SendEmail();
    //send.set_receiverEmail("zta23@sfu.ca");
    //send.sendalertEmail();
    return "home";
  }

  @GetMapping("/home/deleted/{id}")
  public String getSpecificDiner(Map<String, Object> model, @PathVariable String id){
    System.out.println(id);
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      ResultSet restaurant_name = stmt.executeQuery("SELECT * FROM Dinings WHERE id=" + id);
      Statement st = connection.createStatement();
      st.executeUpdate("DELETE FROM Dinings WHERE id='"+id+"'");
      String restaurantName = "";
      while(restaurant_name.next()){
        restaurantName = restaurant_name.getString("restaurant");
      }
      ResultSet diner = stmt.executeQuery("SELECT * FROM Dinings WHERE restaurant = '" +restaurantName+ "'");
      List<List<String>> recs = new ArrayList<>();
      while(diner.next()){
        String idNew = diner.getString("id");
        String name = diner.getString("name");
        String email = diner.getString("email");
        String time = diner.getString("time");
        String date = diner.getString("date");
        String exposed = diner.getString("exposed");
        ArrayList<String> rec = new ArrayList<>();
        rec.add(idNew);
        rec.add(name);
        rec.add(email);
        rec.add(time);
        rec.add(date);
        rec.add(exposed);
        recs.add(rec);
      }
      model.put("recs", recs);
      return "home";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
}


  @PostMapping("/createadminaccountpage")
  public String prepareNewAdminAccountForm(Map<String, Object> model){
    Restaurant restaurant = new Restaurant();
    model.put("restaurant", restaurant);
    return "createadminaccount";
  }

  @GetMapping("/createadminaccount")
  public String prepareToSubmitNewRestaurant(Map<String, Object> model){
    Restaurant restaurant = new Restaurant();
    model.put("restaurant", restaurant);
    return "createadminaccount";
  }

  @PostMapping("/createadminaccount")
  public String createAdminAccount(Map<String, Object> model, Restaurant restaurant){
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate(SQL_RESTAURANTS_INITIALIZER);
      
      //Check if the requested username exists in the database
      String sql = "SELECT * FROM Restaurants WHERE username = '" + restaurant.getUsername() + "'";
      ResultSet restaurantsWithMatchingName = stmt.executeQuery(sql);
      if(restaurantsWithMatchingName.isBeforeFirst()){
        model.put("message", USERNAME_ALREADY_IN_USE);
        return "createadminaccount";
      }
      restaurantsWithMatchingName.next();

      //Check if the requested username exists in the Users database
      stmt.executeUpdate(SQL_USERS_INITIALIZER);
      sql = "SELECT * FROM Users WHERE username = '" + restaurant.getUsername() + "'";
      ResultSet usersWithMatchingName = stmt.executeQuery(sql);
      if(usersWithMatchingName.isBeforeFirst()){
        model.put("message", USERNAME_ALREADY_IN_USE);
        return "createadminaccount";
      }
      usersWithMatchingName.next();

      sql = "INSERT INTO Restaurants (name,username,password,premium,latitude,longitude) VALUES ('" + restaurant.getName() + "', '" + restaurant.getUsername() + "', '" + restaurant.getPassword() + "', " + restaurant.getPremiumStatus() + ", " + restaurant.getLatitude() + ", " + restaurant.getLongitude() + ")";
      System.out.println(sql);
      stmt.executeUpdate(sql);

      sql = "INSERT INTO Users (username,name,email,password,restaurant) VALUES ('" + restaurant.getUsername() + "', '" + restaurant.getName() + "', '" + restaurant.getEmail() + "', '" + restaurant.getPassword() + "', " + restaurant.isRestaurant() + ")";
      System.out.println(sql);
      stmt.executeUpdate(sql);


      // stmt.executeUpdate(SQL_DININGS_INITITALIZER);
      // stmt.executeUpdate("ALTER TABLE Dinings ADD COLUMN IF NOT EXISTS exposed boolean");
      // ResultSet diner = stmt.executeQuery("SELECT * FROM Dinings WHERE restaurant = '"+ restaurant.getName() +"'");
      //   List<List<String>> recs = new ArrayList<>();
      //   while(diner.next()){
      //     String id = diner.getString("id");
      //   String name = diner.getString("name");
      //   String email = diner.getString("email");
      //   String time = diner.getString("time");
      //   String date = diner.getString("date");
      //   ArrayList<String> rec = new ArrayList<>();
      //   rec.add(id);
      //   rec.add(name);
      //   rec.add(email);
      //   rec.add(time);
      //   rec.add(date);
      //   recs.add(rec);
      // }
      // model.put("recs", recs);
      return "redirect:/adminlogin";
    }  catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @RequestMapping("/adminhomepage")
  public String getAdminHomepage(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model){
    System.out.println("Getting dining admim home page...");
    Dining dining = new Dining();
    model.put("dining", dining);
    return getActionFromRestaurantLoginStatus(request, "redirect:/adminhome", "adminlogin");
  }

  @RequestMapping("/adminhome")
  public String prepareAdminHomepage(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model){
    System.out.println("Preparing admin data table...");
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate(SQL_DININGS_INITITALIZER);
      //stmt.executeUpdate("ALTER TABLE Dinings ADD COLUMN IF NOT EXISTS exposed boolean");
      ResultSet diner = stmt.executeQuery("SELECT * FROM Dinings WHERE restaurant = '"+ loggedInUser.getName() +"'");
        List<List<String>> recs = new ArrayList<>();
        while(diner.next()){
        String id = diner.getString("id");
        String name = diner.getString("name");
        String email = diner.getString("email");
        String time = diner.getString("time");
        String date = diner.getString("date");
        String exposed = diner.getString("exposed");
        ArrayList<String> rec = new ArrayList<>();
        rec.add(id);
        rec.add(name);
        rec.add(email);
        rec.add(time);
        rec.add(date);
        rec.add(exposed);
        recs.add(rec);
      }
      model.put("recs", recs);
      return "home";
    }  catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }
  @GetMapping("/map")
  public String googleMap(Map<String, Object> model){
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      Date twoWeeksAgo = Date.valueOf(LocalDate.now().plusDays(-14));
      String sql = "SELECT latitude, longitude, name FROM Restaurants WHERE exposure >= '" + twoWeeksAgo + "'";
      String green = "SELECT latitude, longitude, name FROM Restaurants WHERE exposure IS NULL OR exposure < '" + twoWeeksAgo + "'";
      ResultSet allRestaurants = stmt.executeQuery(sql);
      Statement stmt2 = connection.createStatement();
      ResultSet Green = stmt2.executeQuery(green);
      List<List<Double>> recs = new ArrayList<>();
      List<List<Double>> greens = new ArrayList<>();
      List<String> redNames = new ArrayList<>();
      List<String> greenNames = new ArrayList<>();
      while(allRestaurants.next()){
        Double latitude = allRestaurants.getDouble("latitude");
        Double longitude = allRestaurants.getDouble("longitude");
        String name = allRestaurants.getString("name");
        ArrayList<Double> rec = new ArrayList<>();
        rec.add(latitude);
        rec.add(longitude);
        recs.add(rec);
        redNames.add(name);
      }
      while (Green.next()){
        Double latitude = Green.getDouble("latitude");
        Double longitude = Green.getDouble("longitude");
        String name = Green.getString("name");
        ArrayList<Double> g = new ArrayList<>();
        g.add(latitude);
        g.add(longitude);
        greens.add(g);
        greenNames.add(name);
      }
      model.put("recs", recs);
      model.put("greens", greens);
      model.put("redNames", redNames);
      model.put("greenNames", greenNames);
      return "map";
    }
    catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }

  }
  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }

  // Drops tables specified by a list of names.

  public void destroyDatabaseTables(List<String> tables){
    System.out.println("Dropping " + tables.size() + " database tables...");
    for(String table:tables){
      System.out.println("Dropping " + table);
      try (Connection connection = dataSource.getConnection()) {
        Statement stmt = connection.createStatement();
        String sql = "DROP TABLE " + table + ";";
        stmt.executeUpdate(sql);
      } catch (Exception e){
        System.out.println("An SQL error occurred attempting to drop " + table);
      }
    }
  }

  // Empties tables specified by a list of names without dropping them, using TRUNCATE.
    
  public void resetDatabaseTables(List<String> tables){
    System.out.println("Emptying " + tables.size() + " database tables...");
    for(String table:tables){
      System.out.println("Emptying " + table);
      try (Connection connection = dataSource.getConnection()) {
        Statement stmt = connection.createStatement();
        String sql = "TRUNCATE TABLE " + table + ";";
        stmt.executeUpdate(sql);
      } catch (Exception e){
        System.out.println("An SQL error occurred attempting to empty " + table);
      }
    }
  }

  private void deleteCertainRestaurant(String name){
    String sql = "";
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      sql = "DELETE FROM Restaurants WHERE name = '" + name + "'";
      stmt.executeUpdate(sql);
      
    } catch (Exception e){
      System.out.println("An SQL error occurred attempting to delete a restaurant named " + name + ": " + e.getMessage());
    }
  }

  // Creates the default tables for the app. Run this any time.

  private void createDefaultDineAlertTables(){
    String sql = "";
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      sql = SQL_USERS_INITIALIZER;
      System.out.println(sql);
      stmt.executeUpdate(sql);
      sql = SQL_DINERS_INITIALIZER;
      System.out.println(sql);
      stmt.executeUpdate(sql);
      sql = SQL_RESTAURANTS_INITIALIZER;
      System.out.println(sql);
      stmt.executeUpdate(sql);
      sql = SQL_DININGS_INITITALIZER;
      System.out.println(sql);
      stmt.executeUpdate(sql);
    } catch (Exception e){
      System.out.println("An SQL error occurred attempting to create the default tables!");
    }
  }

  // Login to the app. Set up the cookie and loggedInUser.

  private void login(HttpServletResponse response, User user) {
    System.out.println("Setting user cookie...");
    setCookie(response, "username", user.getUsername());
    System.out.println("Cookie set.");
    System.out.println("Current logged in users: " + loggedInUsers.toString());
    System.out.println("Updating logged in users...");
    loggedInUsers.put(user.getUsername(), user);
    loggedInUser = user;
    System.out.println("Current logged in users: " + loggedInUsers.toString());
  }

    // Determines whether or not to redirect a user to an Diner access only page or to the Diner login screen based on the status of loggedInUser.

  public String getActionFromDinerLoginStatus(HttpServletRequest request, String pageWithAccess, String pageWithoutAccess){
    System.out.println("Determining redirect based on diner login status...");
    if(savedUsernameExistsInCookies(request, "username")){
      String cookie = getCookieString(request, "username");
      System.out.println("A username cookie was found: username = " + cookie);
      if(loggedInUsers.containsKey(cookie) && loggedInUser.isDiner()){
        System.out.println(loggedInUsers.get(cookie).getUsername());
        loggedInUser = new Diner((Diner)loggedInUsers.get(cookie));
        System.out.println("The logged in Diner is: " + loggedInUser.getUsername());
        return pageWithAccess;
      } else {
        return "redirect:/" + pageWithoutAccess;
      }
    } else {
      return "redirect:/" + pageWithoutAccess;
    }
  }
    
  // Determines whether or not to redirect a user to a Restaurant access only page or to the Restaurant login screen based on the status of loggedInUser.

  public String getActionFromRestaurantLoginStatus(HttpServletRequest request, String pageWithAccess, String pageWithoutAccess){
    System.out.println("Determining redirect based on restaurant login status...");
    if(savedUsernameExistsInCookies(request, "username")){
      String cookie = getCookieString(request, "username");
      System.out.println(cookie);
      if(loggedInUsers.containsKey(cookie) && loggedInUser.isRestaurant()){
        loggedInUser = loggedInUsers.get(cookie);
        System.out.println("The logged in Restaurant is: " + loggedInUser.getUsername());
        return pageWithAccess;
      } else {
        return "redirect:/" + pageWithoutAccess;
      }
    } else {
      return "redirect:/" + pageWithoutAccess;
    }
  }

  // Deletes specifically the username cookied. Redundant.

  public void deleteUsernameCookie(HttpServletResponse response){
    System.out.println("Deleting username cookie...");
    deleteCookie(response, "username");
  }

  // Sets the cookie of a given name to a given value.

  public void setCookie(HttpServletResponse response, String cookieName, String cookieValue){
    System.out.println("Setting the cookie " + cookieName + " to " + cookieValue + "...");
    Cookie cookie = new Cookie(cookieName, cookieValue);
    response.addCookie(cookie);
  }

  // Deprecated.

  // public String getUsernameCookie(@CookieValue(value = "username", defaultValue = "No username cookie!") String cookieVal){
  //   return cookieVal;
  // }

  // Retrieves the stored value from the 'username' cookie.

  public String getCookieString(HttpServletRequest request, String cookieName){
    System.out.println("Getting value of cookie " + cookieName + "...");
    Cookie cookie = WebUtils.getCookie(request, cookieName);
    System.out.println("The value of " + cookieName + " was " + cookie.getValue());
    return cookie.getValue();
  }

  // Checks if there is a cookie called 'username'.

  public boolean savedUsernameExistsInCookies(HttpServletRequest request, String cookieName){
    System.out.println("Checking if " + cookieName + " exists in the cookies...");
    Cookie cookie = WebUtils.getCookie(request, cookieName);
    if (Objects.nonNull(cookie)){
      System.out.println(cookieName + " exists in the cookies.");
      return true;
    } 
    else return false;
  }

  // Deletes a cookie with a given name.

  public void deleteCookie(HttpServletResponse response, String cookieName){
    System.out.println("Deleting cookie " + cookieName + "...");
    Cookie cookie = new Cookie(cookieName, null);
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }

  // Checks if the username cookie in the browser exists and matches the username of the loggedInUser.

  public boolean cookieMatchesUser(HttpServletRequest request){
    if(Objects.nonNull(loggedInUser)){
      if(Objects.nonNull(loggedInUser.getName())){
        if(loggedInUser.getName().equalsIgnoreCase(getCookieString(request, "username"))){
          System.out.println("The stored username cookie matches the username of the logged in user.");
          return true;
        }
      }
    }
    return false;
    }


  // Checks if the name of the loggedInUser already matches the cookie. If it doesn't;
  // Checks if the cookie exists at all;
  // Invokes update function.

  public void refreshLoggedInUserFromCookies(HttpServletRequest request, HttpServletResponse response) throws SQLException{
    System.out.println("Checking if user needs to be updated...");
    if(!savedUsernameExistsInCookies(request, "username")){
      setCookie(response, "username", "");
    } else {
      if(Objects.isNull(getCookieString(request, "username"))){
        setCookie(response, "username", "");
      } else {
        if(!cookieMatchesUser(request)){
          if(savedUsernameExistsInCookies(request, "username")){
            System.out.println("Refreshing logged in user...");
            updateLoggedInUserFromCookies(request);
          }
        }
      }
    }
  }
    

  // Creates a new logginInUser based on the discovered cookie, assuming it can find matching database entries.

  public void updateLoggedInUserFromCookies(HttpServletRequest request) throws SQLException{
    System.out.println("Pulling logged in user from cookies...");
    if(savedUsernameExistsInCookies(request, "username")){
      String username = getCookieString(request, "username");
      System.out.println("A username cookie was found: username = " + username);
      //User userFoundInCookies = new User();
      if(userExistsInDatabase(username)){
        if(dinerExistsInDatabase(username)){
          Diner dinerFoundInCookies = buildKnownDinerFromDatabase(username);
          //userFoundInCookies = buildKnownDinerFromDatabase(username);
          loggedInUser = new Diner(dinerFoundInCookies);
          loggedInUsers.put(username, dinerFoundInCookies);
          return;
        }
        if(restaurantExistsInDatabase(username)){
          Restaurant restaurantFoundInCookies = buildKnownRestaurantFromDatabase(username);
          loggedInUser = new Restaurant(restaurantFoundInCookies);
          loggedInUsers.put(username, restaurantFoundInCookies);
          return;
        }
      }
  }
  }

     // ======================= DEPRECATED ==============================

  // public ResultSet getUserByUsername(String username) {
  //   try (Connection connection = dataSource.getConnection()) {
  //     Statement stmt = connection.createStatement();
  //     String sql = "SELECT * FROM Users WHERE username = '" + username + "'";
  //     return stmt.executeQuery(sql);
  //   }  catch (Exception e) {

  //   }
  // }

  // Checks the Users database for an entry matching the given username.

  public boolean userExistsInDatabase(String username) throws SQLException {
    System.out.println("Checking for a user with username " + username + " in the Users database...");
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "SELECT * FROM Users WHERE username = '" + username + "'";
      ResultSet user =  stmt.executeQuery(sql);
      if(!user.isBeforeFirst()){
        System.out.println(username + " could not be found in Users.");
        return false;
      }
      user.next();
      System.out.println(username + " found in Users.");
      return true;

    }  catch (Exception e) {
      System.out.println("There was an SQL error!");
      return false;
    }
    
  }

  // Constructs a User object based on database query results.

  public User buildKnownUserFromDatabase(String username) throws SQLException {
    System.out.println("Creating a User from the database entry for the User with username " + username);
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "SELECT * FROM Users WHERE username = '" + username + "'";
      ResultSet user = stmt.executeQuery(sql);
      User foundUser = new User();
      foundUser.setUsername(user.getString("username"));
      foundUser.setName(user.getString("name"));
      foundUser.setEmail(user.getString("email"));
      foundUser.setPassword(user.getString("password"));
      return foundUser;
    }  catch (Exception e) {
      System.out.println("An SQL error occurred attempting to build a user.");
      return new User();
    }
}


  // ======================= DEPRECATED ==============================

  // public ResultSet getDinerByUsername(String dinerUsername) {
  //   try (Connection connection = dataSource.getConnection()) {
  //     Statement stmt = connection.createStatement();
  //     String sql = "SELECT * FROM Diners WHERE username = '" + dinerUsername + "'";
  //     ResultSet r =  stmt.executeQuery(sql);
  //     return r;
  //   }  catch (Exception e) {

  //   }
  // }

    // Checks the Diners database for an entry matching the given username.

  public boolean dinerExistsInDatabase(String dinerUsername) throws SQLException {
    System.out.println("Checking for a diner with diner username " + dinerUsername + " in the Diners database...");
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "SELECT * FROM Diners WHERE username = '" + dinerUsername + "'";
      ResultSet diner =  stmt.executeQuery(sql);
    if(!diner.isBeforeFirst()){
      System.out.println(dinerUsername + " could not be found in Diners.");
      return false;
    }
    diner.next();
    System.out.println(dinerUsername + " found in Diners.");
    return true;
    }  catch (Exception e) {
      return false;
    }
  }

  // Constructs a Diner object based on query results.

  public Diner buildKnownDinerFromDatabase(String dinerUsername) throws SQLException {
    System.out.println("Creating a Diner from the database entry for the Diner with username " + dinerUsername);
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "SELECT * FROM Diners WHERE username = '" + dinerUsername + "'";
      System.out.println(sql);
      ResultSet diner = stmt.executeQuery(sql);
      diner.next();
      System.out.println("Database queried...");
      Diner foundDiner = new Diner();
      String username = diner.getString("username");
      System.out.println("The database returned a username of " + username);
      String name = diner.getString("name");
      System.out.println("The database returned a name of " + name);
      String password = diner.getString("password");
      System.out.println("The database returned a password of " + password);
      String email = diner.getString("email");
      System.out.println("The database returned an email of " + email);
      boolean exposed = diner.getBoolean("exposed");
      System.out.println("The database returned an exposed value of " + exposed);
      Date exposure = diner.getDate("exposure");
      System.out.println("The database returned an exposure date of " + exposure);
      System.out.println("Got username and name field...");
      
      System.out.println("The database returned a name of " + name);
      foundDiner.setUsername(username);
      foundDiner.setName(name);
      foundDiner.setEmail(email);
      foundDiner.setPassword(password);
      foundDiner.setExposed(exposed);
      if(Objects.nonNull(exposure)){
        LocalDate localexposure = exposure.toLocalDate();
      foundDiner.setExposureDate(localexposure);
      }
      return foundDiner;
    }  catch (Exception e) {
      System.out.println("An SQL error occurred attempting to build a Diner.");
      return new Diner();
    }
  }

    // Checks the Restaurants database for an entry matching the given username.

  public boolean restaurantExistsInDatabase(String username) {
    System.out.println("Checking for a Restaurant with username " + username + " in the Restaurants database...");
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "SELECT * FROM Restaurants WHERE username = '" + username + "'";
      ResultSet restaurant =  stmt.executeQuery(sql);
      if(!restaurant.isBeforeFirst()){
        System.out.println(username + " could not be found in Restaurants.");
        return false;
      }
      restaurant.next();
      System.out.println(username + " found in Restaurants.");
      return true;

    }  catch (Exception e) {
      return false;
    }
    
  }

    // Constructs a Restaurant object based on query results.

  public Restaurant buildKnownRestaurantFromDatabase(String username) {
    System.out.println("Creating a Restaurant from the database entry for the Restaurant with username " + username);
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "SELECT * FROM Restaurants WHERE username = '" + username + "'";
      ResultSet restaurant = stmt.executeQuery(sql);
      restaurant.next();
      Restaurant foundRestaurant = new Restaurant();
      foundRestaurant.setUsername(restaurant.getString("username"));
      foundRestaurant.setName(restaurant.getString("name"));
      foundRestaurant.setId(restaurant.getInt("id"));
      foundRestaurant.setPassword(restaurant.getString("password"));
      foundRestaurant.setLatitude(restaurant.getFloat("latitude"));
      foundRestaurant.setLongitude(restaurant.getFloat("longitude"));
      return foundRestaurant;
    }  catch (Exception e) {
      System.out.println("An SQL error occurred attempting to build a Restaurant.");
      return new Restaurant();
    }
}


}
