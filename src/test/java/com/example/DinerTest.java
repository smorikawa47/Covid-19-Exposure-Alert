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

class DinerTest {
    static Diner p;

    @BeforeAll
    static void setUp(){
        p = new Diner();
        p.setName("Markus");
        p.setUsername("MarkusR");
        p.setEmail("MarkusR@sfu.ca");
        p.setPassword("Password");
        p.setExposed(true);
        p.setExposureDate(LocalDate.now());
        p.setTestResult(false);
        p.setTestResultDate(LocalDate.now());
        
    }

    @Test
    public void personFname(){
        assertEquals(LocalDate.now(),p.getTestResultDate());
        assertEquals(false,p.getTestResult());
        assertEquals(LocalDate.now(),p.getExposureDate());
        assertEquals(true,p.wasExposed());
        assertEquals("Markus",p.getName());
        assertEquals("MarkusR",p.getUsername());
        assertEquals("MarkusR@sfu.ca",p.getEmail());
        assertEquals("Password",p.getPassword());
    }

}