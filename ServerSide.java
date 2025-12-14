import java.io.*;
import java.net.*;
import java.util.*;

public class ServerSide {
    private static Set<ClientConnection> clients = new HashSet<>();
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress());
                ClientConnection client = new ClientConnection(socket);
                clients.add(client);
                new Thread(client).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message, ClientConnection sender) {
        for (ClientConnection client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public static void removeClient(ClientConnection client) {
        clients.remove(client);
    }
}

class ClientConnection implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String name;

    public ClientConnection(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Enter your name: ");
            name = in.readLine();
            System.out.println(name + " joined the chat!");
            ServerSide.broadcast(name + " has joined the chat!", this);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(name + ": " + message);
                ServerSide.broadcast(name + ": " + message, this);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
            ServerSide.removeClient(this);
            ServerSide.broadcast(name + " has left the chat.", this);
            System.out.println(name + " disconnected.");
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}


