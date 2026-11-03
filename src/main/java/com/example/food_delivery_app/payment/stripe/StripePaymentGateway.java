package com.example.food_delivery_app.payment.stripe;

import com.example.food_delivery_app.common.Constants;
import com.example.food_delivery_app.exceptions.PaymentGatewayException;
import com.example.food_delivery_app.payment.gateway.CheckoutRequest;
import com.example.food_delivery_app.payment.gateway.CheckoutLine;
import com.example.food_delivery_app.payment.gateway.CheckoutSession;
import com.example.food_delivery_app.payment.gateway.CheckoutSessionCreator;
import com.example.food_delivery_app.payment.gateway.PaymentRefund;
import com.example.food_delivery_app.payment.properties.StripeProperties;
import com.stripe.exception.StripeException;
import com.stripe.model.Coupon;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.CouponCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Currency;


// The only class that knows about Stripe's SDK.
// It implements the two ports (interfaces) that the application uses to talk to a payment gateway.
// The rest of the app doesn't know or care that Stripe is being used.
// Adaptor pattern: this class adapts Stripe's SDK to the application's ports (interfaces).
// this class adapts the Stripe SDK to the application's ports (interfaces) for creating checkout sessions and processing refunds.
// It encapsulates the Stripe-specific logic and provides a clean interface for the rest of the application to interact with the payment gateway.
@Component
@RequiredArgsConstructor
public class StripePaymentGateway
        implements CheckoutSessionCreator, PaymentRefund {

    private final StripeProperties props;

    // ---------- Port 1: create a Checkout Session ----------
    @Override
    public CheckoutSession create(CheckoutRequest r) {
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
    public void refund(String paymentReference, BigDecimal amount, String currency, String idempotencyKey) {
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

}