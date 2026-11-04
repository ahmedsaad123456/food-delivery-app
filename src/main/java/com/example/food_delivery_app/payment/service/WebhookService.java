package com.example.food_delivery_app.payment.service;

import com.example.food_delivery_app.payment.gateway.PaymentEvent;
import com.example.food_delivery_app.payment.gateway.PaymentEventType;
import com.example.food_delivery_app.payment.repoistory.ProcessedEventStore;
import com.example.food_delivery_app.payment.webhock.WebhookHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WebhookService {

    private final ProcessedEventStore processedEvents;
    private final Map<PaymentEventType, WebhookHandler> handlers;

    public WebhookService(ProcessedEventStore processedEvents, List<WebhookHandler> allHandlers) {
        this.processedEvents = processedEvents;
        // two handlers for the same type -> IllegalStateException at startup (fail fast)
        this.handlers = allHandlers.stream()
                .collect(Collectors.toMap(WebhookHandler::type, Function.identity()));
    }

    // ONE transaction: the event id and every change made by the handler are saved together or not at all
    @Transactional
    public void process(PaymentEvent event) {

        // The event id is unique, so if we have already processed it, we ignore it.
        // This is important because Stripe may send the same event multiple times.
        if (!processedEvents.markProcessed(event.eventId(), event.type().name())) {
            log.info("Webhook event {} was already processed: ignored", event.eventId());
            return;                                                // 200
        }
        WebhookHandler handler = handlers.get(event.type());
        if (handler == null) {
            log.debug("No handler for {}: ignored", event.type());  // UNKNOWN: an event we do not care about
            return;                                                // 200
        }
        handler.handle(event);        // an unchecked exception here rolls everything back, including the id row
    }
}