package com.wms.warehouse.service;

import com.wms.warehouse.repository.DemandHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smile.data.DataFrame;
import smile.data.Tuple;
import smile.data.formula.Formula;
import smile.data.vector.IntVector;
import smile.data.vector.DoubleVector;
import smile.regression.RandomForest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesService {

    @Autowired
    private DemandHistoryRepository demandHistoryRepository;

    public List<Integer> getMonthlySales(String productCode) {
        LocalDate end = LocalDate.of(2024, 6, 19);
        LocalDate start = end.minusYears(2);
    
        List<Object[]> results = demandHistoryRepository.findMonthlyDemandByProduct(productCode, start, end);
        List<Integer> monthlySales = new ArrayList<>();
    
        for (Object[] row : results) {
            Long total = (Long) row[1];
            monthlySales.add(total != null ? total.intValue() : 0);
        }
    
        return monthlySales;
    }

    public int predictNextMonthDemand(String productCode) {
        List<Integer> history = getMonthlySales(productCode);
        System.out.println("Sales history for " + productCode + ": " + history);
    
        if (history.size() < 6) return 0; // Guard: not enough for training
    
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