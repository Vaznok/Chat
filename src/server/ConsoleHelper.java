package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static String readString()  {
        String message;
        try {
            message = reader.readLine();
        } catch (IOException e) {
            System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            return readString();
        }
        return message;
    }

    public static int readInt() {
        int number;
        try {
            number = Integer.parseInt(readString());
        } catch (NumberFormatException e) {
            System.out.println("Произошла ошибка при попытке ввода чисел. Попробуйте еще раз.");
            return readInt();
        }
        return number;
    }

    public static void writeMessage(String message) {
        System.out.println(message);
    }
}
