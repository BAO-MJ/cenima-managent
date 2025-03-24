package com.elite.cinema.utils;

import com.elite.cinema.models.tables.pojos.Refreshments;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.jooq.types.ULong;

import java.time.LocalDateTime;
import java.util.*;

import static com.elite.cinema.utils.TicketPrinter.showError;

public class RefreshmentReceiptPrinter {

    public static void printReceipt(ULong orderId, Map<ULong, Integer> cart,
                                    List<Refreshments> refreshmentsList, long subtotal) {
        try {
            // Load the JRXML template
            JasperReport jasperReport = JasperCompileManager.compileReport(RefreshmentReceiptPrinter.class.getResourceAsStream("/refreshments_receipt_template.jrxml"));

            // Prepare receipt data
            List<Map<String, Object>> dataList = new ArrayList<>();

            for (Map.Entry<ULong, Integer> entry : cart.entrySet()) {
                ULong refreshmentId = entry.getKey();
                Integer quantity = entry.getValue();

                // Find refreshment details
                Refreshments refreshment = refreshmentsList.stream()
                        .filter(r -> r.getId().equals(refreshmentId))
                        .findFirst()
                        .orElse(null);

                if (refreshment != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", refreshment.getName());
                    item.put("quantity", quantity.intValue());
                    item.put("price", PriceFormatter.format(refreshment.getPrice().longValue()));
                    item.put("total", PriceFormatter.format(refreshment.getPrice().longValue() * quantity));
                    dataList.add(item);
                }
            }

            // Prepare parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("OrderId", orderId.toString());
            parameters.put("OrderDate", DateHelper.formatDateTime(LocalDateTime.now()));
            parameters.put("Subtotal", PriceFormatter.format(subtotal));
            parameters.put("Tax", PriceFormatter.format(subtotal / 10));
            parameters.put("Total", PriceFormatter.format(subtotal / 10 * 11));

            // Create data source
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);

            // Fill the report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            TicketPrinter.printReport(jasperPrint);
        } catch (Exception e) {
            showError("Error generating tickets", e.getMessage());
        }
    }
}