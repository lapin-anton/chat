import java.io.IOException;
import java.net.Socket;

public class Client {

    private Connection connection;
    private volatile boolean clientConnected = false;


    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public void run() {
        SocketThread socketThread = new SocketThread();
    }

    public class SocketThread extends Thread {

        @Override
        public void run() {
            String host = getServerAddress();
            int port = getServerPort();
            try(Socket socket = new Socket(host, port)) {
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }

        private void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if(message.getType() == null) throw new IOException("Unexpected MessageType");
                if(message.getType().equals(MessageType.NAME_REQUEST)) {
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                } else if (message.getType().equals(MessageType.NAME_ACCEPTED)) {
                    notifyConnectionStatusChanged(true);
                    break;
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        private void clientMainLoop() throws IOException, ClassNotFoundException {
            while(true) {
                Message message = connection.receive();
                if(message.getType() == null) {
                    throw new IOException("Unexpected MessageType");
                }
                if(message.getType().equals(MessageType.TEXT)) {
                    processIncomingMessage(message.getData());
                } else if (message.getType().equals(MessageType.USER_ADDED)) {
                    informAboutAddingNewUser(message.getData());
                } else if (message.getType().equals(MessageType.USER_REMOVED)) {
                    informAboutDeletingNewUser(message.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        private void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        private void processIncomingMessage(String message) {

        }

        private void informAboutAddingNewUser(String userName) {

        }

        private void informAboutDeletingNewUser(String userName) {

        }
    }

    private String getServerAddress() {
        return null;
    }

    private int getServerPort() {
        return 0;
    }

    private String getUserName() {
        return null;
    }
}
