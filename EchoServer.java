import java.io.*;
import java.net.*;

class ClientHandler extends Thread {
    private final Socket socket;

    ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
            System.out.println("Client connected: " + clientInfo);

            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("[" + clientInfo + "] says: " + msg);
                out.println("Echo: " + msg);
            }

            System.out.println("Client disconnected: " + clientInfo);
        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }
}

public class EchoServer {
    public static void main(String[] args) {
        final int PORT = 5000;
        System.out.println("Server started. Waiting for clients...");

        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = server.accept();
                new ClientHandler(socket).start(); // Handle each client in new thread
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}
