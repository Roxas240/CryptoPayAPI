package com.xemplar.libs.crypto.example;

import com.xemplar.libs.crypto.client.CryptoClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Rohan on 8/22/2017.
 */
public class PaymentPopup extends Application{
    public final String item, pass, ticker, address;
    public final BigDecimal amount;
    public final CryptoClient cli;

    private Stage primaryStage;

    public PaymentPopup(CryptoClient cli, String item, String pass, String ticker, String address, BigDecimal amount){
        this.cli = cli;
        this.item = item;
        this.ticker = ticker;
        this.amount = amount;
        this.address = address;

        String code = "";
        for(int i = 0; i < pass.length(); i++){
            code += (i == 0 ? "" : " ") + pass.charAt(i);
        }

        this.pass = code;
    }

    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getLayout("payment.fxml"));

        StackPane root = new StackPane();
        root.getChildren().add((Node)loader.load());

        Scene s = new Scene(root, 400, 550);
        primaryStage.setScene(s);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent event) {
                cli.cancelRequest("");
            }
        });

        ProgressBar bar = (ProgressBar) s.lookup("#pay_progress");
        ImageView view = (ImageView) s.lookup("#qr_code");
        Label addr = (Label) s.lookup("#address_label");
        Label item = (Label) s.lookup("#pay_item");
        Label cost = (Label) s.lookup("#pay_cost");
        Label code = (Label) s.lookup("#pay_code");
        Button copy = (Button) s.lookup("#btn_copy");
        Button cancel = (Button) s.lookup("#pay_cancel");

        copy.setOnAction((event) -> {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(address);
            clipboard.setContent(content);
        });

        cancel.setOnAction((event) -> {
            cli.cancelRequest("");
            close();
        });

        try{
            Image img = cli.getQR(250, 250);
            view.setImage(img);
        } catch(Exception e){
            e.printStackTrace();
        }

        cost.setText(this.amount.toPlainString() + " " + this.ticker);
        item.setText(this.item);
        code.setText(this.pass);
        addr.setText("Pay to: " + address);

        this.primaryStage = primaryStage;
    }

    public static ArrayList<Node> getAllNodes(Parent root) {
        ArrayList<Node> nodes = new ArrayList<Node>();
        addAllDescendents(root, nodes);
        return nodes;
    }

    private static void addAllDescendents(Parent parent, ArrayList<Node> nodes) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
            if (node instanceof Parent)
                addAllDescendents((Parent)node, nodes);
        }
    }

    public URL getLayout(String path){
        ClassLoader cl = this.getClass().getClassLoader();
        return cl.getResource("Layouts/" + path);
    }

    public void close(){
        primaryStage.close();
    }
    public void show(){
        Platform.runLater(new Runnable() {
            public void run() {
                try {
                    start(new Stage());
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}
