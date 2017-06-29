package client;

import server.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {
    public static void main (String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }

    @Override
    public void run() {
        super.run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() throws IOException {
        int number = (int)(100*Math.random());
        return "date_bot_" + number;
    }

    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (message.contains(": ")) {
                String[] data = message.split(": ");
                String str = data[1];

                SimpleDateFormat dateFormat = null;
                if (data.length == 2) {
                    switch (str) {
                        case ("дата"):
                            dateFormat = new SimpleDateFormat("d.MM.YYYY");
                            break;
                        case ("день"):
                            dateFormat = new SimpleDateFormat("d");
                            break;
                        case ("месяц"):
                            dateFormat = new SimpleDateFormat("MMMM");
                            break;
                        case ("год"):
                            dateFormat = new SimpleDateFormat("YYYY");
                            break;
                        case ("время"):
                            dateFormat = new SimpleDateFormat("H:mm:ss");
                            break;
                        case ("час"):
                            dateFormat = new SimpleDateFormat("H");
                            break;
                        case ("минуты"):
                            dateFormat = new SimpleDateFormat("m");
                            break;
                        case ("секунды"):
                            dateFormat = new SimpleDateFormat("s");
                            break;
                    }
                }
                if (dateFormat != null) {
                    String dateString = dateFormat.format(Calendar.getInstance().getTime());
                    String botAnswer = String.format("Информация для %s: %s", data[0], dateString);
                    sendTextMessage(botAnswer);
                }
            }
        }
    }
}