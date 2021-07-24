package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

//import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ControllerInitializationTest {
    
    @Autowired
    private Main main;
    
    @BeforeAll
    public static void before(){

    }
    @Test
	public void contextLoads() {
        assertThat(main).isNotNull();
	}

    @AfterAll
    public static void after(){

    }
}
