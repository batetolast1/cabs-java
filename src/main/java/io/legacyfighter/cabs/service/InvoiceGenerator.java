package io.legacyfighter.cabs.service;

import io.legacyfighter.cabs.entity.Invoice;
import io.legacyfighter.cabs.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class InvoiceGenerator {

    private final InvoiceRepository invoiceRepository;

    public InvoiceGenerator(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Invoice generate(Integer amount, String subjectName) {
        return invoiceRepository.save(new Invoice(new BigDecimal(amount), subjectName));
    }
}
