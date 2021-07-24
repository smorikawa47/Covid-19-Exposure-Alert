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
public class CovidReportNoDiningsTest {
    static Diner diner;
    static Main main;
    static Map<String, Object> model;
    static DataSource dataSource = main.getDataSource();

    @BeforeAll
    public static void setUp() throws Exception {
        try {
            diner = new Diner();
            diner.setName("GaelanTest");
            diner.setEmail("GaelanTest@sfu.ca");
            diner.setPassword("testpassword");
            diner.setUsername("GaelanTest");
            System.out.println("About to report COVID-19...");
            main.loginToDinerAccount2(model, diner);
        }
        catch (Exception e){
            assertEquals(true, false);
        }
    }    

    @Test
    public static void checkForDiner(){
        assertEquals(true, true);
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM Diners WHERE email = '" + diner.getEmail() + "'";
            ResultSet dinersWithMatchingEmail = stmt.executeQuery(sql);
            assertEquals(false, dinersWithMatchingEmail.isBeforeFirst());
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

