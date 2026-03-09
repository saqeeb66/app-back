package com.arcotcabs.arcotcabs_backend.controller;


import com.arcotcabs.arcotcabs_backend.model.Invoice;
import com.arcotcabs.arcotcabs_backend.service.InvoiceService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/invoice")
@CrossOrigin
public class InvoiceController {

    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    @PostMapping("/{tripId}")
    public Invoice generate(@PathVariable String tripId) {
        return service.generateInvoice(tripId);
    }
}
