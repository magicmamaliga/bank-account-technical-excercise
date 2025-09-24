package com.mate.jpmc.balancetracker;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;

import java.io.IOException;

@Configuration
public class TcpServerConfig {

    @Bean
    IntegrationFlow tcpServerFlow(BankAccountService service, ObjectMapper mapper) {
        var crlf = new ByteArrayCrLfSerializer(); // line-delimited frames
        return IntegrationFlow
                .from(Tcp.inboundAdapter(Tcp.netServer(9090).deserializer(crlf)))
                .transform(byte[].class, bytes -> {
                    try {
                        return mapper.readValue(bytes, Transaction.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .handle(Transaction.class, (transaction, headers) -> {
                    service.processTransaction(new Transaction(transaction.id(), transaction.transactionType(), transaction.amount()));
                    return null; // one-way
                })
                .get();
    }

}
