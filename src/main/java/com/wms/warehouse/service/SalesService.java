package com.wms.warehouse.service;

import com.wms.warehouse.repository.SalesHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.vector.DoubleVector;
import smile.regression.RandomForest;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Service
public class SalesService {

    @Autowired
    private SalesHistoryRepository salesHistoryRepository;

    public List<Integer> getMonthlySales(String productName) {
        // Only consider sales from the last one month
        LocalDate now = LocalDate.now();
        LocalDate oneMonthAgo = now.minusMonths(1);
        List<Object[]> results = salesHistoryRepository.findMonthlySalesByProduct(productName);
        List<Integer> monthlySales = new ArrayList<>();
        for (Object[] row : results) {
            // row[0] is the month, row[1] is the sum
            // Only include if month is within the last month
            Object monthObj = row[0];
            LocalDate monthDate;
            if (monthObj instanceof java.sql.Timestamp) {
                monthDate = ((java.sql.Timestamp) monthObj).toLocalDateTime().toLocalDate();
            } else if (monthObj instanceof java.sql.Date) {
                monthDate = ((java.sql.Date) monthObj).toLocalDate();
            } else if (monthObj instanceof java.time.LocalDate) {
                monthDate = (java.time.LocalDate) monthObj;
            } else if (monthObj instanceof java.time.LocalDateTime) {
                monthDate = ((java.time.LocalDateTime) monthObj).toLocalDate();
            } else if (monthObj instanceof String) {
                monthDate = LocalDate.parse((String) monthObj);
            } else {
                throw new RuntimeException("Unknown date type: " + monthObj.getClass());
            }
            if (!monthDate.isBefore(oneMonthAgo)) {
                Long total = (Long) row[1];
                monthlySales.add(total != null ? total.intValue() : 0);
            }
        }
        return monthlySales;
    }

    public int predictNextMonthDemand(String productName) {
        List<Integer> history = getMonthlySales(productName);
        System.out.println("Sales history for " + productName + ": " + history);

        if (history.size() == 0) return 0;
        if (history.size() < 4) {
            // Fallback: simple average
            return (int) Math.round(history.stream().mapToInt(i -> i).average().orElse(0));
        }

        int n = history.size() - 3;
        if (n <= 0) return 0; // Another guard

        double[] feature1 = new double[n];
        double[] feature2 = new double[n];
        double[] feature3 = new double[n];
        double[] label = new double[n];

        for (int i = 0; i < n; i++) {
            feature1[i] = history.get(i);
            feature2[i] = history.get(i + 1);
            feature3[i] = history.get(i + 2);
            label[i] = history.get(i + 3);
        }

        if (n < 2) return 0; // Ensure minimum rows for RandomForest

        DataFrame df = DataFrame.of(
            DoubleVector.of("f1", feature1),
            DoubleVector.of("f2", feature2),
            DoubleVector.of("f3", feature3),
            DoubleVector.of("label", label)
        );

        RandomForest rf = RandomForest.fit(
            Formula.lhs("label"), df
        );

        double[] latest = {
            history.get(history.size() - 3),
            history.get(history.size() - 2),
            history.get(history.size() - 1)
        };

        DataFrame predictionDf = DataFrame.of(
            DoubleVector.of("f1", new double[]{latest[0]}),
            DoubleVector.of("f2", new double[]{latest[1]}),
            DoubleVector.of("f3", new double[]{latest[2]})
        );

        return (int) Math.round(rf.predict(predictionDf.get(0)));
    }
}