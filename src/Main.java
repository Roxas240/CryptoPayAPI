import com.xemplar.libs.cryptorpc.client.CryptoClient;
import com.xemplar.libs.cryptorpc.client.CryptoClientImpl;
import com.xemplar.libs.cryptorpc.domain.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import sun.reflect.annotation.ExceptionProxy;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Created by Rohan on 8/15/2017.
 */
public class Main {
    public static final int MIN_CONFIRMS = 3;

    public static void main(String[] args) throws Exception{
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        CloseableHttpClient httpProvider = HttpClients.custom().setConnectionManager(cm).build();
        Properties nodeConfig = new Properties();
        InputStream is = new BufferedInputStream(new FileInputStream("node_config.prop"));
        nodeConfig.load(is);
        is.close();

        final CryptoClient client = new CryptoClientImpl(httpProvider, nodeConfig);
        final String paymentID = randomString(16);
        final BigDecimal price = new BigDecimal("0.01");
        final int startBlock = 	getBlockCount(client);
        System.out.println(paymentID);

        Thread t = new Thread(new Runnable() {
            public void run(){
                int lastBlock = startBlock - 1, currentBlock = startBlock;
                BigDecimal bal = new BigDecimal("0.0");
                boolean paid = false;

                while(!paid){
                    if(currentBlock != lastBlock) {
                        BigDecimal d = getPayTX(client, paymentID, startBlock, currentBlock);
                        if(d != null){
                            bal = bal.add(d);
                        }
                        if(bal.compareTo(price) >= 0){
                            paid = true;
                        }
                    }
                    lastBlock = currentBlock;
                    currentBlock = getBlockCount(client);
                    sleep(1000);
                    System.out.println(currentBlock + ", Balance: " + bal);
                }
            }
        });
        t.start();
    }

    public static BigDecimal getPayTX(CryptoClient cli, String payID, int start, int stop){
        BigDecimal ret = new BigDecimal("0.0");
        try{
            for(int i = start; i <= stop; i++){
                Block current = getBlockAt(cli, i);
                if(current.getConfirmations() < MIN_CONFIRMS){
                    continue;
                }
                List<Transaction> transactions = current.getTx();
                for(Transaction tx : transactions){
                    Transaction p = cli.getTransaction(tx.getTxId());
                    String note = p.getComment();
                    if(payID.equals(note)){
                        ret = ret.add(p.getAmount());
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return ret;
    }

    public static int getBlockCount(CryptoClient cli){
        try {
            return cli.getBlockCount();
        } catch(Exception e){
            e.printStackTrace();
        }

        return -1;
    }

    public static Block getBlockAt(CryptoClient cli, int height){
        try {
            return (Block)cli.getBlock(cli.getBlockHash(height), true);
        } catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static String randomString(int length){
        final String master = "!@#$%^&*()1234567890qwertyuiopasdfghjklzxcvbnm,./;'[]QWERTYUIOP{}ASDFGHJKL:ZXCVBNM<>?";
        Random r = new Random();
        String ret = "";
        for(int i = 0; i < length; i++){
            ret += master.charAt(r.nextInt(master.length()));
        }

        return ret;
    }

    public static Payment getLastPayment(CryptoClient cli){
        try{
            return cli.listTransactions("*", 1).get(0);
        } catch(Exception e){
            return null;
        }
    }

    public static void sleep(long mills){
        try{
            Thread.sleep(mills);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
