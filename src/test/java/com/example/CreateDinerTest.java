package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CreateDinerTest {
    static Diner diner;
    static Main main;
    static Map<String, Object> model;
    static DataSource dataSource = main.getDataSource();

    @BeforeAll
    public static void setUp(){
        diner = new Diner();
        diner.setName("GaelanTest");
        diner.setEmail("GaelanTest@sfu.ca");
        diner.setPassword("testpassword");
        diner.setUsername("GaelanTest");
        System.out.println("About to create a diner account...");
        main.createDinerAccount(model, diner);
    }    

    @Test
    public static void checkForDiner(){
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM Diners WHERE email = '" + diner.getEmail() + "'";
            ResultSet dinersWithMatchingEmail = stmt.executeQuery(sql);
            assertEquals(true, dinersWithMatchingEmail.isBeforeFirst());
            dinersWithMatchingEmail.next();
        } catch (Exception e) {
         
        }
    }

    @AfterAll
    public static void tearDown(){
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            String sql = "DELETE FROM Diners WHERE email = '" + diner.getEmail() + "'";
            stmt.executeUpdate(sql);
        } catch (Exception e) {
         
        }
    }
    
}

