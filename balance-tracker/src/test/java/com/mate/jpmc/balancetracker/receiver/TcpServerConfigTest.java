package com.mate.jpmc.balancetracker.receiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mate.jpmc.balancetracker.balance.BankAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(
        classes = {TcpServerConfig.class, TcpServerConfigTest.TestBeans.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class, // keep infra out of this test
        KafkaAutoConfiguration.class
})
class TcpServerConfigTest {

    private static int port;
    @MockitoBean
    private BankAccountService bankAccountService;
    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) throws Exception {
        // choose an ephemeral free port and inject it as "tcp.port"
        try (ServerSocket ss = new ServerSocket(0)) {
            port = ss.getLocalPort();
        }
        registry.add("tcp.port", () -> Integer.toString(port));
    }

    @BeforeEach
    void resetMocks() {
        reset(bankAccountService);
    }

    @Test
    void tcpServer_receivesJsonLine_crlfFramed_and_callsService() throws Exception {
        var tx = new TransactionDTO("abc-123", TransactionType.CREDIT, new BigDecimal("1234.56"));
        byte[] json = objectMapper.writeValueAsBytes(tx);

        try (Socket socket = new Socket("127.0.0.1", port)) {
            OutputStream out = socket.getOutputStream();
            out.write(json);
            out.write('\r'); // CR
            out.write('\n'); // LF
            out.flush();

            ArgumentCaptor<TransactionDTO> captor = ArgumentCaptor.forClass(TransactionDTO.class);
            verify(bankAccountService, timeout(1500)).processTransaction(captor.capture());

            var received = captor.getValue();
            assertThat(received.transactionId()).isEqualTo("abc-123");
            assertThat(received.transactionType()).isEqualTo(TransactionType.CREDIT);
            assertThat(received.amount()).isEqualByComparingTo("1234.56");
        }
    }

    @Test
    void tcpServer_invalidJson_line_causesError_and_nothingIsDispatched() throws Exception {
        try (Socket socket = new Socket("127.0.0.1", port)) {
            socket.getOutputStream().write("not-json\r\n".getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();

            verify(bankAccountService, after(400).never()).processTransaction(any());
        }
    }

    /**
     * Provide an ObjectMapper for the transform step.
     */
    static class TestBeans {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
