package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.test.context.SpringBootTest;

class RestaurantTest {
    static Restaurant p;

    @BeforeAll
    static void setUp(){
        p = new Restaurant();
        p.setName("Markus");
        p.setUsername("MarkusR");
        p.setEmail("MarkusR@sfu.ca");
        p.setPassword("Password");
        p.setId(1);
        p.setPremiumStatus(true);
        p.setExposureDate(LocalDate.now());
        p.setCoordinates(69, 69);
        
    }

    @Test
    public void personFname(){
        assertEquals(69,p.getLatitude());
        assertEquals(69,p.getLongitude());
        assertEquals(LocalDate.now(),p.getExposureDate());
        assertEquals(true,p.getPremiumStatus());
        assertEquals(1,p.getId());
        assertEquals("Markus",p.getName());
        assertEquals("MarkusR",p.getUsername());
        assertEquals("MarkusR@sfu.ca",p.getEmail());
        assertEquals("Password",p.getPassword());
    }

}