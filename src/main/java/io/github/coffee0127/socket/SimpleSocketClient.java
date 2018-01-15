/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Bo-Xuan Fan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.coffee0127.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * SocketClient
 *
 * @since 2018-01-07
 * @author Bo-Xuan Fan
 */
public class SimpleSocketClient extends Application {

    private static final String DEBUG_LEVEL = "[DEBUG] - ";
    private static final String ERROR_LEVEL = "[ERROR] - ";
    private static final String[] availableEncodings = { "UTF-8", "Big5" };

    private TextField port;
    private TextField ip;
    private TextArea reqData;
    private ComboBox<String> reqEncodingOption;
    private TextArea resData;
    private ComboBox<String> resEncodingOption;
    private TextArea console;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        root.setCenter(addGridPane());

        Scene scene = new Scene(root, 750, 450);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Socket Client");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image(Thread.currentThread().getContextClassLoader().getResourceAsStream("favicon.png")));
        primaryStage.show();
    }

    private GridPane addGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.add(new Label("IP"), 0, 0);
        gridPane.getColumnConstraints().add(new ColumnConstraints(80));
        gridPane.getRowConstraints().add(new RowConstraints());
        ip = new TextField();
        gridPane.add(ip, 1, 0);
        gridPane.getColumnConstraints().add(new ColumnConstraints(250));

        gridPane.add(new Label("Port："), 0, 1);
        gridPane.getRowConstraints().add(new RowConstraints());
        port = new TextField();
        port.setText("35000");
        port.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue == null || newValue.isEmpty()) {
                    port.setText(oldValue);
                }

                if (!newValue.matches("\\d*")) {
                    port.setText(newValue.replaceAll("[^\\d]", ""));
                }

                int portNumber = Integer.parseInt(port.getText());
                if (portNumber > 65535) {
                    port.setText("65535");
                }
            }
        });
        gridPane.add(port, 1, 1);

        VBox hbox = new VBox();
        hbox.setSpacing(5);
        reqEncodingOption = new ComboBox<>(FXCollections.observableArrayList(availableEncodings));
        reqEncodingOption.setValue(availableEncodings[0]);
        hbox.getChildren().addAll(new Label("Request："), reqEncodingOption);
        gridPane.add(hbox, 0, 2);
        gridPane.getRowConstraints().add(new RowConstraints(150));
        reqData = new TextArea();
        reqData.setPrefWidth(350);
        reqData.setWrapText(true);
        gridPane.add(reqData, 1, 2);

        hbox = new VBox();
        hbox.setSpacing(5);
        resEncodingOption = new ComboBox<>(FXCollections.observableArrayList(availableEncodings));
        resEncodingOption.setValue(availableEncodings[0]);
        hbox.getChildren().addAll(new Label("Response："), resEncodingOption);
        gridPane.add(hbox, 0, 3);
        gridPane.getRowConstraints().add(new RowConstraints(150));
        resData = new TextArea();
        resData.setPrefWidth(350);
        resData.setPrefRowCount(10);
        resData.setWrapText(true);
        gridPane.add(resData, 1, 3);

        gridPane.add(addButton(), 1, 4);

        gridPane.add(new Label("Debug Console Output："), 2, 0);
        console = new TextArea();
        console.setEditable(false);
        gridPane.add(console, 2, 1, 1, 3);

        gridPane.setPadding(new Insets(20, 10, 20, 10));
        gridPane.setHgap(15);
        gridPane.setVgap(10);
        return gridPane;
    }

    private Button addButton() {
        Button btn = new Button();
        btn.setText("GO! ヾ( ⁰ д ⁰)ﾉ");
        btn.setOnAction(event -> {
            console.setText(log(DEBUG_LEVEL, String.format("IP=%s, Port=%s, Request=%s", ip.getText(), port.getText(), reqData.getText())) + console.getText());

            new Thread(() -> {
                try {
                    Socket socket = new Socket(ip.getText(), Integer.parseInt(port.getText()));

                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), reqEncodingOption.getValue()));
                    pw.println(reqData.getText());
                    pw.flush();

                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), reqEncodingOption.getValue()));
                    String response = br.readLine();
                    console.setText(log(DEBUG_LEVEL, String.format("Response=%s", response)) + console.getText());
                    resData.setText(response);

                    pw.close();
                    br.close();
                    socket.close();
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    StringBuilder stackTrace = new StringBuilder(sw.toString());
                    stackTrace.setLength(stackTrace.length() - 1);
                    console.setText(log(ERROR_LEVEL, stackTrace.toString()) + console.getText());
                }
            }).start();
        });

        return btn;
    }

    private String log(String level, String message) {
        return new StringBuilder()
                .append(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now())).append(' ')
                .append(level)
                .append(message)
                .append(System.lineSeparator())
                .toString();
    }
}
