package org.all;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.awt.*;

//obrigatorio extender de Application
public class Main extends Application {
    private boolean captureKey = false;

    // metodo responsavel por construir tudo
    // Stage stage -> tela inicial do aplicativo
    @Override
    public void start(Stage stage) {
        // Layout e container
        VBox root = new VBox();
        // deixa os componentes no centro
        root.setAlignment(Pos.CENTER);
        //espacamento entre os componentes
        root.setSpacing(20);
        Button setKeyButton = new Button("Set Key: F6");
        // define o estilo do botao com base na classe CSS
        setKeyButton.getStyleClass().add("button-all");
        // muda o texto e torna true a variavel que vai ativar o if do scene
        setKeyButton.setOnAction(event -> {
            setKeyButton.setText("Press a key...");
            captureKey = true;
        });

        Button playAutoClickButton = new Button("Play AutoClick");
        playAutoClickButton.setPrefHeight(50);
        playAutoClickButton.setPrefWidth(200);
        playAutoClickButton.getStyleClass().add("button-all");
        playAutoClickButton.setOnAction(event -> {});

        Button stopAutoClickButton = new Button("Stop AutoClick");
        stopAutoClickButton.getStyleClass().add("button-all");
        stopAutoClickButton.setOnAction(event -> {});
        // scene -> conteudo de dentro da janela, guarda toda a arvore visual
        // Scene(root, 400, 400) - > define layout e tamanho
        Scene scene = new Scene(root, 500, 500);

        // vai capturar a tecla precionada, mudar o texto do botao setKey e tornar false a variavel q ativa o proprio if
        scene.setOnKeyPressed(event -> {
           if (captureKey) {
               KeyCode keyCode = event.getCode();
               setKeyButton.setText("Set Key: " + keyCode.getName());
               captureKey = false;
           }
        });

        // Adiciona os botoes
        root.getChildren().addAll(setKeyButton, playAutoClickButton, stopAutoClickButton);
        // Difine o arquivo css que ditara o estilo e as configuracoes da janela principal
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        // impede que a tela seja colocada em tela cheia ou aumentada
        stage.setResizable(false);
        // define qual sera o conteudo de dentro da janela
        stage.setScene(scene);
        // torna a janela principal visivel

        // Platform.runLater(()
        // roda o comando abaixo na thread do JavaFX
        // coloca o codigo na fila do JavaFX
        // fala pro JavaFX executar quando for seguro
        Platform.runLater(() -> {
            // guarda o tamanho da area visivel da tela
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            // centraliza o app no centro da tela
            stage.setX((bounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((bounds.getHeight() - stage.getHeight()) / 2);
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
class AutoClick{
    private void metodAutoClick(){

    }
}