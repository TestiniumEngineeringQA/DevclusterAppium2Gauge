package com.testinium;

import com.testinium.driver.TestiniumAndroidDriver;
import com.testinium.driver.TestiniumIOSDriver;
import com.testinium.util.Constants;
import com.testinium.util.TestiniumEnvironment;
import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import io.appium.java_client.AppiumDriver;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static com.testinium.util.Constants.CapabilityConstants.*;

public class HookImp {

    private Logger logger = LoggerFactory.getLogger(getClass());
    protected static AppiumDriver driver;
    protected URL hubUrl;

    protected static FluentWait<AppiumDriver> appiumFluentWait;

    Boolean DeviceAndroid =false;
    @BeforeScenario
    public void beforeScenario() {
        try {
            System.out.println("isAndroid:" +TestiniumEnvironment.isPlatformAndroid());
                    if(DeviceAndroid || TestiniumEnvironment.isPlatformAndroid()){
                        DesiredCapabilities overridden = new DesiredCapabilities();
                        overridden.setCapability(APPIUM_APP_PACKAGE, "YOUR_APP_PACKAGE");
                        overridden.setCapability(APPIUM_APP_ACTIVITY, "YOUR_APP_ACTIVITY");
                        overridden.setCapability(APPIUM_AUTO_GRANT_PERMISSIONS, true);
                        hubUrl = new URL("http://192.168.1.89:4723/");
                        driver = new TestiniumAndroidDriver(hubUrl,overridden);
                        logDriverCapabilities();

                        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
                        appiumFluentWait = new FluentWait<AppiumDriver>(driver);
                        appiumFluentWait.withTimeout(Duration.ofSeconds(8))
                                .pollingEvery(Duration.ofMillis(350))
                                .ignoring(NoSuchElementException.class);

                        String totalRetryCount = System.getenv("TOTAL_RETRY_COUNT");
                        String currentRetryCount = System.getenv("CURRENT_RETRY_COUNT");

                        System.out.println(">>> [TOTAL_RETRY_COUNT] : " + totalRetryCount);
                        System.out.println(">>> [CURRENT_RETRY_COUNT] : " + currentRetryCount);

                    }
                    else {
                        System.out.println("IOS");
                        hubUrl = new URL("http://192.168.1.89:4723/");
                        DesiredCapabilities overridden = new DesiredCapabilities();
                        overridden.setCapability(APPIUM_BUNDLE_ID, "YOUR_BUNDLE_ID");
                        driver = new TestiniumIOSDriver(hubUrl, overridden);
                        logDriverCapabilities();


                        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
                        appiumFluentWait = new FluentWait<AppiumDriver>(driver);
                        appiumFluentWait.withTimeout(Duration.ofSeconds(8))
                                .pollingEvery(Duration.ofMillis(350))
                                .ignoring(NoSuchElementException.class);

                        String totalRetryCount = System.getenv("TOTAL_RETRY_COUNT");
                        String currentRetryCount = System.getenv("CURRENT_RETRY_COUNT");

                        System.out.println(">>> [TOTAL_RETRY_COUNT] : " + totalRetryCount);
                        System.out.println(">>> [CURRENT_RETRY_COUNT] : " + currentRetryCount);

                    }
        } catch (MalformedURLException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void logDriverCapabilities() {
        Map<String, Object> capabilities = new TreeMap<>(driver.getCapabilities().asMap());
        System.out.println(">>> [APPIUM_DRIVER_CAPABILITIES] : " + maskSensitiveValues(capabilities));
    }

    private Map<String, Object> maskSensitiveValues(Map<String, ?> values) {
        Map<String, Object> maskedValues = new LinkedHashMap<>();

        values.forEach((key, value) -> {
            if (isSensitiveCapability(key)) {
                maskedValues.put(key, "***");
            } else if (value instanceof Map<?, ?> nestedValues) {
                Map<String, Object> stringKeyedValues = new LinkedHashMap<>();
                nestedValues.forEach((nestedKey, nestedValue) ->
                        stringKeyedValues.put(String.valueOf(nestedKey), nestedValue));
                maskedValues.put(key, maskSensitiveValues(stringKeyedValues));
            } else {
                maskedValues.put(key, value);
            }
        });

        return maskedValues;
    }

    private boolean isSensitiveCapability(String capabilityName) {
        String normalizedName = capabilityName.toLowerCase(Locale.ROOT);
        return normalizedName.contains("password")
                || normalizedName.contains("token")
                || normalizedName.contains("secret")
                || normalizedName.contains("accesskey")
                || normalizedName.contains("api_key")
                || normalizedName.contains("apikey")
                || normalizedName.contains("authorization");
    }

    @AfterScenario
    public void afterScenario() {
        try {
                driver.quit();

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
