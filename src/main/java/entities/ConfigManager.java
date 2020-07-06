package entities;

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
    private static final String propFileName = "src/main/resources/application.properties";

    public Integer getLimitFloor() {
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (prop != null) {
            return Integer.parseInt(prop.getProperty("totalSumFloor"));
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
}
