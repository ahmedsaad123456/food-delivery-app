package com.example.food_delivery_app.order.entity.driver;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


// Document for storing order status changes in MongoDB; this is used for the order timeline
// Document like @Entity, but for MongoDB; it is stored in a collection (like a table) instead of a relational database
// Collection name is "order_timeline"; it is created on the first insert
// Collection -> Document, Document -> Record -> Fields
@Document(collection = "order_timeline") // the "table"; it is created on the first insert
@CompoundIndex(name = "order_time_idx", def = "{'orderId': 1, 'timestamp': 1}") // index for querying by orderId and timestamp
public record OrderTimelineEntry(
        @Id String id,        // Mongo generates an ObjectId (as a String) when this is null
        Integer orderId,      // reference to orders.id in Postgres (NOT enforced by any foreign key)
        String status,
        Instant timestamp,
        String note) {}