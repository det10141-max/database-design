package com.library.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 业务配置项映射（application.yml 中的 app.*）
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Borrow borrow = new Borrow();
    private Fine fine = new Fine();
    private Reserve reserve = new Reserve();

    @Data
    public static class Borrow {
        private int durationDays = 30;
        private int renewDays = 30;
        private int maxRenew = 2;
        private int maxCount = 5;
    }

    @Data
    public static class Fine {
        private double dailyRate = 0.5;
    }

    @Data
    public static class Reserve {
        private int expireDays = 7;
        private int pickupDays = 3;
    }
}
