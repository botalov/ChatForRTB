package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by botal on 14.01.2016.
 */
public class Client {

    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;


    public Client(String IP, int port) {

        Scanner scan = new Scanner(System.in);

        try {
            socket = new Socket(IP, port);

            // Потоки для передачи сообщений
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Просим ввести свой ник
            System.out.println("Enter your nickname:");
            out.println(scan.nextLine());

            // Запускаем вывод всех входящих сообщений в консоль
            Resender resend = new Resender();
            resend.start();

            // Пока пользователь не введёт "exit" отправляем на сервер всё, что
            // введено из консоли
            String str = "";
            while (!str.equals("exit"))
            {
                str = scan.nextLine();
                out.println(str);
            }
            resend.Stop();
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            close();
        }
    }

    /**
     * Закрывает входной и выходной потоки и сокет
     */
    private void close() {
        try
        {
            in.close();
            out.close();
            socket.close();
        } catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Threads were not closed!");
        }
    }

    /**
     * Класс в отдельной нити пересылает все сообщения от сервера в консоль.
     * Работает пока не будет вызван метод Stop().
     */
    private class Resender extends Thread
    {
        private boolean stoped;

        // Метод для останавки обмена сообщениями
        public void Stop()
        {
            stoped = true;
        }

        @Override
        public void run() {
            try
            {
                while (!stoped)
                {
                    // Считываем сообщение из потока ...
                    String str = in.readLine();
                    // ... и печатаем его в консоль
                    System.out.println(str);
                }
            }
            catch (IOException e)
            {
                System.err.println("Error getting message.");
                e.printStackTrace();
            }
        }
    }
}
