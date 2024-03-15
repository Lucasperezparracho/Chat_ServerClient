import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    // Puerto en el que escucha el servidor
    private static final int PORT = 12345;
    // Número máximo de clientes que el servidor puede aceptar
    private static final int MAX_CLIENTS = 10;
    // Lista que almacenará a los manejadores de clientes conectados
    private static List<ClientHandler> clients = new ArrayList<>();

    // Método principal del servidor
    public static void main(String[] args) {
        try {
            // Se crea un socket de servidor que escucha en el puerto especificado
            ServerSocket serverSocket = new ServerSocket(PORT);
            // Se muestra un mensaje indicando que el servidor se ha iniciado correctamente
            System.out.println("Servidor iniciado. Esperando conexiones...");

            // Bucle principal del servidor para aceptar conexiones entrantes
            while (true) {
                // Se acepta la conexión de un cliente
                Socket clientSocket = serverSocket.accept();
                // Se verifica si se ha alcanzado el máximo número de clientes
                if (clients.size() < MAX_CLIENTS) {
                    // Se crea un manejador de cliente para manejar la conexión con el nuevo cliente
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    // Se añade el manejador de cliente a la lista de clientes conectados
                    clients.add(clientHandler);
                    // Se inicia el hilo del manejador de cliente
                    clientHandler.start();
                } else {
                    // Si se ha alcanzado el máximo número de clientes, se rechaza la conexión del nuevo cliente
                    System.out.println("Máximo número de clientes alcanzado. Rechazando conexión.");
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            // Se imprime la traza de la excepción en caso de error
            e.printStackTrace();
        }
    }

    // Clase interna que representa el manejador de cada cliente conectado
    static class ClientHandler extends Thread {
        private Socket clientSocket;
        private InputStream inputStream;
        private OutputStream outputStream;

        // Constructor del manejador de cliente
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        // Método que se ejecuta cuando se inicia el hilo del manejador de cliente
        public void run() {
            try {
                // Se obtienen los flujos de entrada y salida del socket del cliente
                inputStream = clientSocket.getInputStream();
                outputStream = clientSocket.getOutputStream();

                // Se crea un búfer para almacenar los datos recibidos del cliente
                byte[] buffer = new byte[1024];
                int bytesRead;

                // Se lee el mensaje de bienvenida del cliente (su apodo)
                String nickname = readMessage(buffer);
                // Se muestra un mensaje indicando que un nuevo cliente se ha conectado
                System.out.println("Nuevo cliente conectado (" + nickname + "). Actualmente hay " + clients.size() + " usuarios conectados.");
                // Se envía un mensaje de notificación a todos los clientes informando que un nuevo cliente se ha conectado
                broadcastMessage(nickname + " acaba de conectarse a este chat.");

                // Bucle para leer y enviar mensajes entre el servidor y el cliente
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    // Se convierten los bytes recibidos en un String
                    String message = new String(buffer, 0, bytesRead);
                    // Si el cliente envía el comando "/bye", se notifica su salida del chat
                    if (message.equals("/bye")) {
                        broadcastMessage(nickname + " dejó este chat.");
                        break;
                    }
                    // Se muestra en consola el mensaje enviado por el cliente
                    System.out.println(nickname + ": " + message);
                    // Se envía el mensaje a todos los clientes conectados
                    broadcastMessage(nickname + ": " + message);
                }

            } catch (IOException e) {
                // Se imprime la traza de la excepción en caso de error
                e.printStackTrace();
            } finally {
                try {
                    // Se cierra el socket del cliente
                    clientSocket.close();
                    // Se elimina el manejador de cliente de la lista de clientes conectados
                    clients.remove(this);
                    // Se muestra un mensaje indicando que un cliente se ha desconectado
                    System.out.println("Cliente desconectado. Actualmente hay " + clients.size() + " usuarios conectados.");
                } catch (IOException e) {
                    // Se imprime la traza de la excepción en caso de error
                    e.printStackTrace();
                }
            }
        }

        // Método para leer un mensaje del cliente
        private String readMessage(byte[] buffer) throws IOException {
            int bytesRead = inputStream.read(buffer);
            return new String(buffer, 0, bytesRead);
        }

        // Método para enviar un mensaje a todos los clientes conectados
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
    // Dirección IP del servidor
    private static final String SERVER_ADDRESS = "10.0.9.5";
    // Puerto del servidor
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try {
            // Se establece una conexión con el servidor en la dirección y puerto especificados
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            // Se obtienen los flujos de entrada y salida del socket
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            // Se crea un objeto Scanner para leer desde la entrada estándar (teclado)
            Scanner scanner = new Scanner(System.in);
            // Se solicita al usuario que ingrese su nickname
            System.out.print("Ingrese su nickname: ");
            String nickname = scanner.nextLine();
            // Se envía el nickname al servidor como un arreglo de bytes
            outputStream.write(nickname.getBytes());

            // Se crea un hilo para manejar la recepción de mensajes del servidor
            new Thread(() -> {
                // Se crea un búfer para almacenar los bytes recibidos
                byte[] buffer = new byte[1024];
                int bytesRead;
                try {
                    // Se leen los datos del flujo de entrada del socket
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        // Se convierten los bytes recibidos en un String y se imprime en consola
                        String message = new String(buffer, 0, bytesRead);
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    // Se imprime la traza de la excepción en caso de error
                    e.printStackTrace();
                }
            }).start();

            // Bucle principal para enviar mensajes al servidor
            while (true) {
                // Se lee un mensaje desde la entrada estándar y se envía al servidor
                String message = scanner.nextLine();
                outputStream.write(message.getBytes());
            }

        } catch (IOException e) {
            // Se imprime la traza de la excepción en caso de error
            e.printStackTrace();
        }
    }
}


*/
