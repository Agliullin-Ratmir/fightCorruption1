package utils;

import entities.FileEntity;
import entities.Ticket;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilClass {

    public static FileEntity convertTicketToFileEntity(Ticket ticket,
                                                       double maxPrice, double minPrice, double avgPrice) {
        return new FileEntity(ticket, maxPrice, minPrice, avgPrice);
    }

    /**
     * calculating average price of the collection of prices
     * @param prices
     * @return
     */
    public static double calcAvgPrice(Collection<Double> prices) {
        if (prices.size() == 0) {
            return 0.0;
        }
        OptionalDouble average = prices.stream().mapToDouble(e -> e).average();
        if (average.isPresent()) {
            return average.getAsDouble();
        }
        return 0.0;
    }

    /**
     * printing map
     * @param positions
     */
    public static void printMap(Map<String, Double> positions) {
        for(Map.Entry pos : positions.entrySet()) {
            System.out.println("Link: " + pos.getKey() + " Price: " + pos.getValue());
        }
    }

    /**
     * check whether the string contains numbers
     * @param in
     * @return
     */
    public static String getStringOfNumbers(String in) {
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(in);
        if(m.find()) {
            return m.group();
        }
        return null;
    }

    /**
     * make separated entity of tickets for each position from original ticket
     * @param tickets
     * @return
     */
    public static List<Ticket> separatePositions(List<Ticket> tickets) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket.getPositions().size() == 1) {
                result.add(ticket);
                continue;
            }
            for (Map.Entry<String, Double> position : ticket.getPositions().entrySet()) {
                Ticket newTicket = new Ticket(ticket.getId(), ticket.getCustomer());
                newTicket.setTitle(ticket.getTitle());
                newTicket.setRegion(ticket.getRegion());
                newTicket.setAktdat(ticket.getAktdat());
                newTicket.setTotalSum(ticket.getTotalSum());
                newTicket.setLink(ticket.getLink());

                Map<String, Double> newPosition = new HashMap<>();
                newPosition.put(position.getKey(), position.getValue());
                newTicket.setPositions(newPosition);
                result.add(newTicket);
            }
        }
        return result;
    }

    /**
     * Take first key from map. It needs when the map has only one pair.
     * @param map
     * @return
     */
    public static String getFirstKeyFromMap (Map map) {
        if (map.keySet().isEmpty()) {
            return StringUtils.EMPTY;
        }
        if (map.keySet().iterator().hasNext()) {
            return map.keySet().iterator().next().toString();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Take first value from map. It needs when the map has only one pair.
     * @param map
     * @return
     */
    public static Double getFirstValueFromMap (Map map) {
        if (map.keySet().isEmpty()) {
            return 0.0;
        }
        if (map.values().iterator().hasNext()) {
            return Double.valueOf(map.values().iterator().next().toString());
        }
        return 0.0;
    }
}
