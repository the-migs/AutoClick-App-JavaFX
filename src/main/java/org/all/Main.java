package org.all;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.awt.Robot;
import java.awt.AWTException;
import java.nio.file.Paths;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

//obrigatorio extender de Application
public class Main extends Application {
    private static volatile int repetitions = 100;
    private static volatile int timeBeforePlay;
    private static volatile int keyCode = NativeKeyEvent.VC_F6;
    private static volatile String keyName;
    private static volatile boolean playMusic = true;
    private static volatile boolean checkThreadAutoClick = false;
    protected static volatile boolean running = true;
    protected static volatile boolean checkPlayButtonAutoClick = false;
    static volatile boolean hotkeyDown = false;
    private static Robot robot;
    private boolean capturing = false;
    protected Thread threadAutoClick;
    private Music music;
    protected static Number newVolum;

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
        music = new Music();
        music.start();

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
        soundHBox.setPadding(new Insets(3   ,0,0,3));
        soundHBox.setSpacing(4);
        soundHBox.setPickOnBounds(false);

        Image configIcon = new Image(getClass().getResource("/imagens/configs.gif").toExternalForm());
        ImageView configIconView = new ImageView(configIcon);
        configIconView.setFitHeight(55);
        configIconView.setFitWidth(55);
        configIconView.setPreserveRatio(true);

        Image namesIcon = new Image(getClass().getResource("/imagens/names.png").toExternalForm());
        ImageView namesIconView = new ImageView(namesIcon);
        namesIconView.setFitHeight(15);
        namesIconView.setFitWidth(15);

        Image playIcon = new Image(getClass().getResource("/imagens/play.png").toExternalForm());
        ImageView playIconView = new ImageView(playIcon);
        playIconView.setFitWidth(15);
        playIconView.setFitHeight(15);

        Image stopIcon = new Image(getClass().getResource("/imagens/pause.png").toExternalForm());
        ImageView stopIconView = new ImageView(stopIcon);
        stopIconView.setFitWidth(15);
        stopIconView.setFitHeight(15);

        Image returnIcon = new Image(getClass().getResource("/imagens/return.png").toExternalForm());
        ImageView returnIconView = new ImageView(returnIcon);
        returnIconView.setFitWidth(15);
        returnIconView.setFitHeight(15);

        Image return10sIcon = new Image(getClass().getResource("/imagens/return_10s.png").toExternalForm());
        ImageView return10sIconView = new ImageView(return10sIcon);
        return10sIconView.setFitWidth(15);
        return10sIconView.setFitHeight(15);

        Image skip10sIcon = new Image(getClass().getResource("/imagens/skip_10s.png").toExternalForm());
        ImageView skip10sIconView = new ImageView(skip10sIcon);
        skip10sIconView.setFitWidth(15);
        skip10sIconView.setFitHeight(15);

        Image skipIcon = new Image(getClass().getResource("/imagens/skip.png").toExternalForm());
        ImageView skipIconView = new ImageView(skipIcon);
        skipIconView.setFitWidth(15);
        skipIconView.setFitHeight(15);

        Image volumMaxIcon = new Image(getClass().getResource("/imagens/volumMax.png").toExternalForm());
        ImageView volumMaxIconView = new ImageView(volumMaxIcon);
        volumMaxIconView.setFitWidth(15);
        volumMaxIconView.setFitHeight(15);

        Image volumMedianIcon = new Image(getClass().getResource("/imagens/volumMedian.png").toExternalForm());
        ImageView volumMedianIconView = new ImageView(volumMedianIcon);
        volumMedianIconView.setFitWidth(15);
        volumMedianIconView.setFitHeight(15);

        Image volumeLittleIconView = new Image(getClass().getResource("/imagens/volumeLow.png").toExternalForm());
        ImageView volumLowIconView = new ImageView(volumeLittleIconView);
        volumLowIconView.setFitWidth(15);
        volumLowIconView.setFitHeight(15);

        Image muteMusicIcon = new Image(getClass().getResource("/imagens/muteMusic.png").toExternalForm());
        ImageView muteMusicIconView = new ImageView(muteMusicIcon);
        muteMusicIconView.setFitWidth(15);
        muteMusicIconView.setFitHeight(15);

        Button configButton = new Button();
        configButton.setGraphic(configIconView);
        configButton.getStyleClass().add("etc");

        configButton.pressedProperty().addListener((obs, oldVal, pressed) -> {
            if (pressed) {
                configIconView.setScaleX(0.9);
                configIconView.setScaleY(0.9);
            } else {
                configIconView.setScaleX(1);
                configIconView.setScaleY(1);
            }
        });

        HBox configHBox = new HBox();
        configHBox.setPadding(new Insets(8,0,0,3));
        configHBox.setPickOnBounds(false);

        configHBox.setAlignment(Pos.TOP_LEFT);

        configHBox.getChildren().add(configButton);

        Button namesButton = new Button();
        namesButton.setGraphic(namesIconView);
        namesButton.getStyleClass().add("button-all");

        Label namesLabel = new Label("Vanished (Slowed) by mkl\nPure Imagination Orchestra by Adonal Michel");

        CustomMenuItem namesItem = new CustomMenuItem(namesLabel, false);
        ContextMenu namesResult = new ContextMenu(namesItem);
        namesResult.getStyleClass().add("names-menu");

        namesButton.setOnAction(e -> {
            if(namesResult.isShowing()) {
                namesResult.hide();
            } else {
                namesResult.show(namesButton, Side.BOTTOM, 0,0);
            }
        });

        Button skipButton = new Button();
        skipButton.setGraphic(skipIconView);
        skipButton.getStyleClass().add("button-all");
        skipButton.setOnAction(event -> {
           music.next();
        });

        Button skip10sButton = new Button();
        skip10sButton.setGraphic(skip10sIconView);
        skip10sButton.getStyleClass().add("button-all");
        skip10sButton.setOnAction(event -> {
            music.skip10s();
        });

        Button returnButton = new Button();
        returnButton.setGraphic(returnIconView);
        returnButton.getStyleClass().add("button-all");
        returnButton.setOnAction(event -> {
            music.returnMusic();
        });

        Button return10sButton = new Button();
        return10sButton.setGraphic(return10sIconView);
        return10sButton.getStyleClass().add("button-all");
        return10sButton.setOnAction(event -> {
            music.return10s();
        });

        Button playAndStopButton = new Button();
        playAndStopButton.setGraphic(stopIconView);
        playAndStopButton.setOnAction(e -> {
            if (playMusic) {
                playAndStopButton.setGraphic(playIconView);
                playMusic = false;
                music.pause();
            }
            else {
                playAndStopButton.setGraphic(stopIconView);
                playMusic = true;
                music.play();
            }
        });
        playAndStopButton.getStyleClass().add("button-all");

        Button volumeBtn = new Button();
        volumeBtn.setGraphic(volumMedianIconView);
        volumeBtn.getStyleClass().add("button-all");

        Slider volumeSlider = new Slider(0,1,0.2);
        music.volume(0.2);
        volumeSlider.setFocusTraversable(false);
        volumeSlider.setPrefWidth(110);

        volumeSlider.valueProperty().addListener((ov, oldV, newV) -> {
            newVolum = newV;
            if(newV != null){
                music.volume(newV);
            } else {
                music.volume(0.2);
            }
            if(newV.doubleValue() > 0.5 && newV != null) {volumeBtn.setGraphic(volumMaxIconView);}
            if(newV.doubleValue() < 0.5 && newV.doubleValue() > 0.2 && newV != null) {volumeBtn.setGraphic(volumMedianIconView);}
            if(newV.doubleValue() < 0.2 && newV.doubleValue() > 0.0 && newV != null) {volumeBtn.setGraphic(volumLowIconView);}
            if(newV.doubleValue() == 0.0 && newV != null) {volumeBtn.setGraphic(muteMusicIconView);}
        });

        CustomMenuItem sliderItem = new CustomMenuItem(volumeSlider, false);
        ContextMenu volumMenu = new ContextMenu(sliderItem);

        volumeBtn.setOnAction(e -> {
            if(volumMenu.isShowing()) {
                volumMenu.hide();
            } else{
                volumMenu.show(volumeBtn, Side.RIGHT, 2,7);
            }
        });

        soundHBox.getChildren().addAll(namesButton,returnButton, return10sButton, playAndStopButton, skip10sButton, skipButton, volumeBtn);
        // Layout e container
        VBox centerVBOX = new VBox();

        StackPane rootStackPane = new StackPane();
        StackPane.setAlignment(rootStackPane, Pos.TOP_CENTER);
        soundHBox.setAlignment(Pos.TOP_CENTER);

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
        repetitionsSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            repetitions = newValue;
        });

        Label timeBeforePlayLabel = new Label("Seconds before starting:");
        timeBeforePlayLabel.getStyleClass().add("label-all");

        Spinner<Integer> timeBeforePlaySpinner = new Spinner<>(0, 999999999, 0);
        timeBeforePlaySpinner.setEditable(true);
        timeBeforePlaySpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            timeBeforePlay = newValue * 1000;
        });

        configGridPane.add(repetitionsLabel, 0, 0);
        configGridPane.add(repetitionsSpinner, 1, 0);
        configGridPane.add(timeBeforePlayLabel, 0, 1);
        configGridPane.add(timeBeforePlaySpinner, 1, 1);

        rootStackPane.getChildren().addAll(centerVBOX, configHBox, soundHBox);
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

        music.play();

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

class Music {
    private MediaPlayer mediaPlayer;
    private List<Path> playlist;
    private int index = 0;

    public Music() {
        try {
            Path pasta = Paths.get("musics");
            playlist = Files.list(pasta)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".mp3"))
                    .sorted()
                    .toList();

            System.out.println("Qtd músicas: " + playlist.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (playlist == null || playlist.isEmpty()) return;
        playIndex(index);
    }

    private void playIndex(int i) {

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            // libera recursos nativos, evita “vazamento” de recursos
            // evita comportamento estranho ao trocar de música (como não tocar a próxima)
            mediaPlayer.dispose();

        }
        // pega o caminho da musica da vez
        Path file = playlist.get(i);

        // file.toUri().toString() -> O construtor de Media precisa de uma String em formato URI tipo:
        // file:/home/the-migs/.../DarkBeach.mp3
        mediaPlayer = new MediaPlayer(new Media(file.toUri().toString()));

        // sempre que a musica acaba, chama o next
        mediaPlayer.setOnEndOfMedia(this::next);

        mediaPlayer.setOnError(() -> System.out.println("Player erro: " + mediaPlayer.getError()));
        mediaPlayer.play();

        System.out.println("Tocando: " + file.getFileName());
    }

    public void next() {
        if (playlist == null || playlist.isEmpty()) return;
        // % -> retorna zero se chegar no final da playlist
        index = (index + 1) % playlist.size();
        playIndex(index);
        if(Main.newVolum != null) {volume(Main.newVolum);}
        else {volume(0.2);}
    }
    public void returnMusic() {
        if (playlist == null || playlist.isEmpty()) return;
        // index = (index - 1 + playlist.size()) % playlist.size();
        index = (index - 1) % playlist.size();
        if(index < 0) index = 0;
        playIndex(index);
        if(Main.newVolum != null) {volume(Main.newVolum);}
        else {volume(0.2);}
    }
    protected void return10s() {
        if (playlist == null || playlist.isEmpty()) return;
        Duration newTime = mediaPlayer.getCurrentTime().subtract(Duration.seconds(10));
        mediaPlayer.seek(newTime);
    }
    protected void skip10s() {
        if (playlist == null || playlist.isEmpty()) return;
        Duration newTime = mediaPlayer.getCurrentTime().add(Duration.seconds(10));
        mediaPlayer.seek(newTime);
    }

    protected void volume(Number newV) {
        if (playlist == null || playlist.isEmpty() || mediaPlayer == null) return;
        mediaPlayer.setVolume(newV.doubleValue());
    }

    public void pause() { if (mediaPlayer != null) mediaPlayer.pause(); }
    public void play()  { if (mediaPlayer != null) mediaPlayer.play(); }
}