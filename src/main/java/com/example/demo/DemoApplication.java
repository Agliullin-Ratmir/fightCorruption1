package com.example.demo;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import entities.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.EventManager;
import services.ProcurementParser;
import services.SearcherParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import utils.UtilClass;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static utils.UtilClass.separatePositions;


@SpringBootApplication
@ComponentScan(basePackages = {"entities", "services"})
public class DemoApplication {

    private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

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
//        Ticket ticket = new Ticket("id");
//        Map<String, Double> map = procurementParser.getPositions(getHtmlPage("https://zakupki.gov.ru/epz/order/notice/ea44/view/common-info.html?regNumber=0131200001020006002&backUrl=750d5071-aae2-45b0-a5ff-3d282154f69d"));
//        ticket.setPositions(map);
//        System.out.println(separatePositions(Collections.singletonList(ticket)).size());
//        SearcherParser parser = applicationContext.getBean(SearcherParser.class);
//        Map<String, Double> map  = parser.parse("Аккумулятор для телефона Samsung");
//        System.out.println("Min price here: ");
//        UtilClass.printMap(parser.getPositionWithMinPrice(map));
//
//        System.out.println("Max price here: ");
//        UtilClass.printMap(parser.getPositionWithMaxPrice(map));
//
//        System.out.println("Average price here: " + parser.getAvgPrice(map));
//        log.info("Done");
        }

    protected static HtmlPage getHtmlPage(String link) throws IOException {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        return client.getPage(link);
    }
}
