import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ThreadPerRequestFileServer {
    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9002;
        Path file = args.length > 1 ? Paths.get(args[1]) : Paths.get("data.txt");

        try (ServerSocket serverSocket = FileServiceSupport.openServerSocket(port)) {
            System.out.println("Thread-per-request file server listening on port " + port);
            while (true) {
                Socket client = serverSocket.accept();
                new Thread(() -> FileServiceSupport.handleClient(client, file)).start();
            }
        }
    }
}
