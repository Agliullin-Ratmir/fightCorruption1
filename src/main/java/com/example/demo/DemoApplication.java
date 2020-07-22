package com.example.demo;

import services.ProcurementParser;
import services.SearcherParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


import java.io.IOException;


@SpringBootApplication
@ComponentScan(basePackages = {"entities", "services"})
public class DemoApplication {

    @Bean
    public ProcurementParser procurementParser() {
        return new ProcurementParser();
    }

    @Bean
    public SearcherParser searcherParser() {
        return new SearcherParser();
    }

    public static void main(String[] args) throws IOException {
        ApplicationContext applicationContext = SpringApplication.run(DemoApplication.class, args);
        ProcurementParser procurementParser = applicationContext.getBean(ProcurementParser.class);
        procurementParser.parseTickets();
    }
}


