package Server;
import java.io.File;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Created by botal on 14.01.2016.
 */
public class MainServer {
    public static void main(String[] args) {

        try
        {
            Scanner sc = new Scanner(new File("configServer"));
            Integer port = sc.nextInt();

            new Server(port);

            InetAddress inetAddress = InetAddress.getLocalHost();
            System.out.println(inetAddress);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
