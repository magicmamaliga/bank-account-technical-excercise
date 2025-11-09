package com.mate.jpmc.balancetracker.receiver;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mate.jpmc.balancetracker.BalanceTrackerException;
import com.mate.jpmc.balancetracker.balance.BankAccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;

@Configuration
public class TcpServerConfig {
    @Bean
    public TcpNetServerConnectionFactory serverConnectionFactory(@Value("${tcp.port:9090}") int port) {
        TcpNetServerConnectionFactory factory = new TcpNetServerConnectionFactory(port);
        factory.setDeserializer(new ByteArrayCrLfSerializer());
        factory.setSerializer(new ByteArrayCrLfSerializer());
        factory.setBacklog(200);             // ✅ equivalent to soBacklog
        factory.setSoTimeout(10000);         // optional read timeout
        return factory;
    }

    @Bean
    IntegrationFlow tcpServerFlow(TcpNetServerConnectionFactory connectionFactory,
                                  BankAccountService service,
                                  ObjectMapper mapper,
                                  @Value("${tcp.port:9090}") int port) {
        var crlf = new ByteArrayCrLfSerializer();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(100);
        executor.initialize();
        return IntegrationFlow
                .from(Tcp.inboundGateway(connectionFactory))
                .transform(byte[].class, bytes -> {
                    try {
                        return mapper.readValue(bytes, TransactionDTO.class);
                    } catch (IOException e) {
                        return "error reading transaction bytes";
                    }
                }).handle(TransactionDTO.class, (transaction, headers) -> {
                    try {
                        service.processTransaction(new TransactionDTO(transaction.transactionId(), transaction.accountId(), transaction.transactionType(), transaction.amount()));
                    } catch (BalanceTrackerException e) {
                        return "Error processing transaction " + transaction.transactionId();
                    }
                    return "OK";
                })
                .get();
    }

}
