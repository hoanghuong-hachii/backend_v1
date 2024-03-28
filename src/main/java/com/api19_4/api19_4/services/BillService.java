package com.api19_4.api19_4.services;

import com.api19_4.api19_4.dto.BillDTO;
import com.api19_4.api19_4.models.Bill;
import com.api19_4.api19_4.repositories.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
@Service
public class BillService {
    private final BillRepository billRepository;

    @Autowired
    public BillService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

}
