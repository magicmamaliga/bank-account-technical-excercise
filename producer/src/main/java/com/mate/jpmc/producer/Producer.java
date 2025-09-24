package com.mate.jpmc.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;

import static com.mate.jpmc.producer.TransactionType.CREDIT;
import static com.mate.jpmc.producer.TransactionType.DEBIT;


@Component
public class Producer {

    private static final Logger LOG = LoggerFactory.getLogger(Producer.class);

    private final MessageChannel toTcp;
    private final int maxRetries;

    Producer(MessageChannel toTcp,
             @Value("${maxRetries:100}")
             int maxRetries) {
        this.toTcp = toTcp;
        this.maxRetries = maxRetries;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        LOG.info("Starting Producer");
        var exec = Executors.newFixedThreadPool(2);
        try {
            exec.submit(() -> generateTransaction(CREDIT));
            exec.submit(() -> generateTransaction(DEBIT));
        } finally {
            exec.shutdown();
        }
        LOG.info("Stopping Producer");
    }

    private void generateTransaction(TransactionType transactionType) {
        var rnd = new Random();
        while (true) {
            try {
                BigDecimal amount = BigDecimal.valueOf(200 + rnd.nextDouble(500000 - 200 + 1));
                if (DEBIT == transactionType) amount = amount.negate();
                var tx = new Transaction(UUID.randomUUID().toString(), transactionType, amount);
                sendWithRetry(tx);
                Thread.sleep(1); // ~25/sec per thread
            } catch (Exception e) {
                LOG.info("Interrupted {}", e.getMessage());
                Thread.currentThread().interrupt();
                break;
            }
        }
    }


    void sendWithRetry(Transaction tx) throws InterruptedException {
        int attempt = 0;

        while (true) {
            try {
                toTcp.send(MessageBuilder.withPayload(tx).build());
                return;
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxRetries) {
                    LOG.error("Failed to send tx {} after {} retries: {}", tx, attempt, e.getMessage());
                    throw e; // give up after max retries
                }
                LOG.warn("Error sending tx {}. Retry {}/{}. Retrying in 5s. Error: {}",
                        tx, attempt, maxRetries, e.getMessage());
                Thread.sleep(5000);
            }
        }
    }

    public record Transaction(String id, TransactionType transactionType, BigDecimal amount) {
    }

}
