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
     * Специальная "обёртка" для ArrayList, которая обеспечивает доступ к массиву из разных потоков
     */
    private List<Connection> connections = Collections.synchronizedList(new ArrayList<Connection>());
    private  List<String> history = Collections.synchronizedList(new ArrayList<String>());
    private ServerSocket serverSocket;

    /**
     * Конструктор создаёт сервер. Затем для каждого подключения создаётся объект Connection и добавляет его в список подключений.
     */
    public Server(int port)
    {
        try
        {
            serverSocket = new ServerSocket(port);
            System.out.println(InetAddress.getLocalHost());

            while (true) {
                Socket socket = serverSocket.accept();

                // Создаём объект Connection и добавляем его в список
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
     * Закрывает все потоки всех соединений а также серверный сокет
     */
    private void closeAll()
    {
        try
        {
            serverSocket.close();

            // Перебор всех Connection и вызов метода close() для каждого. Блок
            // synchronized {} необходим для правильного доступа к одним данным
            // их разных нитей
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
         * Инициализирует поля объекта и получает имя пользователя
         * @param socket сокет, полученный из server.accept()
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
         * Запрашивает имя пользователя и ожидает от него сообщений. При
         * получении каждого сообщения, оно вместе с именем пользователя
         * пересылается всем остальным.
         */
        @Override
        public void run()
        {
            try
            {
                name = in.readLine();
                // Отправляем всем клиентам сообщение о том, что зашёл новый пользователь
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

                    // Отправляем всем клиентам очередное сообщение
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
         * Закрывает входной и выходной потоки и сокет
         */
        public void close()
        {
            try
            {
                in.close();
                out.close();
                socket.close();

                // Если больше не осталось соединений, закрываем всё, что есть и
                // завершаем работу сервера
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
