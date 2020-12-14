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
            return null;
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {

        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {

        }
    }

    public static void sendBroadcastMessage(Message message) {

    }
}
