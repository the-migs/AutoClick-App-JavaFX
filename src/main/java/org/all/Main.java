package org.all;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

import java.awt.*;

//obrigatorio extender de Application
public class Main extends Application {
    private static volatile int repetitions = 100;
    private static volatile int timeBeforePlay;
    private static volatile int keyCode;
    private static volatile String keyName;
    private static volatile boolean playMusic = true;
    private static volatile boolean checkThreadAutoClick = false;
    protected static volatile boolean running = true;
    protected static volatile boolean checkPlayButtonAutoClick = false;
    static volatile boolean hotkeyDown = false;
    static volatile boolean KILLPORRA = false;
    private static Robot robot;
    private boolean captureKey = false;
    private boolean capturing = false;
    protected Thread threadAutoClick;

    protected void metodThreadAutoClick(){
        threadAutoClick = new Thread(() -> {
            checkPlayButtonAutoClick = true;
            running = true;
            while (running) {
                if(!checkThreadAutoClick) {
                    checkThreadAutoClick = true;
                    try {
                        Thread.sleep(timeBeforePlay);
                        for (int i = 0; i < repetitions && running; i++) {
                            try {
                                robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
                                Thread.sleep(6);
                                robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        running = false;
                        checkThreadAutoClick = false;
                        checkPlayButtonAutoClick = false;
                    }
                }
            }
        });
        if(!checkThreadAutoClick) {threadAutoClick.start();}
    }

    @Override
    public void start(Stage stage) {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }

        stage.setOnCloseRequest(event -> {
            // ao fechar o app, "mata" a thread de forma segura
            // running fica false e ela fecha sozinha
            // se estiver dormindo o interrupt manda um InterruptedException e a Thread sai imediatamente
           running = false;
           if (threadAutoClick != null && threadAutoClick.isAlive()) {
               threadAutoClick.interrupt();
           }

           // desliga o JNativeHook
           try{
               GlobalScreen.unregisterNativeHook();
           } catch (NativeHookException e) {
               e.printStackTrace();
           }

           // encerra a javafx
           javafx.application.Platform.exit();
           // garante que tudo feche mesmo
           System.exit(0);
        });

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

        setKeyButton.setOnAction(e -> {
            if (capturing) return;
            capturing = true;

            setKeyButton.setText("Press a key...");

            NativeKeyListener tempListener = new NativeKeyListener() {
                @Override
                public void nativeKeyReleased(NativeKeyEvent ev) {
                    keyCode = ev.getKeyCode();
                    keyName = NativeKeyEvent.getKeyText(keyCode);

                    javafx.application.Platform.runLater(() -> {
                        setKeyButton.setText("Key defined: " + keyName);
                    });

                    GlobalScreen.removeNativeKeyListener(this);
                    capturing = false;
                }
            };

            GlobalScreen.addNativeKeyListener(tempListener);
        });

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {

            @Override
            public void nativeKeyPressed(NativeKeyEvent event) {
                if (event.getKeyCode() != keyCode) return;

                // evita repetir enquanto a tecla está segurada
                if (hotkeyDown) return;
                hotkeyDown = true;

                if (checkThreadAutoClick) {
                    checkThreadAutoClick = false;
                    checkPlayButtonAutoClick = false;
                    running = false;
                    if (threadAutoClick != null) threadAutoClick.interrupt();
                } else {
                    if (!checkPlayButtonAutoClick) {
                        metodThreadAutoClick();
                    }
                }
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent event) {
                if (event.getKeyCode() == keyCode) {
                    hotkeyDown = false; // libera pra próxima vez
                }
            }
        });

        Button playAutoClickButton = new Button("Play AutoClick");
        playAutoClickButton.setPrefHeight(50);
        playAutoClickButton.setPrefWidth(200);
        playAutoClickButton.getStyleClass().add("button-all");
        playAutoClickButton.setOnAction(event -> {
            metodThreadAutoClick();
        });

        Button stopAutoClickButton = new Button("Stop AutoClick");
        stopAutoClickButton.getStyleClass().add("button-all");
        stopAutoClickButton.setOnAction(event -> {
            running = false;
            checkThreadAutoClick = false;
            checkPlayButtonAutoClick = false;
            threadAutoClick.interrupt();
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

        rootStackPane.getChildren().addAll(centerVBOX, soundHBox);
        // scene -> conteudo de dentro da janela, guarda toda a arvore visual
        // Scene(centerVBOX, 400, 400) - > define layout e tamanho
        Scene scene = new Scene(rootStackPane, 550, 550);

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
        try{
            robot = new Robot();
        } catch (AWTException e){
            e.printStackTrace();
        }

        launch();
    }
}