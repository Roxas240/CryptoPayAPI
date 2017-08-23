import com.xemplar.libs.crypto.server.net.CryptoServer;

/**
 * Created by Rohan on 8/15/2017.
 */
public class Main {
    public static final String address = "D9btaj1e87UyDsyjqe4bs6Jr5EMWqrFHhb";
    public static final String user = "pay_wallet";
    public static final char[] pass = "pX6RNOOWkFfy5BX3".toCharArray();

    public static void main(String[] args){
        CryptoServer server = new CryptoServer(address, "http://localhost/index.php", user, pass, "node_config.prop");
        Thread t = new Thread(server);
        t.start();
    }
}
