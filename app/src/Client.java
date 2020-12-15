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

        }

        private void clientMainLoop() throws IOException, ClassNotFoundException {

        }

        private void notifyConnectionStatusChanged(boolean clientConnected) {

        }
    }

    private String getServerAddress() {
        return null;
    }

    private int getServerPort() {
        return 0;
    }
}
