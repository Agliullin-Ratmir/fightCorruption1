package services;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import entities.FileEntity;
import entities.Steps;
import entities.Ticket;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import utils.UtilClass;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

import static utils.UtilClass.*;

@Component
public class SearcherParser extends AbstractParser {

    private static final Logger log = LoggerFactory.getLogger(SearcherParser.class);

    private static final String AVITO_PAGE = "https://www.avito.ru";
    private static final String AVITO_SEARCH_PAGE = AVITO_PAGE + "/rossiya?q=";
    private static final String PAGE_PART = "&p=";
    private static final int LIMIT_PAGES = 1;

    @Autowired
    private ApplicationEventPublisher publisher;

    public Map<String, Double> parse(String query) throws IOException {
        Map<String, Double> positions = new HashMap<>();
        for(int i = 1; i <= LIMIT_PAGES; i++) {
            HtmlPage page = getHtmlPage(AVITO_SEARCH_PAGE + query + PAGE_PART + i);
            positions.putAll(getPositions(page));
        }
        return positions;
    }

    /**
     * take position of the product with min price
     * @param positions
     * @return
     */
    public Map<String, Double> getPositionWithMinPrice(Map<String, Double> positions) {
        int size = positions.size();
        Set<String> keys = positions.keySet();
        String minKey = null;
        //set minPrice max value for finding min value
        Double minPrice = positions.values().stream().mapToDouble(e -> e).max().getAsDouble();
        for (String key : keys) {
            Double price = positions.get(key);
            if (price < minPrice) {
                minKey = key;
                minPrice = price;
            }
        }
        Map<String, Double> result = new HashMap<>();
        result.put(minKey, minPrice);
        return result;
    }

    /**
     * take position of the product with max price
     * @param positions
     * @return
     */
    public Map<String, Double> getPositionWithMaxPrice(Map<String, Double> positions) {
        int size = positions.size();
        Set<String> keys = positions.keySet();
        String maxKey = null;
        Double maxPrice = 0.0;
        for (String key : keys) {
            Double price = positions.get(key);
            if (price > maxPrice) {
                maxKey = key;
                maxPrice = price;
            }
        }
        Map<String, Double> result = new HashMap<>();
        result.put(maxKey, maxPrice);
        return result;
    }

    /**
     * calc average price
     * @param positions
     * @return
     */
    public Double getAvgPrice(Map<String, Double> positions) {
        return UtilClass.calcAvgPrice(positions.values());
    }

    @Override
    public Map<String, Double> getPositions(HtmlPage page) {
        Map<String, Double> positions = new HashMap<>();
        Document document = Jsoup.parse(page.asXml());
        Elements prices = document.getElementsByClass("snippet-price ");
        Elements links = document.getElementsByClass("snippet-link");
        int size = prices.size();
        for(int i = 0; i < size; i++) {
            String link = AVITO_PAGE + links.get(i).attr("href");
            String priceString = UtilClass.getStringOfNumbers(prices.get(i).text());
            if (priceString == null) {
                continue;
            }
             positions.put(link, Double.valueOf(priceString.trim()));
        }
        log.info("The positions are collected");
        return positions;
    }

    /**
     * Listener for parsing actual prices of products
     * @param eventManager
     * @throws IOException
     */
   @EventListener(condition = "#eventManager.step.equals('GET_ACTUAL_PRICES')")
    public void getActualPrices(EventManager eventManager) throws IOException {
       System.out.println("Collecting is finished");
       List<Ticket> inputTickets = eventManager.getCollectedTickets();
       List<FileEntity> fileEntities = new ArrayList<>();
       log.info(String.format("The inputTickets' amount is: %s",
               inputTickets.size()));
       int i = 0;
       for (Ticket ticket : inputTickets) {
           Map<String, Double> map  = parse(ticket.getTitle());
           Double avgPrice = getAvgPrice(map);
           Map<String, Double> maxPrice = getPositionWithMaxPrice(map);
           Map<String, Double> minPrice = getPositionWithMinPrice(map);
           fileEntities.add(new FileEntity(ticket, getFirstValueFromMap(maxPrice),
                   getFirstKeyFromMap(maxPrice), getFirstValueFromMap(minPrice),
                   getFirstKeyFromMap(minPrice), avgPrice));
           if (i > 10) {
               break;
           } else {
               i++;
           }
           log.info(String.format("The current step is: %s",
                   i));
       }
       log.info("Collecting is finished");
       log.info(String.format("The fileEntities' amount is: %s",
               fileEntities.size()));
       eventManager = new EventManager(this, Steps.CREATE_FILE.toString());
       eventManager.setFileEntities(fileEntities);
       publisher.publishEvent(eventManager);
   }
}
