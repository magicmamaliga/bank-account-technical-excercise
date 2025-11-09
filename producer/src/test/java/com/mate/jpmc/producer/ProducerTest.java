package com.mate.jpmc.producer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.math.BigDecimal;

import static com.mate.jpmc.producer.TransactionType.CREDIT;
import static com.mate.jpmc.producer.TransactionType.DEBIT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProducerTest {

    private MessageChannel messageChannel;
    private Producer producer;

    @BeforeEach
    void setUp() {
        messageChannel = mock(MessageChannel.class);
        producer = new Producer(messageChannel, 3);
    }
////
////    @Test
////    void testCreditTransactionIsPositive() throws Exception {
////        Producer.Transaction tx = new Producer.Transaction("id", CREDIT, BigDecimal.valueOf(500));
////        assertTrue(tx.amount().compareTo(BigDecimal.ZERO) > 0);
////    }
////
////    @Test
////    void testDebitTransactionIsNegative() throws Exception {
////        Producer.Transaction tx = new Producer.Transaction("id", DEBIT, BigDecimal.valueOf(-500));
////        assertTrue(tx.amount().compareTo(BigDecimal.ZERO) < 0);
////    }
////
////    @Test
////    void testSendWithRetrySuccessFirstTry() throws Exception {
////        when(messageChannel.send(any(Message.class))).thenReturn(true);
////
////        Producer.Transaction tx = new Producer.Transaction("id", CREDIT, BigDecimal.valueOf(1000));
////        producer.sendWithRetry(tx);
////
////        verify(messageChannel, times(1)).send(any(Message.class));
////    }
////
////    @Test
////    void testSendWithRetryRetriesAndSucceeds() throws Exception {
////        when(messageChannel.send(any(Message.class)))
////                .thenThrow(new RuntimeException("fail"))
////                .thenReturn(true);
////
////        Producer.Transaction tx = new Producer.Transaction("id", CREDIT, BigDecimal.valueOf(1000));
////        producer.sendWithRetry(tx);
////
////        verify(messageChannel, times(2)).send(any(Message.class));
////    }
////
////    @Test
////    void testSendWithRetryFailsAfterMaxRetries() {
////        when(messageChannel.send(any(Message.class)))
////                .thenThrow(new RuntimeException("always fail"));
////
////        Producer.Transaction tx = new Producer.Transaction("id", CREDIT, BigDecimal.valueOf(1000));
////
////        assertThrows(RuntimeException.class, () -> producer.sendWithRetry(tx));
////
////        verify(messageChannel, times(3)).send(any(Message.class));
//    }
//
//    @Test
//    void testStartSubmitsTwoThreads() {
//        // Just check it doesn't throw and logs start
//        assertDoesNotThrow(() -> producer.start());
//    }
}