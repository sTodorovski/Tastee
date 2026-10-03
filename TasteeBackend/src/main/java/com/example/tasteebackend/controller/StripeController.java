package com.example.tasteebackend.controller;

import com.example.tasteebackend.model.Order;
import com.example.tasteebackend.model.enums.OrderStatus;
import com.example.tasteebackend.repository.OrderRepository;
import com.example.tasteebackend.services.EmailService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stripe")
@RequiredArgsConstructor
public class StripeController {

    private final OrderRepository orderRepository;
    private final EmailService emailService;

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    @PostMapping("/webhook")
    public String webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        System.out.println("Stripe webhook received");

        try {
            Event event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    endpointSecret
            );

            System.out.println("Event type: " + event.getType());

            if (event.getType().equals("payment_intent.succeeded")) {
                PaymentIntent paymentIntent = (PaymentIntent) event.getData().getObject();

                if (paymentIntent == null) {
                    System.out.println("PaymentIntent object missing");
                    return "Missing object";
                }

                String paymentIntentId = paymentIntent.getId();
                System.out.println("Stripe PaymentIntent ID: " + paymentIntentId);

                Order order = orderRepository.findByPaymentIntentId(paymentIntentId).orElse(null);

                if (order == null) {
                    System.out.println("No order found with PaymentIntent: " + paymentIntentId);
                    return "Order missing";
                }

                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
                emailService.sendOrderReceipt(order);

                System.out.println("Order marked PAID and receipt sent");
            }

            return "OK";
        } catch (Exception e) {
            e.printStackTrace();
            return "Webhook error";
        }
    }
}