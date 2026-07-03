import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolFileServer {
    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9003;
        int poolSize = args.length > 1 ? Integer.parseInt(args[1]) : Runtime.getRuntime().availableProcessors();
        Path file = args.length > 2 ? Paths.get(args[2]) : Paths.get("data.txt");
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);

        try (ServerSocket serverSocket = FileServiceSupport.openServerSocket(port)) {
            System.out.println("Thread-pool file server listening on port " + port);
            while (true) {
                Socket client = serverSocket.accept();
                pool.execute(() -> FileServiceSupport.handleClient(client, file));
            }
        } finally {
            FileServiceSupport.shutdown(pool);
        }
    }
}
