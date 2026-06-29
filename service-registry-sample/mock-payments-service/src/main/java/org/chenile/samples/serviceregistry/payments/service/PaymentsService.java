package org.chenile.samples.serviceregistry.payments.service;

import org.chenile.samples.serviceregistry.payments.model.PaymentRequest;
import org.chenile.samples.serviceregistry.payments.model.PaymentResponse;

public interface PaymentsService {
    PaymentResponse getPayment(String paymentId);

    PaymentResponse authorizePayment(PaymentRequest paymentRequest);
}
