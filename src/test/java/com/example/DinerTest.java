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
    public void DateCheck(){
        assertEquals(LocalDate.now(),p.getTestResultDate());
        assertEquals(LocalDate.now(),p.getExposureDate());

    }
    @Test
    public void BooleanCheck(){
        assertEquals(false,p.getTestResult());
        assertEquals(true,p.wasExposed());
        p.setTestResult(true);
        p.setExposed(false);
        assertEquals(true,p.getTestResult());
        assertEquals(false,p.wasExposed());
    }
    @Test
    public void StringCheck(){
        assertEquals("Markus",p.getName());
        assertEquals("MarkusR",p.getUsername());
        assertEquals("MarkusR@sfu.ca",p.getEmail());
        assertEquals("Password",p.getPassword());
        p.setName("test");
        p.setUsername("test");
        p.setEmail("test");
        p.setPassword("test");
        assertEquals("test",p.getName());
        assertEquals("test",p.getUsername());
        assertEquals("test",p.getEmail());
        assertEquals("test",p.getPassword());
    }

}