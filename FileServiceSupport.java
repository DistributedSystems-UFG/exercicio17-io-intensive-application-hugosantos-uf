import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class FileServiceSupport {
    private FileServiceSupport() {
    }

    public static void handleClient(Socket socket, Path file) {
        try (Socket client = socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             OutputStream output = client.getOutputStream()) {
            String command = reader.readLine();
            if ("READ".equals(command)) {
                output.write(Files.readAllBytes(file));
            } else {
                output.write("ERROR: comando invalido\n".getBytes(StandardCharsets.UTF_8));
            }
            output.flush();
        } catch (IOException e) {
            System.err.println("Erro atendendo cliente: " + e.getMessage());
        }
    }

    public static void shutdown(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static ServerSocket openServerSocket(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
        return serverSocket;
    }
}
