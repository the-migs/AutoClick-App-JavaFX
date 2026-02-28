package org.all;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

//obrigatorio extender de Application
public class Main extends Application {
    private boolean captureKey = false;
    private volatile int repetitions = 0;
    private volatile int timeBeforePlay = 0;
    private volatile boolean playMusic = true;

    // metodo responsavel por construir tudo
    // Stage stage -> tela inicial do aplicativo
    @Override
    public void start(Stage stage) {

        HBox soundHBox = new HBox();
        soundHBox.setSpacing(4);
        soundHBox.setPickOnBounds(false);
        Button returnButton = new Button("<<");
        returnButton.getStyleClass().add("button-all");
        Button playButton = new Button("No dance");
        playButton.setOnAction(e -> {
            if (playMusic) {
                playButton.setText("Dance");
                playMusic = false;
            }
            else {
                playButton.setText("No dance");
                playMusic = true;
            }
        });

        playButton.getStyleClass().add("button-all");
        Button skipButton = new Button(">>");
        skipButton.getStyleClass().add("button-all");
        soundHBox.getChildren().addAll(returnButton, playButton, skipButton);
        // Layout e container
        VBox centerVBOX = new VBox();

        StackPane rootStackPane = new StackPane();
        StackPane.setAlignment(rootStackPane, Pos.TOP_LEFT);


        // deixa os componentes no centro
        centerVBOX.setAlignment(Pos.CENTER);
        //espacamento entre os componentes
        centerVBOX.setSpacing(20);
        Button setKeyButton = new Button("Set Key: F6");
        // define o estilo do botao com base na classe CSS
        setKeyButton.getStyleClass().add("button-all");
        // muda o texto e torna true a variavel que vai ativar o if do scene
        setKeyButton.setOnAction(event -> {
            setKeyButton.setText("Press a key...");
            captureKey = true;
        });

        GridPane configGridPane = new GridPane();
        configGridPane.setAlignment(Pos.CENTER);
        configGridPane.setPrefSize(500, 100);
        configGridPane.setMaxSize(500, 100);
        configGridPane.setVgap(8);
        configGridPane.setHgap(20);
        configGridPane.getStyleClass().add("GridPane-all");

        Label repetitionsLabel = new Label("Number of repetitions:");
        repetitionsLabel.getStyleClass().add("label-all");

        // cria um bloco com seta pra aumentar e diminuir os numeros
        Spinner<Integer> repetitionsSpinner = new Spinner<>(0, 999999999, 1000);
        // permite ser editavel
        repetitionsSpinner.setEditable(true);
        // pega o valor
        repetitions = repetitionsSpinner.getValue();

        Label timeBeforePlayLabel = new Label("Seconds before starting:");
        timeBeforePlayLabel.getStyleClass().add("label-all");

        Spinner<Integer> timeBeforePlaySpinner = new Spinner<>(0, 999999999, 0);
        timeBeforePlaySpinner.setEditable(true);
        timeBeforePlay = (timeBeforePlaySpinner.getValue()) * 1000;

        configGridPane.add(repetitionsLabel, 0, 0);
        configGridPane.add(repetitionsSpinner, 1, 0);
        configGridPane.add(timeBeforePlayLabel, 0, 1);
        configGridPane.add(timeBeforePlaySpinner, 1, 1);

        Button playAutoClickButton = new Button("Play AutoClick");
        playAutoClickButton.setPrefHeight(50);
        playAutoClickButton.setPrefWidth(200);
        playAutoClickButton.getStyleClass().add("button-all");
        playAutoClickButton.setOnAction(event -> {
        });

        Button stopAutoClickButton = new Button("Stop AutoClick");
        stopAutoClickButton.getStyleClass().add("button-all");
        stopAutoClickButton.setOnAction(event -> {});

        rootStackPane.getChildren().addAll(centerVBOX, soundHBox);
        // scene -> conteudo de dentro da janela, guarda toda a arvore visual
        // Scene(centerVBOX, 400, 400) - > define layout e tamanho
        Scene scene = new Scene(rootStackPane, 550, 550);

        // vai capturar a tecla precionada, mudar o texto do botao setKey e tornar false a variavel q ativa o proprio if
        scene.setOnKeyPressed(event -> {
           if (captureKey) {
               KeyCode keyCode = event.getCode();
               setKeyButton.setText("Set Key: " + keyCode.getName());
               captureKey = false;
           }
        });

        // Adiciona os botoes
        centerVBOX.getChildren().addAll(setKeyButton, playAutoClickButton, stopAutoClickButton, configGridPane);
        // Difine o arquivo css que ditara o estilo e as configuracoes da janela principal
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // define qual sera o conteudo de dentro da janela
        stage.setScene(scene);
        // impede que a tela seja colocada em tela cheia ou aumentada
        stage.setResizable(false);

        // antes de mostrar move a tela para uma area nao visivel
        // sem isso ela aparece no canto e depois centraliza
        stage.setX(-100000);
        stage.setY(-100000);
        // mesmo que o WM mostre ela, estara transparente
        stage.setOpacity(0);
        // pre centralizacao
        stage.centerOnScreen();
        // mostra a janela
        stage.show();

        // deixa o JavaFX + WM assentarem tamanho/decoração
        // espera 20 ms
        // sem esse delay a centralizacao pode usar um tamanho errado
        // pode dar micro pulo
        // pode nao centralizar
        PauseTransition p = new PauseTransition(Duration.millis(20));
        // Codigo roda depois que tudo assentou
        p.setOnFinished(e -> {
            stage.sizeToScene();
            stage.centerOnScreen(); // e depois disso tudo, centraliza com tudo preparado
            stage.setOpacity(1); // janela fica visivel
        });
        p.play(); // inicia o timer

    }

    public static void main(String[] args) {
        launch();
    }
}
class AutoClick{
    private void metodAutoClick(){

    }
}