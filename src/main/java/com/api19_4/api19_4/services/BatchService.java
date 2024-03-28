package com.api19_4.api19_4.services;

import com.api19_4.api19_4.models.Batch;
import com.api19_4.api19_4.repositories.BatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BatchService {
    @Autowired
    private BatchRepository batchRepository;

   public void saveBatch (Batch batch){
       batchRepository.save(batch);
   }

    public void saveBatch(org.hibernate.engine.jdbc.batch.spi.Batch batch) {
    }
}
