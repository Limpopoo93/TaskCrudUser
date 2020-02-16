package by.config;

import java.util.ResourceBundle;

public class DatabasePropertiesUtil {
    private static DatabasePropertiesUtil instance;
    private ResourceBundle resourceBundle;

    private static final String BUNDLE_NAME = "database";

    public static DatabasePropertiesUtil getInstance() {
        if (instance == null) {
            instance = new DatabasePropertiesUtil();
            instance.resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
        }
        return instance;
    }

    public String getProperty(String key) {
        return (String)resourceBundle.getObject(key);
    }
}
