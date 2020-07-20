package services;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import entities.Ticket;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class AbstractParser {

    /**
     * get pairs: product - price
     * @param page
     * @return
     */
    public abstract Map<String, Double> getPositions(HtmlPage page);

    protected int getIndex(List<HtmlElement> items, String point) {
        int index = 0;
        for (HtmlElement item : items) {
            if (item.asText().contains(point)) {
                break;
            }
            index++;
        }
        return index;
    }

    /**
     * get html code of the page by the link
     * @param link
     * @return
     * @throws IOException
     */
    protected HtmlPage getHtmlPage(String link) throws IOException {
        WebClient client = new WebClient();
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        return client.getPage(link);
    }
}
