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
import java.io.PrintWriter;
import java.net.Socket;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

/**
 * SocketClient
 *
 * @since 2018-01-07
 * @author Bo-Xuan Fan
 */
public class SocketClient extends Application {

    private TextField port;
    private TextField ip;
    private TextArea reqData;
    private TextArea resData;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        root.setCenter(addGridPane());

        Scene scene = new Scene(root, 450, 450);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Socket Client");
        primaryStage.show();
    }

    private GridPane addGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.add(new Label("IP"), 0, 0);
        gridPane.getRowConstraints().add(new RowConstraints());
        ip = new TextField();
        gridPane.add(ip, 1, 0);
        gridPane.add(new Label("Port"), 0, 1);
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
        gridPane.add(new Label("Request"), 0, 2);
        gridPane.getRowConstraints().add(new RowConstraints(150));
        reqData = new TextArea();
        reqData.setPrefWidth(350);
        reqData.setWrapText(true);
        gridPane.add(reqData, 1, 2);
        gridPane.add(new Label("Response"), 0, 3);
        gridPane.getRowConstraints().add(new RowConstraints(150));
        resData = new TextArea();
        resData.setPrefWidth(350);
        resData.setPrefRowCount(10);
        resData.setWrapText(true);
        gridPane.add(resData, 1, 3);
        gridPane.add(addButton(), 1, 4);

        gridPane.setPadding(new Insets(20, 10, 20, 10));
        gridPane.setHgap(15);
        gridPane.setVgap(10);
        return gridPane;
    }

    private Button addButton() {
        Button btn = new Button();
        btn.setText("GO! ヾ( ⁰ д ⁰)ﾉ");
        btn.setOnAction(event -> {
            try {
                System.out.println(String.format("IP=%s\nPort=%s\nData=%s", ip.getText(), port.getText(), reqData.getText()));
                Socket socket = new Socket(ip.getText(), Integer.parseInt(port.getText()));

                PrintWriter pw = new PrintWriter(socket.getOutputStream());
                pw.println(reqData.getText());
                pw.flush();

                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                resData.setText(br.readLine());

                pw.close();
                br.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return btn;
    }
}
