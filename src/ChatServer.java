import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 10;
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor iniciado. Esperando conexiones...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                if (clients.size() < MAX_CLIENTS) {
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clients.add(clientHandler);
                    clientHandler.start();
                } else {
                    System.out.println("Máximo número de clientes alcanzado. Rechazando conexión.");
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket clientSocket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                inputStream = clientSocket.getInputStream();
                outputStream = clientSocket.getOutputStream();

                byte[] buffer = new byte[1024];
                int bytesRead;

                String nickname = readMessage(buffer);
                System.out.println("Nuevo cliente conectado (" + nickname + "). Actualmente hay " + clients.size() + " usuarios conectados.");
                broadcastMessage(nickname + " acaba de conectarse a este chat.");

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    String message = new String(buffer, 0, bytesRead);
                    System.out.println(nickname + ": " + message);
                    broadcastMessage(nickname + ": " + message);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                    clients.remove(this);
                    System.out.println("Cliente desconectado. Actualmente hay " + clients.size() + " usuarios conectados.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String readMessage(byte[] buffer) throws IOException {
            int bytesRead = inputStream.read(buffer);
            return new String(buffer, 0, bytesRead);
        }

        private void broadcastMessage(String message) throws IOException {
            for (ClientHandler client : clients) {
                client.outputStream.write(message.getBytes());
            }
        }
    }
}

/*
// Project: PSP_ChatClient
// Path: src/ChatClient.java

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_ADDRESS = "10.0.9.5";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            Scanner scanner = new Scanner(System.in);
            System.out.print("Ingrese su nickname: ");
            String nickname = scanner.nextLine();
            outputStream.write(nickname.getBytes());

            new Thread(() -> {
                byte[] buffer = new byte[1024];
                int bytesRead;
                try {
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        String message = new String(buffer, 0, bytesRead);
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            while (true) {
                String message = scanner.nextLine();
                outputStream.write(message.getBytes());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

*/
