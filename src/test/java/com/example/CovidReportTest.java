package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CovidReportTest {
    static Diner diner;
    static Dining dining;
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
            LocalDate date = LocalDate.now();
            try (Connection connection = dataSource.getConnection()) {
                Statement stmt = connection.createStatement();
                String sql = "INSERT INTO Dinings (restaurant,name,email,time,date,exposed) VALUES ('testRestaurant', 'GaelanTest', 'GaelanTest@sfu.ca', '', '" + date + "', 'false')";
                stmt.executeUpdate(sql);
            } catch (Exception e) {
             
            }
            
            System.out.println("About to report COVID-19...");
            main.loginToDinerAccount2(model, diner);
        }
        catch (Exception e){
            assertEquals(true, false);
        }
    }    

    @Test
    public static void checkForReport(){
        assertEquals(true, true);
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM Dinings WHERE email = '" + diner.getEmail() + "' AND exposed = TRUE";
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
            String sql = "DELETE FROM Dinings WHERE email = '" + diner.getEmail() + "'";
            stmt.executeUpdate(sql);
        } catch (Exception e) {
         
        }
    }
    
}

