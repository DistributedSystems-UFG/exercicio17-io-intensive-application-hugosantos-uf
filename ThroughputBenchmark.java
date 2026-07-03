import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThroughputBenchmark {
    private static final int REQUESTS = 1200;
    private static final int CLIENT_THREADS = 24;
    private static final Path FILE = Paths.get("data.txt");

    public static void main(String[] args) throws Exception {
        runBenchmark("single-threaded", 9101, ThroughputBenchmark::runSingleThreadedServer);
        runBenchmark("thread-per-request", 9102, ThroughputBenchmark::runThreadPerRequestServer);
        runBenchmark("thread-pool", 9103, ThroughputBenchmark::runThreadPoolServer);
    }

    private static void runBenchmark(String name, int port, ServerRunner runner) throws Exception {
        ServerSocket serverSocket = FileServiceSupport.openServerSocket(port);
        Thread serverThread = new Thread(() -> runner.run(serverSocket), name + "-server");
        serverThread.setDaemon(true);
        serverThread.start();
        Thread.sleep(200);

        long start = System.nanoTime();
        runClients(port);
        long elapsed = System.nanoTime() - start;
        double seconds = elapsed / 1_000_000_000.0;
        double throughput = REQUESTS / seconds;

        serverSocket.close();
        serverThread.join(1000);

        System.out.printf("%s: %.2f req/s (%d requests in %.3f s)%n", name, throughput, REQUESTS, seconds);
    }

    private static void runClients(int port) throws InterruptedException {
        ExecutorService clients = Executors.newFixedThreadPool(CLIENT_THREADS);
        CountDownLatch done = new CountDownLatch(REQUESTS);
        List<RuntimeException> failures = new ArrayList<>();

        for (int i = 0; i < REQUESTS; i++) {
            clients.execute(() -> {
                try {
                    String response = FileClient.read("localhost", port);
                    if (!response.contains("linha 1")) {
                        throw new IllegalStateException("resposta invalida");
                    }
                } catch (IOException e) {
                    synchronized (failures) {
                        failures.add(new RuntimeException(e));
                    }
                } finally {
                    done.countDown();
                }
            });
        }

        done.await();
        clients.shutdown();
        if (!failures.isEmpty()) {
            throw failures.get(0);
        }
    }

    private static void runSingleThreadedServer(ServerSocket serverSocket) {
        while (!serverSocket.isClosed()) {
            try {
                FileServiceSupport.handleClient(serverSocket.accept(), FILE);
            } catch (IOException e) {
                return;
            }
        }
    }

    private static void runThreadPerRequestServer(ServerSocket serverSocket) {
        while (!serverSocket.isClosed()) {
            try {
                Socket client = serverSocket.accept();
                new Thread(() -> FileServiceSupport.handleClient(client, FILE)).start();
            } catch (IOException e) {
                return;
            }
        }
    }

    private static void runThreadPoolServer(ServerSocket serverSocket) {
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            while (!serverSocket.isClosed()) {
                try {
                    Socket client = serverSocket.accept();
                    pool.execute(() -> FileServiceSupport.handleClient(client, FILE));
                } catch (IOException e) {
                    return;
                }
            }
        } finally {
            FileServiceSupport.shutdown(pool);
        }
    }

    private interface ServerRunner {
        void run(ServerSocket serverSocket);
    }
}
