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

            // ������ ��� �������� ���������
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // ������ ������ ���� ���
            System.out.println("Enter your nickname:");
            out.println(scan.nextLine());

            // ��������� ����� ���� �������� ��������� � �������
            Resender resend = new Resender();
            resend.start();

            // ���� ������������ �� ����� "exit" ���������� �� ������ ��, ���
            // ������� �� �������
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
     * ��������� ������� � �������� ������ � �����
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
     * ����� � ��������� ���� ���������� ��� ��������� �� ������� � �������.
     * �������� ���� �� ����� ������ ����� Stop().
     */
    private class Resender extends Thread
    {
        private boolean stoped;

        // ����� ��� ��������� ������ �����������
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
                    // ��������� ��������� �� ������ ...
                    String str = in.readLine();
                    // ... � �������� ��� � �������
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
