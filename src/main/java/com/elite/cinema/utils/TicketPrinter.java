package com.elite.cinema.utils;

import com.elite.cinema.models.tables.pojos.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class TicketPrinter {

    public static void printTickets(long reservationId, String movie, LocalDateTime showTime, String room, List<ReservedSeats> seats) {

        try {
            // Prepare data for each ticket
            List<Map<String, String>> ticketsData = new ArrayList<>();

            for (ReservedSeats seat : seats) {
                Map<String, String> ticketData = new HashMap<>();

                // Basic reservation info
                ticketData.put("orderId", Long.toString(reservationId));
                ticketData.put("movieTitle", movie);
                ticketData.put("roomName", room);
                ticketData.put("screeningDate", DateHelper.formatDate(showTime.toLocalDate()));
                ticketData.put("screeningTime", DateHelper.formatTime(showTime.toLocalTime()));

                // Seat information
                int row = seat.getRow().intValue();
                int col = seat.getColumn().intValue();
                String seatLabel = String.format("%c-%d", (char)('A' + row), col + 1);
                ticketData.put("seatLabel", seatLabel);

                // Determine seat type and price
                String seatType;
                if (row <= 3) {
                    seatType = "Regular";
                } else if (row <= 7) {
                    seatType = "VIP";
                } else {
                    seatType = "Recliner";
                }

                ticketData.put("seatType", seatType);

                // Add ticket code for barcode
                ticketData.put("ticketCode", generateTicketCode(reservationId, row, col));

                ticketsData.add(ticketData);
            }

            // Create the data source
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(ticketsData);

            // Load report template
            InputStream templateStream = TicketPrinter.class.getResourceAsStream("/ticket_template.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);

            // Fill the report
            var jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<>(), dataSource);

            // Show preview and print dialog
            showPreviewAndPrint(jasperPrint);

        } catch (JRException e) {
            showError("Error generating tickets", e.getMessage());
        }
    }

    private static String generateTicketCode(Long reservationId, int row, int col) {
        return String.format("%s-%02d-%02d", reservationId, row, col);
    }

    public static void showPreviewAndPrint(JasperPrint jasperPrint) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Print Tickets");
        alert.setHeaderText("Your tickets are ready");
        alert.setContentText("Would you like to preview or print your tickets?");

        ButtonType previewButton = new ButtonType("Preview");
        ButtonType printButton = new ButtonType("Print");
        ButtonType cancelButton = new ButtonType("Cancel");

        alert.getButtonTypes().setAll(previewButton, printButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == previewButton) {
                // Show preview
                JasperViewer.viewReport(jasperPrint, false);
            } else if (result.get() == printButton) {
                // Print directly
                printReport(jasperPrint);
            }
        }
    }

    public static void printReport(JasperPrint jasperPrint) {
        try {
            JasperPrintManager.printReport(jasperPrint, true);
            showSuccess();
        } catch (JRException e) {
            showError("Printing Error", e.getMessage());
        }
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void showSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Printing");
        alert.setHeaderText(null);
        alert.setContentText("Tickets sent to printer");
        alert.showAndWait();
    }
}