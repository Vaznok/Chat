package server; /**
 * Created by Виталий on 18.05.2017.
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Виталий on 15.05.2017.
 */
public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<String, Connection>();

    public static void main(String[] args) throws IOException {
        ConsoleHelper.writeMessage("Введите порт для подключения к чату.");
        int port = ConsoleHelper.readInt();

        ServerSocket serverSocket = new ServerSocket(port);
        ConsoleHelper.writeMessage("Сервер запущен!");

        while (true) {
            try{
                Socket socket = serverSocket.accept();
                ConsoleHelper.writeMessage("Пользователь подключился");
                new Handler(socket).start();
            } catch (Exception e) {
                serverSocket.close();
                ConsoleHelper.writeMessage("Ошибка сервера " + e);
                break;
            }
        }
    }



    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> connection : connectionMap.entrySet()) {
            Connection value = connection.getValue();
            try {
                value.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Cообщение не было отправленно " + e);
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            String userName;
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();

                if(message.getType() == MessageType.USER_NAME) {
                    userName = message.getData();
                    if (userName.isEmpty() || connectionMap.containsKey(userName)) {
                        ConsoleHelper.writeMessage("Недопустимое имя пользователя, повторите ввод.");
                    } else {
                        connectionMap.put(userName, connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                        break;
                    }
                }
            }
            return userName;
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> user : connectionMap.entrySet()) {
                String keyName = user.getKey();


                if (!keyName.equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, keyName));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while(true) {
                Message message = connection.receive();
                if(message.getType() == MessageType.TEXT) {
                    String format = userName + ": " + message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, format));
                } else {
                    ConsoleHelper.writeMessage("Ошибка ввода текста");
                }
            }

        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Установлено новое соединение с удаленным адресом " + socket.getRemoteSocketAddress());
            String userName = null;
            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);
                sendListOfUsers(connection, userName);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
            }
            if (userName != null) {
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }
            ConsoleHelper.writeMessage("Cоединение с удаленным адресом закрыто");
        }
    }
}
