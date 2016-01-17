package Client;
import java.io.File;
import java.util.Scanner;

/**
 * Created by botal on 14.01.2016.
 */
public class MainClient {
    public static void main(String[] args) {

        try
        {
            Scanner sc = new Scanner(new File("configClient"));
            String ip = sc.next();
            Integer port = sc.nextInt();

            new Client(ip, port);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
