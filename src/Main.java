import com.xemplar.libs.crypto.server.net.CryptoServer;

/**
 * Created by Rohan on 8/15/2017.
 */
public class Main {
    public static void main(String[] args){
        if(args == null) error();
        if(args.length != 4) error();
        CryptoServer server = new CryptoServer(args[3], args[2], args[0], args[1].toCharArray(), "node_config.prop");
        Thread t = new Thread(server);
        t.start();
    }

    public static void error(){
        System.out.println("Usage:\n" +
                "  java -jar CryptoPayAPI.jar <db_user> <db_pass> <web_url> <coin_address> <node_config>\n" +
                "  \n" +
                "  <db_user>      User account for database specified on server index.php\n" +
                "  <db_pass>      User password for account specifed\n" +
                "  <web_url>      Location of web server's index.php\n" +
                "  <coin_address> Your Cryptocoin Wallet address\n" +
                "  <node_config>  Location of the node_config.prop");
        System.exit(1);
    }
}
