package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by botal on 14.01.2016.
 */
public class Server {

    /**
     * ����������� "������" ��� ArrayList, ������� ������������ ������ � ������� �� ������ �������
     */
    private List<Connection> connections = Collections.synchronizedList(new ArrayList<Connection>());
    private  List<String> history = Collections.synchronizedList(new ArrayList<String>());
    private ServerSocket serverSocket;

    /**
     * ����������� ������ ������. ����� ��� ������� ����������� �������� ������ Connection � ��������� ��� � ������ �����������.
     */
    public Server(int port)
    {
        try
        {
            serverSocket = new ServerSocket(port);
            System.out.println(InetAddress.getLocalHost());

            while (true) {
                Socket socket = serverSocket.accept();

                // ������ ������ Connection � ��������� ��� � ������
                Connection connection = new Connection(socket);
                connections.add(connection);

                connection.start();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            closeAll();
        }
    }

    /**
     * ��������� ��� ������ ���� ���������� � ����� ��������� �����
     */
    private void closeAll()
    {
        try
        {
            serverSocket.close();

            // ������� ���� Connection � ����� ������ close() ��� �������. ����
            // synchronized {} ��������� ��� ����������� ������� � ����� ������
            // �� ������ �����
            synchronized(connections)
            {
                Iterator<Connection> iter = connections.iterator();
                while(iter.hasNext())
                {
                    ((Connection) iter.next()).close();
                }
            }
        } catch (Exception e)
        {
            System.err.println("Threads were not closed!");
        }
    }


    private class Connection extends Thread {
        private BufferedReader in;
        private PrintWriter out;
        private Socket socket;

        private String name = "";

        /**
         * �������������� ���� ������� � �������� ��� ������������
         * @param socket �����, ���������� �� server.accept()
         */
        public Connection(Socket socket)
        {
            this.socket = socket;

            try
            {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

            } catch (IOException e)
            {
                e.printStackTrace();
                close();
            }
        }

        /**
         * ����������� ��� ������������ � ������� �� ���� ���������. ���
         * ��������� ������� ���������, ��� ������ � ������ ������������
         * ������������ ���� ���������.
         */
        @Override
        public void run()
        {
            try
            {
                name = in.readLine();
                // ���������� ���� �������� ��������� � ���, ��� ����� ����� ������������
                synchronized(connections)
                {
                    Iterator<Connection> iter = connections.iterator();
                    while(iter.hasNext())
                    {
                        ((Connection) iter.next()).out.println(name + " cames now");
                    }
                }

                for (String mess : history)
                {
                    out.println(mess);
                }

                String str = "";
                while (true)
                {
                    str = in.readLine();
                    if(str.equals("exit")) break;

                    String message = name + ": " + str;
                    history.add(message);

                    // ���������� ���� �������� ��������� ���������
                    synchronized(connections)
                    {
                        Iterator<Connection> iter = connections.iterator();
                        while(iter.hasNext()) {
                            ((Connection) iter.next()).out.println(message);
                        }
                    }
                }

                synchronized(connections)
                {
                    Iterator<Connection> iter = connections.iterator();
                    while(iter.hasNext())
                    {
                        ((Connection) iter.next()).out.println(name + " has left");
                    }
                }
            } catch (IOException e)
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
        public void close()
        {
            try
            {
                in.close();
                out.close();
                socket.close();

                // ���� ������ �� �������� ����������, ��������� ��, ��� ���� �
                // ��������� ������ �������
                connections.remove(this);
                if (connections.size() == 0)
                {
                    Server.this.closeAll();
                    System.exit(0);
                }
            } catch (Exception e)
            {
                System.err.println("Threads were not closed!");
            }
        }
    }
}
