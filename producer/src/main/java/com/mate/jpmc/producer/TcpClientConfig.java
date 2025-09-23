package com.mate.jpmc.producer;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;
import org.springframework.messaging.MessageChannel;

@Configuration
public class TcpClientConfig {


    @Bean
    public MessageChannel toTcp() { return new DirectChannel(); }

    @Bean
    public IntegrationFlow tcpClientFlow(ObjectMapper mapper) {
        var crlf = new ByteArrayCrLfSerializer();
        return IntegrationFlow
                .from(toTcp())
                .transform(Producer.Tx.class, tx -> {
                    try {
                        return mapper.writeValueAsBytes(tx);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .handle(Tcp.outboundAdapter(Tcp.netClient("localhost", 9090).serializer(crlf)))
                .get();
    }
}

