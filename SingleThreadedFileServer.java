import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SingleThreadedFileServer {
    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9001;
        Path file = args.length > 1 ? Paths.get(args[1]) : Paths.get("data.txt");

        try (ServerSocket serverSocket = FileServiceSupport.openServerSocket(port)) {
            System.out.println("Single-threaded file server listening on port " + port);
            while (true) {
                FileServiceSupport.handleClient(serverSocket.accept(), file);
            }
        }
    }
}
