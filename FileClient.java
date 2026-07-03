import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class FileClient {
    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9001;
        System.out.println(read(host, port));
    }

    public static String read(String host, int port) throws IOException {
        try (Socket socket = new Socket(host, port);
             OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
             ByteArrayOutputStream response = new ByteArrayOutputStream()) {
            writer.write("READ\n");
            writer.flush();
            socket.shutdownOutput();

            byte[] buffer = new byte[4096];
            int read;
            while ((read = socket.getInputStream().read(buffer)) != -1) {
                response.write(buffer, 0, read);
            }
            return response.toString(StandardCharsets.UTF_8.name());
        }
    }
}
