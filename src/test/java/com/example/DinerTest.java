package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.*;

public class DinerTest {
    static Diner d;


    @BeforeAll
    static void setUp(){
        d = new Diner();
        d.setName("Gaelan");
        d.setEmail("gaelan@sfu.ca");
    }

    @Test
    public void dinerName(){
        assertEquals("Gaelan", d.getName());
        assertEquals("gaelan@sfu.ca", d.getEmail());
    }
}
