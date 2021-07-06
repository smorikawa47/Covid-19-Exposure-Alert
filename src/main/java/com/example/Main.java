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

  @GetMapping("/diningreport")
  public String diningReport(Map<String, Object> model){
    Dining dining = new Dining();
    model.put("dining", dining);
    return "diningreport";
  }

  // // 
  /*Do we need this ? Done by Tauseef Added this 05.06 */
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

  @GetMapping("/adminlogin")
  public String adminLogin(Map<String, Object> model){
    Restaurant restaurant = new Restaurant();
    model.put("restaurant", restaurant);
    return "adminlogin";
  }

  @PostMapping("/adminloginpage")
  public String adminLoginPage(Map<String, Object> model){
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
      model.put("restaurant", restaurant);
      return "redirect:/home";
    }  catch (Exception e) {
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
      String sql = "INSERT INTO Restaurants (name,username,password,premium) VALUES ('" + restaurant.getName() + "', '" + restaurant.getUsername() + "', '" + restaurant.getPassword() + "', " + restaurant.getPremiumStatus() + ")";
      System.out.println(sql);
      stmt.executeUpdate(sql);
      model.put("restaurant", restaurant);
      return "redirect:/home";
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
