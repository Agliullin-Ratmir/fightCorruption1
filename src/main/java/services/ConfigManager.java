package services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@PropertySource("classpath:/config.properties")
@Component
public class ConfigManager {

    private static final String propFileName = "config.properties";
    private static final String KEY_TOTAL_SUM_FLOOR = "totalSumFloor";
    private static final String KEY_MAX_PERCENTAGE = "maxPricePercentage";

    @Autowired
    private Environment env;
    /**
     * take the min limit of the ticket's price which should exam
     * @return
     */
    public Integer getLimitFloor() {
       return Integer.valueOf(env.getProperty(KEY_TOTAL_SUM_FLOOR));
    }

    /**
     * determine the difference between actual price and maximum price and
     * compare with max difference in percents
     * @param actualPrice
     * @param maxPrice
     * @return
     */
    public boolean isPriceMoreThanMax(double actualPrice, double maxPrice) {
        double percentage = ((actualPrice/maxPrice) - 1)* 100;
        if (percentage > Double.parseDouble(env.getProperty(KEY_MAX_PERCENTAGE))) {
            return true;
        }
        return false;
    }

    /**
     * take property by the key
     * @param key
     * @return
     */
    public String getProperty(String key) {
        return env.getProperty(key);
    }
}
