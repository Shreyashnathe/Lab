import java.io.*;
import java.net.*;
import java.util.*;

class ClientHandler extends Thread {
    Socket socket;
    ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            Scanner in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                String msg = in.nextLine();
                System.out.println("Client: " + msg);
                out.println("Echo: " + msg);
            }

            in.close();
            socket.close();
            System.out.println("Client disconnected.");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

public class EchoServer {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(5000);
        System.out.println("Server started. Waiting for clients...");

        while (true) {
            Socket socket = server.accept();
            System.out.println("New client connected.");
            new ClientHandler(socket).start();
        }
    }
}
