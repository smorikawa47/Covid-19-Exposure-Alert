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
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.*;
import java.sql.Time;
import java.sql.Date;

@Controller
@SpringBootApplication
public class Main {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  @RequestMapping("/")
  String index(Map<String, Object> model) {
    return "index";
  }

  @GetMapping("/home")
  public String getAdminHomePage(Map<String, Object> model, Restaurant restaurant){
    model.put("restaurant", restaurant);
    return "home";
  }

  @GetMapping("/thankyou")
  public String thankDinerForReport(Map<String, Object> model){
    return "thankyou";
  }

  @GetMapping("/dinerlogin")
  public String dinerLogin(Map<String, Object> model){
    Diner diner = new Diner();
    model.put("diner", diner);
    return "dinerlogin";
  }

  @PostMapping("/dinerloginpage")
  public String dinerLoginPage(Map<String, Object> model){
    Diner diner = new Diner();
    model.put("diner", diner);
    return "dinerlogin";
  }

  @PostMapping(
    path = "/dinerlogin",
    consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE}
    )
  public String loginToDinerAccount(Map<String, Object> model, Diner diner) throws Exception {
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
      System.out.println(queriedPassword);
      System.out.println(diner.getPassword());
      if(!(diner.getPassword().equals(queriedPassword))){
        String message = "Password does not match.";
        model.put("message", message);
        return "dinerlogin";
      }
      
      model.put("diner", d);
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
      model.put("diner", diner);
      return "redirect:/diningreport";
    }  catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }


  @GetMapping("/diningreport")
  public String diningReport(Map<String, Object> model){
    Dining dining = new Dining();
    model.put("dining", dining);
    return "diningreport";
  }

  // // 
  /*Do we need this ? Done by Tauseef Added this 05.07 */
  @PostMapping("/diningreportpage")
  public String diningReportPage(Map <String, Object> model){
    Dining dining = new Dining();
    model.put("dining", dining);
    return "diningreport"; //diningreport.html
  }

  @PostMapping("/diningreport")
  public String reportDining(Map<String, Object> model, Dining dining){
    Date date = Date.valueOf(dining.getDate());
    Time time = Time.valueOf(dining.getTime());
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Dinings (id serial, restaurant varchar(30), name varchar(30), email varchar(30), time time, date date)");
      String sql = "INSERT INTO Dinings (restaurant,name,email,time,date) VALUES ('" + dining.getRestaurant() + "', '" + dining.getDinerName() + "', '" + dining.getDinerEmail() + "', '" + time + "', '" + date + "')";
      System.out.println(sql);
      stmt.executeUpdate(sql);
      model.put("dining", dining);
      return "redirect:/thankyou";
    }  catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

    // Is this even in use?? -Markus
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
      //Andy: query diner information
      //System.out.println("SELECT * FROM Dinings WHERE restaurant = '"+ restaurant.getName() +"'");
      ResultSet restaurants = stmt.executeQuery("SELECT * FROM Restaurants WHERE username = '"+restaurant.getUsername()+"'");
      String restaurantName = "";
      while(restaurants.next()) {
        restaurantName = restaurants.getString("name");
      }
      ResultSet diner = stmt.executeQuery("SELECT * FROM Dinings WHERE restaurant = '" +restaurantName+ "'");
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

      //model.put("restaurant", restaurant);
      //return "redirect:/home";
      return "home";
    }  catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
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
        ArrayList<String> rec = new ArrayList<>();
        rec.add(idNew);
        rec.add(name);
        rec.add(email);
        rec.add(time);
        rec.add(date);
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
      //model.put("restaurant", restaurant);

      //Andy: query diner information associated with restaurant name
      //System.out.println("SELECT * FROM Dinings WHERE restaurant = '"+ restaurant.getName() +"'");
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

}
