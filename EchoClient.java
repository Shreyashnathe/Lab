package db_echoServer;
import java.io.*;
import java.net.*;
import java.util.*;

public class EchoClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 5000);
        Scanner sc = new Scanner(System.in);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        System.out.println("Connected to server. Type messages (type 'exit' to quit):");

        while (true) {
            String msg = sc.nextLine();
            if (msg.equalsIgnoreCase("exit")) break;
            out.println(msg);                   
            System.out.println("Server: " + in.readLine());
        }

        sc.close();
        socket.close();
    }
}
