package client;

import server.*;

import java.io.IOException;
import java.net.Socket;

public class Client {
    public static void main (String[] args) {
        Client client = new Client();
        client.run();
    }

    protected Connection connection;
    private volatile boolean clientConnected = false;

    protected String getServerAddress() throws IOException {
        ConsoleHelper.writeMessage("Введите IP адресс");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() throws IOException {
        ConsoleHelper.writeMessage("Введите порт");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() throws IOException {
        ConsoleHelper.writeMessage("Впиши свое имя");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Сообщение не было отправленно");
            clientConnected = false;
        }
    }

    public void run() {
        synchronized (this) {
            SocketThread thread = getSocketThread();
            thread.setDaemon(true);
            thread.start();
            try {
                wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Нет ответа от сервера.");
                return;
            }
            if (clientConnected == true) ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду ‘exit’.");
            else ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
            while (clientConnected) {
                String str = ConsoleHelper.readString();
                if (str.equals("exit")) {
                    clientConnected = false;
                    break;
                }
                if(shouldSendTextFromConsole()) sendTextMessage(str);
            }
        }
    }

    public class SocketThread extends Thread {
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage("Пользователь " + userName + " присоединился к чату.");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage("Пользователь " + userName + " покинул чат.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            synchronized (Client.this) {
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                MessageType messageType = message.getType();

                if (messageType == MessageType.NAME_REQUEST) {
                    String userName = getUserName();
                    connection.send(new Message(MessageType.USER_NAME, userName));

                } else if (messageType == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                MessageType messageType = message.getType();
                String messageData = message.getData();

                if (messageType == MessageType.TEXT) {
                    processIncomingMessage(messageData);
                } else if (messageType == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(messageData);
                } else if (messageType == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(messageData);
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        @Override
        public void run() {
            try {

                Socket socket = new Socket(getServerAddress(), getServerPort());
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }
}
