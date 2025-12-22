package io.wahid.publication.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find application.properties");
                // Handle the error appropriately, e.g., throw an exception
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            // Handle the exception appropriately
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static int getIntProperty(String key) {
        return Integer.parseInt(getProperty(key));
    }

    public static char getCharProperty(String key) {
        return getProperty(key).charAt(0);
    }

    public static boolean getBooleanProperty(String key) {
        return Boolean.getBoolean(getProperty(key));
    }
}