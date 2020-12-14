import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int port = ConsoleHelper.readInt();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен.");
            while (true) {
                Socket s = serverSocket.accept();
                Handler handler = new Handler(s);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String clientName = null;
            ConsoleHelper.writeMessage(String.format("Установлено соединение с %s",
                    socket.getRemoteSocketAddress().toString()));
            try (Connection connection = new Connection(socket)){
                clientName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, clientName));
                notifyUsers(connection, clientName);
                serverMainLoop(connection, clientName);
            } catch (Exception e) {
                ConsoleHelper.writeMessage(String.format("Ошибка обмена данными с удаленным адресом %s",
                        socket.getRemoteSocketAddress()));
            } finally {
                if(clientName != null) {
                    connectionMap.remove(clientName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, clientName));
                }
                ConsoleHelper.writeMessage(String.format("Соединение с удаленным адресом %s закрыто",
                        socket.getRemoteSocketAddress().toString()));
            }
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            Message m;
            do {
                connection.send(new Message(MessageType.NAME_REQUEST));
                m = connection.receive();
            } while (!m.getType().equals(MessageType.USER_NAME) ||
                    m.getData().equals("") || connectionMap.containsKey(m.getData()));
            connectionMap.put(m.getData(), connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED));
            return m.getData();
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for(Map.Entry<String, Connection> pair: connectionMap.entrySet()) {
                if (pair.getKey().equals(userName)) continue;
                Message message = new Message(MessageType.USER_ADDED, pair.getKey());
                connection.send(message);
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                String formatted = null;
                Message message = connection.receive();
                if(message.getType() == MessageType.TEXT) {
                    formatted = String.format("%s: %s", userName, message.getData());
                    message = new Message(MessageType.TEXT, formatted);
                    sendBroadcastMessage(message);
                } else {
                    ConsoleHelper.writeMessage("Ошибка отправки сообщения");
                }
            }
        }
    }

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> pair: connectionMap.entrySet()) {
            Connection c = pair.getValue();
            try {
                c.send(message);
            } catch (IOException e) {
                System.out.println("Сообщение не отправлено.");
            }
        }
    }
}
