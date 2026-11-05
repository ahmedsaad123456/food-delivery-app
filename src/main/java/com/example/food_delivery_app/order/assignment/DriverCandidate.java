package com.example.food_delivery_app.order.assignment;


import com.example.food_delivery_app.order.entity.driver.Driver;

/** A driver plus the data the strategies may need. activeOrders is computed by the caller (one query for all drivers). *
 *
 * @param driver is the driver entity
 * @param activeOrders is the number of active orders assigned to this driver
 */
public record DriverCandidate(Driver driver, int activeOrders) {}