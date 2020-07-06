package entities;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcurementParser {

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

    public static List<Ticket> parseTickets() {
        List<Ticket> result = new ArrayList<>();
        int floor = new ConfigManager().getLimitFloor();
        try {
            for (int pageNumber = 1; pageNumber <= 3; pageNumber++) {
                HtmlPage page = getHtmlPage(MAIN_PAGE_LINK + START_PAGE_LINK_FIRST + pageNumber + START_PAGE_LINK_SECOND);
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
            return result;
        }
    }

    private static String getId(HtmlElement item, int i) {
        HtmlElement idTag = ((List<HtmlElement>) item.getByXPath("//div[@class='" + ID_TAG_NAME + "']")).get(i);
        HtmlAnchor itemAnchor = ((HtmlAnchor) idTag.getFirstByXPath(".//a"));
        return itemAnchor.asText().replace("№", "").trim();
    }

    private static String getLinkOfTicket(HtmlElement item, int i) {
        HtmlElement idTag = ((List<HtmlElement>) item.getByXPath("//div[@class='" + ID_TAG_NAME + "']")).get(i);
        HtmlAnchor itemAnchor = ((HtmlAnchor) idTag.getFirstByXPath(".//a"));
        String link = itemAnchor.getHrefAttribute();
        if (link.startsWith("https://zakupki.gov.ru/")) {
            return link;
        }
        return MAIN_PAGE_LINK + "/" + link;
    }

    private static double getPrice(HtmlElement item, int i) {
        HtmlElement idTag = ((List<HtmlElement>) item.getByXPath("//div[@class='" + PRICE_TAG_NAME + "']")).get(i);
        String priceString = idTag.asText().replace(" ", "")
                .replace("₽", "").replace(",", ".").trim();
        return Double.parseDouble(priceString);
    }

    private static String getText(HtmlElement item, int i) {
        HtmlElement idTag = ((List<HtmlElement>) item.getByXPath("//div[@class='" + TEXT_TAG_NAME + "']")).get(i);
        return idTag.asText();
    }

    public static Ticket getTicketInfo(HtmlElement item, Double price, int i) throws IOException {
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

    //https://www.tutorialspoint.com/jsoup/jsoup_use_dom.htm

    public static Map<String, Double> getPositions(HtmlPage page) {
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
                    prices.put(i, Double.valueOf(priceString));
                    ++i;
                    System.out.println(priceString);
                    break;
                }
            }
            return mixMaps(prices, items);
        } catch (RuntimeException e) {
            System.out.println("there is the error: " + page.toString());
            return null;
        }
    }

    public static double getFinishTotalSum(String link) throws IOException {
        HtmlPage page = getHtmlPage(link);//tableBlock__body
        List<HtmlElement> items = (List<HtmlElement>) page.getByXPath("//tbody[@class='" + "tableBlock__body" + "']");
        List<HtmlElement> elements = items.get(0).getHtmlElementsByTagName("td");
        if (elements.size() < 3) {
            return 0.0;
        }
        HtmlElement element = items.get(0).getHtmlElementsByTagName("td").get(3);
        System.out.println(items.get(0).asText().trim());
        System.out.println(element.asText().trim());
        String priceString = element.asText().trim().replace(",", ".")
                .replaceAll("\\s", "");
        if (priceString.isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(priceString);
    }

    public static String getAktdat(String link) throws IOException {//table__cell table__cell-body
        HtmlPage page = getHtmlPage(link);//tableBlock__body
        Document document = Jsoup.parse(page.asXml());
        Elements els = document.select("span:contains(Дата и время формирования результатов определения поставщика)");
        return els.get(0).firstElementSibling().nextElementSibling().text().trim();
    }

    private static Map<String, Double> mixMaps(Map<Integer, Double> prices, Map<Integer, String> items) {
        Map<String, Double> positions = new HashMap<>();
        for(int i = 0; i < prices.size(); i++) {
            positions.put(items.get(i), prices.get(i));
        }
        return positions;
    }

    private static void checkPositions(Map<String, Double> positions, String key, Double newValue) {
        if ((!positions.containsKey(key)) ||
                (positions.containsKey(key) && positions.get(key) < newValue)) {
            positions.put(key, newValue);
        }
    }

    public static String getCustomer(HtmlPage page) {
        List<HtmlElement> items = (List<HtmlElement>) page.getByXPath("//span[@class='" + SECTION_INFO_TAG_NAME + "']");
        return items.get(8).asText().trim();
    }

    public static String getRegion(HtmlPage page) {
        List<HtmlElement> items = (List<HtmlElement>) page.getByXPath("//span[@class='" + SECTION_INFO_TAG_NAME + "']");
        return items.get(10).asText().trim();
    }

    //в абстрактный класс
    private static int getIndex(List<HtmlElement> items, String point) {
        int index = 0;
        for (HtmlElement item : items) {
            if (item.asText().contains(point)) {
                break;
            }
            index++;
        }
        return index;
    }

    //вынести в асбстрактный класс
    public static HtmlPage getHtmlPage(String link) throws IOException {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        return client.getPage(link);
    }
}
