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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.WebUtils;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
// import java.time.*;
import java.sql.Time;
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

  public DataSource getDataSource(){
    return this.dataSource;
  }

  private static Map<String, User> loggedInUsers = new HashMap<String, User>();

  private static User loggedInUser = new User();

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  @RequestMapping("/")
  String index(HttpServletRequest request, Map<String, Object> model) throws SQLException {
    refreshLoggedInUserFromCookies(request);
    return "index";
  }

  @RequestMapping("index")
  String index1(HttpServletRequest request, Map<String, Object> model) throws SQLException {
    refreshLoggedInUserFromCookies(request);
    return "index";
  }

  @GetMapping("/home")
  public String getAdminHomePage(Map<String, Object> model, Restaurant restaurant){
    model.put("restaurant", restaurant);
    return "home";
  }

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
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Diners (id serial, username varchar(30), name varchar(16), email varchar(30), password varchar(30), exposed boolean, exposure date)");
      stmt.executeUpdate("ALTER TABLE Dinings ADD COLUMN IF NOT EXISTS exposed boolean");
      String sql = "SELECT * FROM Diners WHERE username = '" + diner.getUsername() + "'";
      System.out.println(sql);
      ResultSet queriedUser = stmt.executeQuery(sql);

      if(!queriedUser.isBeforeFirst()){
        String message = "Username not found in system.";
        model.put("message", message);
        return "reportlogin";
      }
      queriedUser.next();
      
      String queriedPassword = queriedUser.getString("password");
      System.out.println(queriedPassword);
      System.out.println(diner.getPassword());
      if(!(diner.getPassword().equals(queriedPassword))){
        String message = "Password does not match.";
        model.put("message", message);
        return "reportlogin";
      }

      String queriedEmail = queriedUser.getString("email");
      String sql2 = "SELECT * FROM Dinings WHERE email = '" + queriedEmail + "'";
      System.out.println(sql2);
      ResultSet isDined = stmt.executeQuery(sql2);
      if(!isDined.isBeforeFirst()){
        String message = "You haven't dined at a restaurant yet.";
        model.put("message", message);
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
    path = "/dinerlogin",
    consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE}
    )
  public String loginToDinerAccount(HttpServletResponse response, Map<String, Object> model, Diner diner) throws Exception {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      Diner d = new Diner();
      d.setUsername(diner.getUsername());
      d.setName(diner.getName());
      d.setEmail(diner.getEmail());
      d.setPassword(diner.getPassword());
      System.out.println(d);
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Diners (id serial, username varchar(30), name varchar(16), email varchar(30), password varchar(30), exposed boolean, exposure date)");
      String sql = "SELECT * FROM Diners WHERE username = '" + diner.getUsername() + "'";
      System.out.println(sql);
      ResultSet queriedUser = stmt.executeQuery(sql);

      if(!queriedUser.isBeforeFirst()){
        String message = "Username not found in system.";
        model.put("message", message);
        return "dinerlogin";
      }
      queriedUser.next();
      
      String queriedPassword = queriedUser.getString("password");
      //System.out.println(queriedPassword);
      //System.out.println(diner.getPassword());
      if(!(diner.getPassword().equals(queriedPassword))){
        String message = "Password does not match.";
        model.put("message", message);
        return "dinerlogin";
      }
      
      System.out.println("Setting user cookie...");
      setCookie(response, "username", diner.getUsername());
      System.out.println("Cookie set.");
      System.out.println("Current logged in users: " + loggedInUsers.toString());
      System.out.println("Updating logged in users...");
      loggedInUsers.put(d.getUsername(), diner);
      loggedInUser = diner;
      System.out.println("Current logged in users: " + loggedInUsers.toString());
      System.out.println("Adding the diner " + d + " to the model...");
      model.put("diner", d);
      System.out.println("Redirecting...");
      return "redirect:/diningreport";

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
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Diners (id serial, username varchar(30), name varchar(16), email varchar(30), password varchar(30), exposed boolean, exposure date)");
      
      //Check if the requested username exists in the database
      String sql = "SELECT * FROM Diners WHERE username = '" + diner.getUsername() + "'";
      ResultSet dinersWithMatchingName = stmt.executeQuery(sql);
      if(dinersWithMatchingName.isBeforeFirst()){
        String message = "Username already exists, please try another.";
        model.put("message", message);
        return "createdineraccount";
      }
      dinersWithMatchingName.next();

      //Check if the requested email exists in the database
      sql = "SELECT * FROM Diners WHERE email = '" + diner.getEmail() + "'";
      ResultSet dinersWithMatchingEmail = stmt.executeQuery(sql);
      if(dinersWithMatchingEmail.isBeforeFirst()){
        String message = "Email already registered, please try another.";
        model.put("message", message);
        return "createdineraccount";
      }
      dinersWithMatchingEmail.next();

      sql = "INSERT INTO Diners (username,name,email,password,exposed) VALUES ('" + diner.getUsername() + "', '" + diner.getName() + "', '" + diner.getEmail() + "', '" + diner.getPassword() + "', " + diner.wasExposed() + ")";
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
    return getActionFromDinerLoginStatus(request, "diningreport", "dinerlogin");
  }

  @PostMapping("/diningreportpage")
  public String diningReportPage(HttpServletRequest request, Map <String, Object> model){
    System.out.println("Getting dining report page...");
    Dining dining = new Dining();
    model.put("dining", dining);
    return getActionFromDinerLoginStatus(request, "diningreport", "dinerlogin");
  }

  @PostMapping("/diningreport")
  public String reportDining(Map<String, Object> model, Dining dining){
    Date date = Date.valueOf(dining.getDate());
    Time time = Time.valueOf(dining.getTime());
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Dinings (id serial, restaurant varchar(30), name varchar(30), email varchar(30), time time, date date, exposed boolean)");
      stmt.executeUpdate("ALTER TABLE Dinings ADD COLUMN IF NOT EXISTS exposed boolean");
      String sql = "INSERT INTO Dinings (restaurant,name,email,time,date,exposed) VALUES ('" + dining.getRestaurant() + "', '" + dining.getDinerName() + "', '" + dining.getDinerEmail() + "', '" + time + "', '" + date + "', '" + dining.getDinerExposed() +"')";
      System.out.println(sql);
      stmt.executeUpdate(sql);
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
  public String loginToAdminAccount(Map<String, Object> model, Restaurant restaurant) throws Exception {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      Restaurant r = new Restaurant();
      r.setName(restaurant.getName());
      r.setUsername(restaurant.getUsername());
      r.setPassword(restaurant.getPassword());
      r.setPremiumStatus(restaurant.getPremiumStatus());
      System.out.println(r);
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Restaurants (id serial, name varchar(30), username varchar(16), password varchar(30), premium boolean)");
      String sql = "SELECT * FROM Restaurants WHERE username = '" + restaurant.getUsername() + "'";
      System.out.println(sql);
      ResultSet queriedUser = stmt.executeQuery(sql);

      if(!queriedUser.isBeforeFirst()){
        String message = "Username not found in system.";
        model.put("message", message);
        return "adminlogin";
      }
      queriedUser.next();
      
      String queriedPassword = queriedUser.getString("password");
      System.out.println(queriedPassword);
      System.out.println(restaurant.getPassword());
      if(!(restaurant.getPassword().equals(queriedPassword))){
        String message = "Password does not match.";
        model.put("message", message);
        return "adminlogin";
      }
      ResultSet restaurants = stmt.executeQuery("SELECT * FROM Restaurants WHERE username = '"+restaurant.getUsername()+"'");
      String restaurantName = "";
      while(restaurants.next()) {
        restaurantName = restaurants.getString("name");
      }
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Dinings (id serial, restaurant varchar(30), name varchar(30), email varchar(30), time time, date date, exposed boolean)");
      stmt.executeUpdate("ALTER TABLE Dinings ADD COLUMN IF NOT EXISTS exposed boolean");
      ResultSet diner = stmt.executeQuery("SELECT * FROM Dinings WHERE restaurant = '" +restaurantName+ "'");
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
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Restaurants (id serial, name varchar(30), username varchar(16), password varchar(30), premium boolean)");
      
      //Check if the requested username exists in the database
      String sql = "SELECT * FROM Restaurants WHERE username = '" + restaurant.getUsername() + "'";
      ResultSet restaurantsWithMatchingName = stmt.executeQuery(sql);
      if(restaurantsWithMatchingName.isBeforeFirst()){
        String message = "Username already exists, please try another.";
        model.put("message", message);
        return "createadminaccount";
      }
      restaurantsWithMatchingName.next();

      sql = "INSERT INTO Restaurants (name,username,password,premium) VALUES ('" + restaurant.getName() + "', '" + restaurant.getUsername() + "', '" + restaurant.getPassword() + "', " + restaurant.getPremiumStatus() + ")";
      System.out.println(sql);
      stmt.executeUpdate(sql);
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Dinings (id serial, restaurant varchar(30), name varchar(30), email varchar(30), time time, date date, exposed boolean)");
      stmt.executeUpdate("ALTER TABLE Dinings ADD COLUMN IF NOT EXISTS exposed boolean");
      ResultSet diner = stmt.executeQuery("SELECT * FROM Dinings WHERE restaurant = '"+ restaurant.getName() +"'");
        List<List<String>> recs = new ArrayList<>();
        while(diner.next()){
          String id = diner.getString("id");
        String name = diner.getString("name");
        String email = diner.getString("email");
        String time = diner.getString("time");
        String date = diner.getString("date");
        ArrayList<String> rec = new ArrayList<>();
        rec.add(id);
        rec.add(name);
        rec.add(email);
        rec.add(time);
        rec.add(date);
        recs.add(rec);
      }
      model.put("recs", recs);
      return "home";
    }  catch (Exception e) {
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



  public String getActionFromDinerLoginStatus(HttpServletRequest request, String pageWithAccess, String pageWithoutAccess){
    if(savedUsernameExistsInCookies(request, "username")){
      String cookie = getCookieString(request, "username");
      System.out.println(cookie);
      if(loggedInUsers.containsKey(cookie) && loggedInUser.isDiner()){
        loggedInUser = loggedInUsers.get(cookie);
        System.out.println("The logged in user is: " + loggedInUser.getUsername());
        return pageWithAccess;
      } else {
        return "redirect:/" + pageWithoutAccess;
      }
    } else {
      return "redirect:/" + pageWithoutAccess;
    }
  }
    

  public String getActionFromRestaurantLoginStatus(HttpServletRequest request, String pageWithAccess, String pageWithoutAccess){
    if(savedUsernameExistsInCookies(request, "username")){
      String cookie = getCookieString(request, "username");
      System.out.println(cookie);
      if(loggedInUsers.containsKey(cookie) && loggedInUser.isRestaurant()){
        loggedInUser = loggedInUsers.get(cookie);
        System.out.println("The logged in user is: " + loggedInUser.getUsername());
        return pageWithAccess;
      } else {
        return "redirect:/" + pageWithoutAccess;
      }
    } else {
      return "redirect:/" + pageWithoutAccess;
    }
  }

  public void deleteUsernameCookie(HttpServletResponse response){
    deleteCookie(response, "username");
  }

  public void setCookie(HttpServletResponse response, String cookieName, String cookieValue){
    Cookie cookie = new Cookie(cookieName, cookieValue);
    response.addCookie(cookie);
  }

  public String getUsernameCookie(@CookieValue(value = "username", defaultValue = "No username cookie!") String cookieVal){
    return cookieVal;
  }

  public String getCookieString(HttpServletRequest request, String cookieName){
    Cookie cookie = WebUtils.getCookie(request, cookieName);
    return cookie.getValue();
  }

  public boolean savedUsernameExistsInCookies(HttpServletRequest request, String cookieName){
    Cookie cookie = WebUtils.getCookie(request, cookieName);
    if (Objects.nonNull(cookie)){
      return true;
    } 
    else return false;
  }

  public void deleteCookie(HttpServletResponse response, String cookieName){
    Cookie cookie = new Cookie(cookieName, null);
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }

  public void refreshLoggedInUserFromCookies(HttpServletRequest request) throws SQLException{
    if(savedUsernameExistsInCookies(request, "username")){
      String username = getCookieString(request, "username");
      User userFoundInCookies = new User();
      if(userExistsInDatabase(username)){
        if(dinerExistsInDatabase(username)){
          userFoundInCookies = buildKnownDinerFromDatabase(username);
          loggedInUser = userFoundInCookies;
          loggedInUsers.put(username, userFoundInCookies);
        }
        if(restaurantExistsInDatabase(username)){
          userFoundInCookies = buildKnownRestaurantFromDatabase(username);
          loggedInUser = userFoundInCookies;
          loggedInUsers.put(username, userFoundInCookies);
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

  public boolean userExistsInDatabase(String username) throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "SELECT * FROM Users WHERE username = '" + username + "'";
      ResultSet user =  stmt.executeQuery(sql);
      if(!user.isBeforeFirst()){
        return false;
      }
      user.next();
      return true;

    }  catch (Exception e) {
      return false;
    }
    
  }

  public User buildKnownUserFromDatabase(String username) throws SQLException {
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

  public boolean dinerExistsInDatabase(String dinerUsername) throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "SELECT * FROM Diners WHERE username = '" + dinerUsername + "'";
      ResultSet diner =  stmt.executeQuery(sql);
    if(!diner.isBeforeFirst()){
      return false;
    }
    diner.next();
    return true;
    }  catch (Exception e) {
      return false;
    }
  }

  public Diner buildKnownDinerFromDatabase(String dinerUsername) throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "SELECT * FROM Diners WHERE username = '" + dinerUsername + "'";
      ResultSet diner =  stmt.executeQuery(sql);
      Diner foundDiner = new Diner();
      foundDiner.setUsername(diner.getString("username"));
      foundDiner.setName(diner.getString("name"));
      foundDiner.setEmail(diner.getString("email"));
      foundDiner.setPassword(diner.getString("password"));
      foundDiner.setExposed(Boolean.parseBoolean(diner.getString("exposed")));
      foundDiner.setExposureDate((diner.getDate("exposure").toLocalDate()));
      return foundDiner;
    }  catch (Exception e) {
      return new Diner();
    }
  }

  public boolean restaurantExistsInDatabase(String username) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "SELECT * FROM Restaurants WHERE username = '" + username + "'";
      ResultSet restaurant =  stmt.executeQuery(sql);
      if(!restaurant.isBeforeFirst()){
        return false;
      }
      restaurant.next();
      return true;

    }  catch (Exception e) {
      return false;
    }
    
  }

  public Restaurant buildKnownRestaurantFromDatabase(String username) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "SELECT * FROM Restaurants WHERE username = '" + username + "'";
      ResultSet restaurant = stmt.executeQuery(sql);
      Restaurant foundRestaurant = new Restaurant();
      foundRestaurant.setUsername(restaurant.getString("username"));
      foundRestaurant.setName(restaurant.getString("name"));
      foundRestaurant.setId(restaurant.getInt("id"));
      foundRestaurant.setPassword(restaurant.getString("password"));
      return foundRestaurant;
    }  catch (Exception e) {
      return new Restaurant();
    }
}

}
