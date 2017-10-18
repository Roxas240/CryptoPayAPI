package com.xemplar.games.cross.cryptominer;

import com.xemplar.libs.crypto.client.CryptoClient;
import com.xemplar.libs.crypto.common.NetworkListener;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rohan on 8/20/2017.
 */
public class CryptoTest extends Application implements MoneyListener, NetworkListener.ClientPaymentListener {
    private static final NumberFormat format = NumberFormat.getCurrencyInstance();
    private Map<String, Resource> resources = new HashMap<>();
    private List<Resource> res = new ArrayList<>();

    private static final String CRYPTO_ADDRESS = "DDjbew9FjBH9zgJyFCk7Bq7fDzzGSTqQf6";

    private CryptoClient cli;
    private PaymentPopup popup;
    private Label totalMoney;
    private long money = 5;

    public static BigDecimal B1_COST = new BigDecimal("0.05"), B2_COST = new BigDecimal("0.01");

    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getLayout("test.fxml"));

        StackPane root = new StackPane();
        root.getChildren().add((Node)loader.load());

        Scene s = new Scene(root, 800, 480);
        primaryStage.setScene(s);
        primaryStage.show();

        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            for (Resource r : res) {
                r.miners = 0;
            }
            System.exit(0);
        });

        char[] encpass = "J7fg9Gfekj5Fjf86".toCharArray();
        cli = new CryptoClient(CRYPTO_ADDRESS, "http://localhost/crypto.php", "pay_manager", "s4BgNvfcmoj0q5bz".toCharArray(), encpass);

        Button opt1 = (Button) s.lookup("#buy_opt1");
        Button opt2 = (Button) s.lookup("#buy_opt2");

        opt1.setOnAction((event) ->
            cli.makeRequest("b1", B1_COST, this)
        );

        opt2.setOnAction((event) ->
            cli.makeRequest("b2", B2_COST, this)
        );

        this.totalMoney = (Label) s.lookup("#money");
        addRes(s, "stone",    5, 2, 10, 1.08);
        addRes(s, "coal",     70, 15, 25, 1.08);
        addRes(s, "iron",     450, 130, 75, 1.11);
        addRes(s, "aluminum", 21000, 1100, 250, 1.09);
        addRes(s, "lead",     160000, 10000, 760, 1.10);
        addRes(s, "copper",   2200000, 860000, 2200, 1.08);
        addRes(s, "silver",   19400000, 1500000, 5000, 1.08);
        addRes(s, "gold",     620000000, 45000000, 12000, 1.07);
    }

    //NetworkListener.ClientPaymentListener
    public void onPinReceived(String key, String pin) {
        if (popup == null) {
            switch(key){
            case "b1":
                popup = new PaymentPopup(cli, "Money", pin, "DNR", CRYPTO_ADDRESS, B1_COST);
                break;
            case "b2":
                popup = new PaymentPopup(cli, "Money", pin, "DNR", CRYPTO_ADDRESS, B2_COST);
                break;
            }
            if(popup != null) {
                popup.show();
            }
        }
    }

    public void onPayReceived(String key, String txid) {
        addMoney(10000000);
        Platform.runLater(() -> {
            popup.close();
            popup = null;
        });
    }

    public void onPayError(String key, Exception e) {
        e.printStackTrace();
    }

    public void onPayCanceled(String key) {

    }

    public void onConfirmUpdate(String key, int confirms) {
        if(popup == null) return;
        Platform.runLater(() -> popup.setConfirms(confirms));
    }

    //Game Methods
    public void addRes(Scene start, String name, int startCost, long value, long delay, double mult){
        Resource res = new Resource(start, name, startCost, value, delay, mult, this);
        this.resources.put(name, res);
        this.res.add(res);
    }

    public URL getLayout(String path){
        ClassLoader cl = this.getClass().getClassLoader();
        return cl.getResource("Layouts/" + path);
    }

    public void addMoney(long amount) {
        money += amount;
        setText(totalMoney, "Total Money: " + format.format(money));
    }

    public void buyMiner(String name) {
        Resource find = resources.get(name);
        long res = find.addMiner(money);
        this.money = res == -1 ? money : res;
        setText(totalMoney, "Total Money: " + format.format(money));

        System.out.println("Money: " + money);
        System.out.println("Miners: " + find.getMiners());

        if(find.getMiners() == 1 && !find.added){
            find.added = true;
            Thread t = new Thread(find);
            t.start();
        }
    }

    private class Resource implements Runnable{
        private volatile boolean added;
        private volatile double cost;
        private volatile long miners;
        private double multiply;
        private Button buy;

        private ProgressBar prog;
        private double progress;
        private long dur, delay;

        private Label made;
        private long money;

        private final MoneyListener listener;
        private final String name, print;
        private final long value;

        private Resource(Scene start, String name, int startCost, long value, long delay, double mult, final MoneyListener listener){
            System.out.println(name);
            this.prog = (ProgressBar) start.lookup("#" + name + "_progress");
            this.made = (Label) start.lookup("#" + name + "_money");
            this.buy = (Button) start.lookup("#" + name + "_buy");

            this.multiply = mult;
            this.name = name;
            this.print = (name.charAt(0) + "").toUpperCase() + name.substring(1);
            this.value = value;
            this.cost = startCost;
            this.delay = delay;
            this.listener = listener;

            setText(made, print + " Earnings: " + format.format(money));
            setText(buy, "(" + miners + ") Buy Miner: " + format.format((long)cost));

            this.buy.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent event) {
                    listener.buyMiner(Resource.this.name);
                }
            });
        }

        public void run(){
            while(miners > 0) {
                int divider = (int) miners / 25;
                divider = divider == 0 ? 1 : divider * 2;

                long interval = (delay / divider);

                if (prog.getProgress() >= 0.999) {
                    money += value * miners;
                    listener.addMoney(value * miners);
                    setText(made, print + " Earnings: " + format.format(money));
                    prog.setProgress(0.0);
                } else {
                    if (interval * 4 > 4) {
                        prog.setProgress(prog.getProgress() + 0.01);
                        sleep(interval);
                    } else {
                        prog.setProgress(1);
                        sleep(interval * 100);
                    }
                }

            }
        }

        public long addMiner(long money){
            if(miners > 0) {
                System.out.println(delay / miners);
            }
            long deduct = (long)cost;
            if(deduct <= money){
                miners++;
                cost *= multiply;
                setText(buy, "(" + miners + ") Buy Miner: " + format.format((long)cost));
                return money - deduct;
            } else {
                return -1;
            }
        }

        public long getMiners(){
            return miners;
        }

        public void sleep(long mills){
            try{
                Thread.sleep(mills);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void setText(final Label lbl, final String text){
        Platform.runLater(new Runnable() {
            public void run() {
                lbl.setText(text);
            }
        });
    }

    public static void setText(final Button lbl, final String text){
        Platform.runLater(new Runnable() {
            public void run() {
                lbl.setText(text);
            }
        });
    }
}
