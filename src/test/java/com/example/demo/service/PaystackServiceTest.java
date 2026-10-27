package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class PaystackServiceTest {

    @Test
    void parsesSuccessfulTransactionVerificationFromPaystack() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.paystack.co/transaction/verify/paystack-ref-123"))
                .andExpect(header("Authorization", "Bearer test-secret"))
                .andRespond(withSuccess("""
                        {
                          "status": true,
                          "data": {
                            "status": "success",
                            "reference": "paystack-ref-123",
                            "amount": 6100,
                            "currency": "NGN"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        PaystackTransaction transaction = new PaystackService(builder, "test-secret")
                .verifyTransaction("paystack-ref-123");

        assertEquals(new PaystackTransaction("paystack-ref-123", "success", 6100, "NGN"), transaction);
        server.verify();
    }

    @Test
    void rejectsUnsuccessfulPaystackTransaction() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.paystack.co/transaction/verify/paystack-ref-123"))
                .andRespond(withSuccess("""
                        {
                          "status": true,
                          "data": {
                            "status": "failed",
                            "reference": "paystack-ref-123",
                            "amount": 6100,
                            "currency": "NGN"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThrows(IllegalArgumentException.class,
                () -> new PaystackService(builder, "test-secret").verifyTransaction("paystack-ref-123"));
        server.verify();
    }
}
