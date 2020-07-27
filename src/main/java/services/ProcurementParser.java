package services;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import entities.Steps;
import entities.Ticket;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.UtilClass.getStringOfNumbers;
import static utils.UtilClass.separatePositions;

@Component
public class ProcurementParser extends AbstractParser {

    private static final Logger log = LoggerFactory.getLogger(ProcurementParser.class);

//  проверку текста заявки на валидность(только закупки)
    private static final String MAIN_PAGE_LINK =
            "https://zakupki.gov.ru";
    private static final String ITEM_TAG_NAME = "row no-gutters registry-entry__form mr-0";
    private static final String ID_TAG_NAME = "registry-entry__header-mid__number";
    private static final String PRICE_TAG_NAME = "price-block__value";
    private static final String TEXT_TAG_NAME = "registry-entry__body-value";
    private static final String START_PAGE_LINK_FIRST =
            "/epz/order/extendedsearch/results.html?morphology=on&search-filter=Дате+размещения&pageNumber=";
    private static final String START_PAGE_LINK_SECOND =
            "&sortDirection=false&recordsPerPage=_10&showLotsInfoHidden=false&sortBy=UPDATE_DATE&fz44=on&pc=on&currencyIdGeneral=-1&selectedSubjectsIdNameHidden=%7B%7D";
    private static final String SECTION_INFO_TAG_NAME = "section__info";
    private static final String SUPPLIER_RESULTS_LINK = "https://zakupki.gov.ru/epz/order/notice/ea44/view/supplier-results.html?regNumber=";

    private static final String positionItemTagName = "tableBlock__row";

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private ApplicationEventPublisher publisher;

    public List<Ticket> parseTickets() {
        List<Ticket> result = new ArrayList<>();
        int floor = configManager.getLimitFloor();
        try {
            for (int pageNumber = 1; pageNumber <= 3; pageNumber++) {
                HtmlPage page = getHtmlPage(MAIN_PAGE_LINK + START_PAGE_LINK_FIRST + pageNumber + START_PAGE_LINK_SECOND);
                Thread.sleep(1000);
                int i = 0;
                List<HtmlElement> items = (List<HtmlElement>) page.getByXPath("//div[@class='" + ITEM_TAG_NAME + "']");
                for (HtmlElement item : items) {
                    Ticket ticket = null;
                    double price = getPrice(item, i);
                    if (price >= floor) {
                        ticket = getTicketInfo(item, price, i);
                        if (ticket == null ||
                                ticket.getPositions() == null || ticket.getPositions().size() == 0) {
                            continue;
                        }
                        result.add(ticket);
                        System.out.println(ticket.toString());
                    }
                    i++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            log.info(String.format("The first amount is: %s", result.size()));
            EventManager eventManager = new EventManager(this, Steps.GET_ACTUAL_PRICES.toString());
            eventManager.setCollectedTickets(separatePositions(result));
            publisher.publishEvent(eventManager);
            return result;
        }
    }

    private String getId(HtmlElement item, int i) {
        HtmlElement idTag = ((List<HtmlElement>) item.getByXPath("//div[@class='" + ID_TAG_NAME + "']")).get(i);
        HtmlAnchor itemAnchor = ((HtmlAnchor) idTag.getFirstByXPath(".//a"));
        return itemAnchor.asText().replace("№", "").trim();
    }

    private String getLinkOfTicket(HtmlElement item, int i) {
        HtmlElement idTag = ((List<HtmlElement>) item.getByXPath("//div[@class='" + ID_TAG_NAME + "']")).get(i);
        HtmlAnchor itemAnchor = ((HtmlAnchor) idTag.getFirstByXPath(".//a"));
        String link = itemAnchor.getHrefAttribute();
        if (link.startsWith("https://zakupki.gov.ru/")) {
            return link;
        }
        return MAIN_PAGE_LINK + "/" + link;
    }

    public double getPrice(HtmlElement item, int i) {
        HtmlElement idTag = ((List<HtmlElement>) item.getByXPath("//div[@class='" + PRICE_TAG_NAME + "']")).get(i);
        String priceString = idTag.asText().replace(" ", "")
                .replace("₽", "").replace(",", ".").trim();
        return Double.parseDouble(priceString);
    }

    private String getText(HtmlElement item, int i) {
        HtmlElement idTag = ((List<HtmlElement>) item.getByXPath("//div[@class='" + TEXT_TAG_NAME + "']")).get(i);
        return idTag.asText();
    }

    public Ticket getTicketInfo(HtmlElement item, Double price, int i) throws IOException {
        String link = getLinkOfTicket(item, i);
        String id = getId(item, i);
        Ticket ticket = new Ticket(id);
        HtmlPage page = getHtmlPage(link);
        Map<String, Double> positions = getPositions(page);
        if (positions == null || positions.isEmpty()) {
            return null;
        }
        ticket.setPositions(positions);
        if (ticket.getPositions().size() == 0) {
            return ticket;
        }
        ticket.setCustomer(getCustomer(page));
        ticket.setRegion(getRegion(page));
        ticket.setLink(link);
        ticket.setTitle(getText(item, i));
        ticket.setTotalSum(price);
        ticket.setAktdat(getAktdat(SUPPLIER_RESULTS_LINK + id));
        ticket.setFinishTotalSum(getFinishTotalSum(SUPPLIER_RESULTS_LINK + id));
        System.out.println("ticket " + id + "has parsed!");
        return ticket;
    }

    @Override
    public Map<String, Double> getPositions(HtmlPage page) {
        try {
            Map<Integer, Double> prices = new HashMap<>();
            Map<Integer, String> items = new HashMap<>();
            Document document = Jsoup.parse(page.asXml());
            Element sampleDiv = document.getElementById("positionKTRU");
            if (sampleDiv == null) {
                return null;
            }
            int i = 0;
            Elements sections = sampleDiv.getElementsByClass("section__info");
            if (sections == null || sections.isEmpty()) {
                return null;
            }
            for (Element el : sections) {
                items.put(i, el.text().trim());
                ++i;
                System.out.println(el.text().trim());
            }

            i = 0;
            sections = sampleDiv.getElementsByClass("tableBlock__row");
            if (sections == null || sections.isEmpty()) {
                return null;
            }
            for (Element el : sections) {
                int size = el.getElementsByTag("td").size();
                if (size > 2) {
                    String priceString = el.getElementsByTag("td")
                            .get(size - 2).text().replace(",", ".").trim()
                            .replaceAll("\\s", "");
                    if (getStringOfNumbers(priceString) == null) {
                        continue;
                    }
                    prices.put(i, Double.valueOf(priceString));
                    ++i;
                    System.out.println(priceString);
                }
            }
            return mixMaps(prices, items);
        } catch (RuntimeException e) {
            System.out.println("there is the error: " + page.toString());
            return null;
        }
    }

    public double getFinishTotalSum(String link) throws IOException {
        HtmlPage page = getHtmlPage(link);
        List<HtmlElement> items = (List<HtmlElement>) page.getByXPath("//tbody[@class='" + "tableBlock__body" + "']");
        if (items.isEmpty()) {
            return 0.0;
        }
        List<HtmlElement> elements = items.get(0).getHtmlElementsByTagName("td");
        if (elements.size() < 3) {
            return 0.0;
        }
        HtmlElement element = items.get(0).getHtmlElementsByTagName("td").get(3);
        System.out.println(items.get(0).asText().trim());
        System.out.println(element.asText().trim());
        String priceString = getStringOfNumbers(element.asText().trim().replace(",", ".")
                .replaceAll("\\s", ""));
        if (priceString == null) {
            return 0.0;
        }
        return Double.parseDouble(priceString);
    }

    public String getAktdat(String link) throws IOException {//table__cell table__cell-body
        HtmlPage page = getHtmlPage(link);//tableBlock__body
        Document document = Jsoup.parse(page.asXml());
        Elements els = document.select("span:contains(Дата и время формирования результатов определения поставщика)");
        if (els.isEmpty()) {
            return StringUtils.EMPTY;
        }
        return els.get(0).firstElementSibling().nextElementSibling().text().trim();
    }

    private Map<String, Double> mixMaps(Map<Integer, Double> prices, Map<Integer, String> items) {
        Map<String, Double> positions = new HashMap<>();
        for(int i = 0; i < prices.size(); i++) {
            positions.put(items.get(i), prices.get(i));
        }
        return positions;
    }

    private void checkPositions(Map<String, Double> positions, String key, Double newValue) {
        if ((!positions.containsKey(key)) ||
                (positions.containsKey(key) && positions.get(key) < newValue)) {
            positions.put(key, newValue);
        }
    }

    public String getCustomer(HtmlPage page) {
        List<HtmlElement> items = (List<HtmlElement>) page.getByXPath("//span[@class='" + SECTION_INFO_TAG_NAME + "']");
        return items.get(8).asText().trim();
    }

    public String getRegion(HtmlPage page) {
        List<HtmlElement> items = (List<HtmlElement>) page.getByXPath("//span[@class='" + SECTION_INFO_TAG_NAME + "']");
        return items.get(10).asText().trim();
    }

}
