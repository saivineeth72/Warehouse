package com.wms.warehouse.service;

import com.wms.warehouse.repository.DemandHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesService {

    @Autowired
    private DemandHistoryRepository demandHistoryRepository;

    public List<Integer> getMonthlySales(String productCode) {

        LocalDate oneYearAgo = LocalDate.of(2016, 1, 1);
        LocalDate now = LocalDate.of(2017, 1, 1);

        List<Object[]> results = demandHistoryRepository.findMonthlyDemandByProduct(productCode, oneYearAgo, now);
        List<Integer> monthlySales = new ArrayList<>();

        for (Object[] row : results) {
            Long total = (Long) row[1];
            monthlySales.add(total != null ? total.intValue() : 0);
        }

        return monthlySales;
    }
}