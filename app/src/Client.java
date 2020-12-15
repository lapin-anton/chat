import java.io.IOException;
import java.net.Socket;

public class Client {

    protected Connection connection;
    private volatile boolean clientConnected = false;


    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this) {
                this.wait();
                notify();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Ошибка подключения клиента");
        }
        if(clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено.\n" +
                    "Для выхода наберите команду 'exit'.");
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }

        while (clientConnected){
            String line = null;
            try {
                line = ConsoleHelper.readString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (shouldSendTextFromConsole()) sendTextMessage(line);
            if (line.equals("exit")) break;
        }
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

        protected void clientHandshake() throws IOException, ClassNotFoundException {
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

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
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

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(String.format("%s присоединился к чату.", userName));
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(String.format("%s покинул чат.", userName));
        }
    }

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Введите ip сервера:");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Введите порт сервера:");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Введите Ваш никнейм:");
        return ConsoleHelper.readString();
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    private boolean shouldSendTextFromConsole() {
        return true;
    }

    protected void sendTextMessage(String text) {
        try {
            Message message = new Message(MessageType.TEXT, text);
            connection.send(message);
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Сообщение не отправлено");
            clientConnected = false;
        }
    }
}
