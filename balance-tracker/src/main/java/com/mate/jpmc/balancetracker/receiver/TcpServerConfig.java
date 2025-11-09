package com.mate.jpmc.balancetracker.receiver;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mate.jpmc.balancetracker.BalanceTrackerException;
import com.mate.jpmc.balancetracker.balance.BankAccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;

import java.io.IOException;

@Configuration
public class TcpServerConfig {

    @Bean
    IntegrationFlow tcpServerFlow(BankAccountService service, ObjectMapper mapper,
                                  @Value("${tcp.port:9090}") int port) {
        var crlf = new ByteArrayCrLfSerializer();
        return IntegrationFlow
                .from(Tcp.inboundAdapter(Tcp.netServer(port).deserializer(crlf)))
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
                    return null;
                })
                .get();
    }

}
