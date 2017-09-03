import com.xemplar.libs.crypto.server.ServerListener;
import com.xemplar.libs.crypto.server.net.CryptoServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.math.BigDecimal;
import java.net.URL;

/**
 * Created by Rohan on 8/15/2017.
 */
public class Main extends Application implements ServerListener{
    private static String location;
    private ObservableList<TX> model;
    private TableView<TX> table;
    private Stage primaryStage;
    private Scene s;
    private int canceled;

    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getLayout("server.fxml"));

        StackPane root = new StackPane();
        root.getChildren().add((Node)loader.load());

        s = new Scene(root, 800, 480);
        primaryStage.setScene(s);
        primaryStage.show();
        this.primaryStage = primaryStage;
        this.table = (TableView<TX>) s.lookup("#table");
        this.model = FXCollections.emptyObservableList();
        this.table.setItems(model);
        ObservableList<TableColumn<TX, ?>> cols = this.table.getColumns();

        TableColumn<TX, Integer> id = (TableColumn<TX, Integer>)cols.get(0);
        id.setCellValueFactory(cellData -> cellData.getValue().id.asObject());
        TableColumn<TX, String> pin = (TableColumn<TX, String>)cols.get(1);
        pin.setCellValueFactory(cellData -> cellData.getValue().pin);
        TableColumn<TX, String> desc = (TableColumn<TX, String>)cols.get(2);
        desc.setCellValueFactory(cellData -> cellData.getValue().desc);
        TableColumn<TX, String> status = (TableColumn<TX, String>)cols.get(3);
        status.setCellValueFactory(cellData -> cellData.getValue().status);
        TableColumn<TX, String> amount = (TableColumn<TX, String>)cols.get(4);
        amount.setCellValueFactory(cellData -> cellData.getValue().amount);
        TableColumn<TX, Long> time = (TableColumn<TX, Long>)cols.get(5);
        time.setCellValueFactory(cellData -> cellData.getValue().timeFilled.asObject());

        primaryStage.setOnCloseRequest((WindowEvent event) -> System.exit(0));
        CryptoServer server = new CryptoServer(location, this);
        Thread t = new Thread(server);
        t.start();
    }

    public URL getLayout(String path){
        ClassLoader cl = this.getClass().getClassLoader();
        return cl.getResource("Layouts/" + path);
    }

    public void onRequestCanceled(int id) {
        canceled++;
        setText((Label)s.lookup("#req_cancel"), canceled + "");
    }

    public void onRequestFilled(int id) {
        for(TX tx : model){
            if(tx.sortID == id){
                tx.setFilled();
                return;
            }
        }
    }

    public void onRequestReceived(int id, String desc, BigDecimal amount) {
        Platform.runLater(() -> model.add(new TX(id, desc, amount)));
    }

    public void onRequestGenerated(final int id, final String pin) {
        Platform.runLater(() -> {
            for(TX tx : model){
                if(tx.sortID == id){
                    tx.setPin(pin);
                }
            }
            table.refresh();
        });
    }

    public void onRequestUpdate(int id, String status){
        Platform.runLater(() -> {
            for(TX tx : model){
                if(tx.sortID == id){
                    tx.setStatus(status);
                }
            }
            table.refresh();
        });
    }

    private class TX{
        private final SimpleStringProperty amount;
        private final SimpleStringProperty desc;
        private final SimpleIntegerProperty id;

        private SimpleLongProperty timeFilled;
        private SimpleStringProperty status;
        private SimpleStringProperty pin;

        private int sortID;

        private TX(int id, String desc, BigDecimal amount){
            this.amount = new SimpleStringProperty(amount.toPlainString());
            this.desc = new SimpleStringProperty(desc);
            this.id = new SimpleIntegerProperty(id);
            this.sortID = id;

            this.timeFilled = new SimpleLongProperty(0);
            this.status = new SimpleStringProperty("Requested");
            this.pin = new SimpleStringProperty("");
        }

        private void setFilled(){
            this.timeFilled = new SimpleLongProperty(System.currentTimeMillis());
        }

        private void setPin(String pin){
            this.pin.set(pin);
        }

        private void setStatus(String status){
            this.status.set(status);
        }
    }

    public static void main(String[] args){
        args = "node_config.prop".split(" ");
        if(args == null) error();
        if(args.length != 1) error();

        location = args[0];
        launch(args);
    }

    public static void error(){
        System.out.println("Usage:\n" +
                "  java -jar CryptoPayAPI.jar <node_config>\n" +
                "    <node_config>  Location of the node_config.prop");
        System.exit(1);
    }

    public static void setText(final Label lbl, final String text){
        Platform.runLater(() -> lbl.setText(text));
    }

    public static void setText(final Button lbl, final String text){
        Platform.runLater(() -> lbl.setText(text));
    }
}
