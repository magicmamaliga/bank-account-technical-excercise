package com.mate.jpmc.producer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "maxRetries=3"
})
class ProducerIntegrationTest {

    @Autowired
    private Producer producer;

    @Autowired
    private BlockingQueue<Message<?>> messageQueue;

    @Test
    void testProducerSendsMessages() throws Exception {
        // Producer.start() runs automatically on ApplicationReadyEvent,
        // but we can also call generateTransaction indirectly by waiting.
        Message<?> msg = messageQueue.take(); // blocks until message arrives
        assertThat(msg.getPayload()).isInstanceOf(Producer.Transaction.class);

        Producer.Transaction tx = (Producer.Transaction) msg.getPayload();
//        assertThat(tx.id()).isNotNull();

        System.out.println(tx);
        if(tx.transactionType()==TransactionType.CREDIT){
            assertThat(tx.amount()).isBetween(BigDecimal.valueOf(200), BigDecimal.valueOf(500000));
        } else if(tx.transactionType()==TransactionType.DEBIT){
            assertThat(tx.amount()).isBetween(BigDecimal.valueOf(-500000), BigDecimal.valueOf(-200));
        }

    }

    @Configuration
    static class TestConfig {

        @Bean
        Producer producer(MessageChannel toTcp) {
            return new Producer(toTcp, 3);
        }


        @Bean
        BlockingQueue<Message<?>> messageQueue() {
            return new LinkedBlockingQueue<>();
        }

        @Bean
        MessageChannel toTcp(BlockingQueue<Message<?>> queue) {
            DirectChannel channel = new DirectChannel();
            channel.addInterceptor(new ChannelInterceptor() {
                @Override
                public Message<?> preSend(Message<?> message, MessageChannel ch) {
                    queue.add(message);
                    return message;
                }
            });
            return channel;
        }
    }
}
