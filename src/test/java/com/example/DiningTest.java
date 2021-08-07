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

class DiningTest {
    static Dining p;

    @BeforeAll
    static void setUp(){
        p = new Dining();
        p.setDinerUsername("MarkusR");
        p.setDinerName("Markus");
        p.setDinerExposed(false);
        p.setDinerEmail("markusrobinson00@gmail.com");
        p.setRestaurant("Restaurant1");
    }

    @Test
    public void StringCheck(){
        assertEquals("Markus",p.getDinerName());
        assertEquals("MarkusR",p.getDinerUsername());
        assertEquals("markusrobinson00@gmail.com",p.getDinerEmail());
        assertEquals("Restaurant1",p.getRestaurant());
        p.setDinerName("steve");
        p.setDinerUsername("steve");
        p.setDinerEmail("steve");
        p.setRestaurant("steve");
        assertEquals("steve",p.getDinerName());
        assertEquals("steve",p.getDinerUsername());
        assertEquals("steve",p.getDinerEmail());
        assertEquals("steve",p.getRestaurant());
    }
    @Test
    public void CheckDate(){
        assertEquals(p.getDate(), LocalDate.now());
        assertEquals("Restaurant1",p.getRestaurant());
    }
    @Test
    public void CheckExposure(){
        assertEquals(false,p.getDinerExposed());
        p.setDinerExposed(true);
        assertEquals(true,p.getDinerExposed());
    }
}
