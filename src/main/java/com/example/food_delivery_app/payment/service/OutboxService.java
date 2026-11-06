package com.example.food_delivery_app.payment.service;


import com.example.food_delivery_app.payment.repoistory.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository outbox;
    private final JsonMapper jsonMapper; // Jackson 3, the same one every handler uses

    @Transactional(propagation = Propagation.MANDATORY) // MUST be called inside an existing transaction
    public void save(String aggregateId, String type, Object payload) {
        outbox.insert(aggregateId, type, jsonMapper.writeValueAsString(payload));
    }
}