package com.glovoapp.backender;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@ComponentScan("com.glovoapp.backender")
@EnableAutoConfiguration
public class Application {
}
