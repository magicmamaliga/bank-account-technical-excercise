package com.mate.jpmc.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.Executors;


@Component
class Producer {

    private static final Logger LOG = LoggerFactory.getLogger(Producer.class);

    private final MessageChannel toTcp;

    Producer(MessageChannel toTcp) {
        this.toTcp = toTcp;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        LOG.info("Starting Producer");
        var exec = Executors.newFixedThreadPool(2);
        exec.submit(() -> loop(true));  // credits
        exec.submit(() -> loop(false)); // debits
    }

    private void loop(boolean credit) {
        var rnd = new Random();
        while (true) {
            try {
                double amount = 200 + rnd.nextInt(500_000 - 200 + 1);
                if (!credit) amount = -amount;
                var tx = new Tx(amount);
                LOG.info("Sending tx: {}", tx);
                toTcp.send(MessageBuilder.withPayload(tx).build());
                Thread.sleep(40); // ~25/sec per thread
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public record Tx(double amount) {
    }

}
