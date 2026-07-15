package com.aisalescrm.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OrderNumberGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicInteger sequence = new AtomicInteger(1);

    /**
     * Generates: ORD-20240115-0001
     */
    public String generateOrderNumber() {
        String date = LocalDate.now().format(DATE_FMT);
        int seq = sequence.getAndIncrement() % 10000;
        return String.format("ORD-%s-%04d", date, seq);
    }

    /**
     * Generates: INV-20240115-0001
     */
    public String generateInvoiceNumber() {
        String date = LocalDate.now().format(DATE_FMT);
        int seq = sequence.getAndIncrement() % 10000;
        return String.format("INV-%s-%04d", date, seq);
    }
}
