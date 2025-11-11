import java.io.*;
import java.net.*;
import java.util.*;

public class EchoClient {
    public static void main(String[] args) {
        final String SERVER = "localhost";
        final int PORT = 5000;

        try (
            Socket socket = new Socket(SERVER, PORT);
            Scanner sc = new Scanner(System.in);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            System.out.println("Connected to server. Type messages (type 'exit' to quit):");

            while (true) {
                System.out.print("You: ");
                String msg = sc.nextLine();
                if (msg.equalsIgnoreCase("exit")) break;

                out.println(msg);
                System.out.println("Server: " + in.readLine());
            }

            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
