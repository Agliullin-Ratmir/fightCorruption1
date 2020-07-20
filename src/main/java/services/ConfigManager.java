package services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
public class ConfigManager {
    private Properties prop = new Properties();
    private static final String propFileName = "src/main/resources/config.properties";
    private static final String KEY_TOTAL_SUM_FLOOR = "totalSumFloor";
    private static final String KEY_MAX_PERCENTAGE = "maxPricePercentage";

    /**
     * take the min limit of the ticket's price which should exam
     * @return
     */
    public Integer getLimitFloor() {
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (prop != null) {
            return Integer.parseInt(prop.getProperty(KEY_TOTAL_SUM_FLOOR));
        } else {
            return null;
        }
    }


    private Properties init() throws IOException {
        if (prop.size() != 0) {
            return prop;
        }
        try(InputStream inputStream = new FileInputStream(propFileName)) {
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return prop;
        }
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
        if (percentage > Double.parseDouble(prop.getProperty(KEY_MAX_PERCENTAGE))) {
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
        return prop.getProperty(key);
    }
}
