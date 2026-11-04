package com.example.food_delivery_app.payment.stripe;

import com.example.food_delivery_app.common.Constants;
import com.example.food_delivery_app.exceptions.InvalidWebhookSignatureException;
import com.example.food_delivery_app.exceptions.PaymentGatewayException;
import com.example.food_delivery_app.payment.gateway.*;
import com.example.food_delivery_app.payment.properties.StripeProperties;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.CouponCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;


// The only class that knows about Stripe's SDK.
// It implements the two ports (interfaces) that the application uses to talk to a payment gateway.
// The rest of the app doesn't know or care that Stripe is being used.
// Adaptor pattern: this class adapts Stripe's SDK to the application's ports (interfaces).
// this class adapts the Stripe SDK to the application's ports (interfaces) for creating checkout sessions and processing refunds.
// It encapsulates the Stripe-specific logic and provides a clean interface for the rest of the application to interact with the payment gateway.
@Component
@RequiredArgsConstructor
@Slf4j
public class StripePaymentGateway
        implements CheckoutSessionCreator, PaymentRefund, WebhookSignatureVerifier {

    private final StripeProperties props;

    // ---------- Port 1: create a Checkout Session ----------
    @Override
    public CheckoutSession createSession(CheckoutRequest r) {
        String currency = r.currency().toLowerCase();               // Stripe wants lowercase
        try {
            SessionCreateParams.Builder params = SessionCreateParams.builder() // use builder to avoid long constructor with many params
                    .setMode(SessionCreateParams.Mode.PAYMENT)       // one-time payment, not a subscription
                    .setSuccessUrl(r.successUrl()) // the user will be redirected here after successful payment
                    .setCancelUrl(r.cancelUrl()) // the user will be redirected here if they cancel the payment
                    .setExpiresAt(Instant.now().plus(Duration.ofMinutes(31)).getEpochSecond()) // session expires in 31 minutes ( reuse window is 25 minutes, so we give a bit more time for the user to complete the payment)
                    .putMetadata(Constants.ORDER_ID, String.valueOf(r.orderId()))          // comes back on checkout.session.* events to help us identify the order
                    .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
                            .putMetadata(Constants.ORDER_ID, String.valueOf(r.orderId()))  // comes back on payment_intent.* events to help us identify the order
                            .build());

            for (CheckoutLine line : r.lines()) { // add each line item to the session like (name, quantity, unit price)
                params.addLineItem(SessionCreateParams.LineItem.builder() // Stripe wants a list of line items, each with a name, quantity, and unit price
                        .setQuantity((long) line.quantity()) // Stripe wants quantity as long, not int
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder() // Stripe wants price data for each line item, including currency, unit amount, and product data
                                .setCurrency(currency) // Stripe wants currency in lowercase, e.g. "usd" , "egp"
                                .setUnitAmount(toMinorUnits(line.unitPrice(), r.currency())) // Stripe wants unit amount in minor units, e.g. 126.00 EGP -> 12600
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder() // Stripe wants product data for each line item, including name
                                        .setName(line.name()) // Stripe wants product name, e.g. "Pizza Margherita"
                                        .build())
                                .build())
                        .build());
            }

            // If the order has a discount, create a coupon in Stripe and add it to the session.
            // The coupon is created with a unique idempotency key to avoid creating multiple coupons for the same order.
            if (r.discount().signum() > 0) {
                Coupon coupon = Coupon.create(CouponCreateParams.builder() // create a coupon in Stripe with the discount amount and currency
                        .setAmountOff(toMinorUnits(r.discount(), r.currency())) // Stripe wants amount off in minor units, e.g. 10.00 EGP -> 1000
                        .setCurrency(currency) // Stripe wants currency in lowercase, e.g. "usd" , "egp"
                        .setDuration(CouponCreateParams.Duration.ONCE) // the coupon can be used only once
                        .build(), options(r.idempotencyKey() + "-coupon")); // use a unique idempotency key for the coupon to avoid creating multiple coupons for the same order
                params.addDiscount(SessionCreateParams.Discount.builder().setCoupon(coupon.getId()).build()); // add the coupon to the session as a discount
            }

            // Create the session in Stripe using the params and the idempotency key.
            // The session id and url are returned to the caller.
            // idempotency key is used to avoid creating multiple sessions for the same order if the request is retried.
            Session session = Session.create(params.build(), options(r.idempotencyKey()));
            return new CheckoutSession(session.getId(), session.getUrl());
        } catch (StripeException e) {
            throw new PaymentGatewayException(e);    // the SDK exception never leaves this class
        }
    }

    // ---------- Port 2: refund ----------
    @Override
    public void refundPayment(String paymentReference, BigDecimal amount, String currency, String idempotencyKey) {
        try {
            Refund.create(RefundCreateParams.builder() // create a refund in Stripe with the payment reference, amount, and currency
                    .setPaymentIntent(paymentReference) // the payment reference is the Stripe payment intent id, which is returned in the checkout.session.completed event
                    .setAmount(toMinorUnits(amount, currency)) // Stripe wants amount in minor units, e.g. 10.00 EGP -> 1000
                    .build(), options(idempotencyKey)); // use a unique idempotency key for the refund to avoid creating multiple refunds for the same payment
        } catch (StripeException e) {
            throw new PaymentGatewayException(e);
        }
    }

    // The API key travels with each request, so we never touch the static Stripe.apiKey
    private RequestOptions options(String idempotencyKey) {
        return RequestOptions.builder()
                .setApiKey(props.secretKey())
                .setIdempotencyKey(idempotencyKey)
                .build();
    }

    // 126.00 EGP -> 12600. Uses the currency's own digits (JPY has 0, EGP has 2).
    static long toMinorUnits(BigDecimal amount, String currencyCode) {
        // The currency's default fraction digits are used to determine how many decimal places to move the point.
        // For example, EGP has 2 fraction digits, so 126.00 becomes 12600. JPY has 0 fraction digits, so 126 becomes 126.
        int digits = Currency.getInstance(currencyCode.toUpperCase()).getDefaultFractionDigits();

        // We use movePointRight to shift the decimal point to the right by the number of fraction digits, and then convert to long.
        return amount.movePointRight(digits).longValueExact();
    }

    // ---------- Port 3: verify webhook signature ----------
    @Override
    public PaymentEvent verifyStripeSignature(String payload, String signatureHeader) {
        // The webhook secret is used to verify the signature of the webhook event.
        // It is configured in the application properties and should not be null or blank.
        if (props.webhookSecret() == null || props.webhookSecret().isBlank()) {
            throw new IllegalStateException("stripe.webhook-secret is not configured");
        }
        Event event;
        try {
            // checks HMAC + the 5-minute timestamp tolerance
            // The signatureHeader is the value of the "Stripe-Signature" header from the webhook request.
            event = Webhook.constructEvent(payload, signatureHeader, props.webhookSecret());
        } catch (SignatureVerificationException e) {
            // The signature verification failed, which means the webhook request may not be from Stripe or has been tampered with.
            log.warn("Webhook signature verification failed: {}", e.getMessage());
            throw new InvalidWebhookSignatureException();
        }

        // The event type is used to determine what kind of payment event it is, and we extract the relevant data from the event object.
        return switch (event.getType()) {
            case "checkout.session.completed"    -> getPaymentIntentAndOrderIdFromSession(event);
            case "payment_intent.payment_failed" -> getOrderIdFromPaymentIntent(event);
            case "charge.refunded"               -> getPaymentIntentAndOrderIdFromCharge(event);
            default -> new PaymentEvent(event.getId(), PaymentEventType.UNKNOWN, null, null, null);
        };
    }

    // from session, we can get the payment intent and the order id from the metadata.
    // The payment intent is used to identify the payment in Stripe, and the order id is used to identify the order in our system.
    private PaymentEvent getPaymentIntentAndOrderIdFromSession(Event event) {
        // The data object of the event is a Session object, which contains the payment status, payment intent, and metadata.
        Session s = (Session) dataObject(event);
        // "paid" for cards. Delayed methods (bank debit) complete the session first and pay later.
        PaymentEventType type = "paid".equals(s.getPaymentStatus())
                ? PaymentEventType.CHECKOUT_COMPLETED : PaymentEventType.UNKNOWN;
        return new PaymentEvent(event.getId(), type, s.getId(), s.getPaymentIntent(), orderId(s.getMetadata()));
    }

    // from payment intent, we can get the order id from the metadata.
    // The payment intent is used to identify the payment in Stripe, and the order id is used to identify the order in our system.
    private PaymentEvent getOrderIdFromPaymentIntent(Event event) {
        PaymentIntent pi = (PaymentIntent) dataObject(event);
        return new PaymentEvent(event.getId(), PaymentEventType.PAYMENT_FAILED, null, pi.getId(), orderId(pi.getMetadata()));
    }

    // from charge, we can get the payment intent and the order id from the metadata.
    private PaymentEvent getPaymentIntentAndOrderIdFromCharge(Event event) {
        Charge c = (Charge) dataObject(event);
        // charge.refunded also fires for PARTIAL refunds. We only handle full refunds for now.
        PaymentEventType type = Boolean.TRUE.equals(c.getRefunded())
                ? PaymentEventType.REFUNDED : PaymentEventType.UNKNOWN;
        return new PaymentEvent(event.getId(), type, null, c.getPaymentIntent(), orderId(c.getMetadata()));
    }

    // The data object of the event is deserialized from the event's data object deserializer.
    private StripeObject dataObject(Event event) {
        EventDataObjectDeserializer d = event.getDataObjectDeserializer();
        // getObject() is present ONLY when the event's API version equals the SDK's API version.
        return d.getObject().orElseGet(() -> deserializeLeniently(d));
    }

    // If the event's API version is different from the SDK's API version,
    // we need to deserialize the data object leniently, which may lose some fields.
    private StripeObject deserializeLeniently(EventDataObjectDeserializer d) {
        try {
            return d.deserializeUnsafe();
        } catch (EventDataObjectDeserializationException e) {
            throw new PaymentGatewayException(e);
        }
    }

    private static Integer orderId(Map<String, String> metadata) {
        String value = metadata == null ? null : metadata.get(Constants.ORDER_ID);
        try {
            return value == null ? null : Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}