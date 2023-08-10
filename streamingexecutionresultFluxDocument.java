import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Random;

public class SocketPoller {

    public static void main(String[] args) {
        Sinks.Many<String> sink1 = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.Many<String> sink2 = Sinks.many().unicast().onBackpressureBuffer();

        // Thread 1 polling socket
        new Thread(() -> {
            while (true) {
                String data = pollDataFromSocket1(); // Mocked method
                sink1.tryEmitNext(data);
            }
        }).start();

        // Thread 2 polling socket
        new Thread(() -> {
            while (true) {
                String data = pollDataFromSocket2(); // Mocked method
                sink2.tryEmitNext(data);
            }
        }).start();

        Flux<String> mergedFlux = Flux.merge(sink1.asFlux(), sink2.asFlux());

        mergedFlux.subscribe(data -> System.out.println("Received: " + data));
    }

    // Mock methods for demonstration
    private static String pollDataFromSocket1() {
        try {
            Thread.sleep(new Random().nextInt(1000)); // Random delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Data1";
    }

    private static String pollDataFromSocket2() {
        try {
            Thread.sleep(new Random().nextInt(1000)); // Random delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Data2";
    }
}