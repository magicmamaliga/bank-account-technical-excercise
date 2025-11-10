package com.mate.jpmc.producer;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.stream.IntStream;


/**
 * Unoptimised calls: Processed 1000000 messages in 156.44 sec (6392.42 msg/sec)
 * Cached AccountId for lookup: Processed 1000000 messages in 104.87 sec (9535.74 msg/sec)
 */
public class TcpLoadTest {

    public static void main(String[] args) throws Exception {
        int threads = 100;     // number of concurrent clients
        int messages = 10000;  // messages per client
        String jsonTemplate = "{\"transactionId\":\"t%d\",\"accountId\":\"ABC1234\",\"transactionType\":\"CREDIT\",\"amount\":1000}\r\n";

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        long start = System.nanoTime();

        IntStream.range(0, threads).forEach(i -> executor.submit(() -> {
            try (Socket socket = new Socket("localhost", 9090);
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                for (int j = 0; j < messages; j++) {
                    out.write(String.format(jsonTemplate, i * messages + j));
                    out.flush();
                    in.readLine(); // read server response ("OK")
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);

        long durationNs = System.nanoTime() - start;
        double seconds = durationNs / 1_000_000_000.0;
        double totalMessages = threads * messages;
        System.out.printf("Processed %.0f messages in %.2f sec (%.2f msg/sec)%n",
                totalMessages, seconds, totalMessages / seconds);
    }
}
