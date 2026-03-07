package org.all;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

import java.awt.*;
import java.nio.file.Paths;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

//obrigatorio extender de Application
public class Main extends Application {
    private static volatile int keyCode = NativeKeyEvent.VC_F6;
    private static volatile String keyName;
    private static volatile boolean playMusic = false;
    static volatile boolean hotkeyDown = false;
    private boolean capturing = false;
    private Music music;
    protected static Number newVolum;

    private static volatile int repetitions = 5000;
    private static volatile int timeBeforePlay = 0;
    private static volatile int clickSpeed = 6;
    private static volatile boolean antiAFK = false;
    private static volatile int pauseMove = 10000;

    private static Robot robot;
    private static Robot robotMove;
    protected Thread threadAutoClick;
    protected Thread threadMove;

    protected static volatile boolean running = false;
    protected static volatile boolean checkPlayButtonAutoClick = false;
    private static volatile boolean checkThreadAutoClick = false;
    private volatile boolean checkThreadMove = false;
    private final Object robotLock = new Object();

    protected void warmUp() {
        try {
            robot.setAutoDelay(0);

            tapKey(KeyEvent.VK_SHIFT, 20);

            robot.mouseMove(MouseInfo.getPointerInfo().getLocation().x,
                    MouseInfo.getPointerInfo().getLocation().y);

            Toolkit.getDefaultToolkit();
            MouseInfo.getPointerInfo();

            new Thread(() -> {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }).start();

        } catch (Exception ignored) {
        }
    }

    protected void metodThreadAutoClick() {
        if (checkThreadAutoClick) return;      // já rodando, não duplica
        // trava ANTES do start

        threadAutoClick = new Thread(() -> {
            running = true;
            checkThreadAutoClick = true;

            try {
                Thread.sleep(timeBeforePlay);
                if(antiAFK) {
                    Thread.sleep(5000);
                }
                for (int i = 0; i < repetitions && running; i++) {

                    // Press (LOCK CURTO)
                    synchronized (robotLock) {
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    }

                    // fora do lock
                    Thread.sleep(clickSpeed);

                    // Release (LOCK CURTO)
                    synchronized (robotLock) {
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    }

                    // janela pro teclado entrar no meio dos clicks
                    Thread.sleep(2);
                }

            } catch (InterruptedException ignored) {
            } finally {
                running = false;
                checkThreadAutoClick = false;
                checkPlayButtonAutoClick = false;
            }
        });

        threadAutoClick.start();
    }

    protected void metodThreadMove() {
        if (checkThreadMove) return;           // já rodando, não duplica
        checkThreadMove = true;                // trava ANTES do start

        threadMove = new Thread(() -> {
            try {
                Thread.sleep(timeBeforePlay);

                while (antiAFK && running) {
                    // preciona e solta uma tecla automaticamente (140 -> tempo que fica precionada)
                    tapKey(KeyEvent.VK_W, 140);
                    Thread.sleep(450);

                    tapKey(KeyEvent.VK_S, 140);
                    Thread.sleep(450);

                    tapKey(KeyEvent.VK_SPACE, 50);
                    Thread.sleep(pauseMove);
                }

            } catch (InterruptedException ignored) {
            } finally {
                // evita tecla presa se parar no meio
                safeRelease(KeyEvent.VK_W);
                safeRelease(KeyEvent.VK_S);
                safeRelease(KeyEvent.VK_SPACE);

                checkThreadMove = false;
            }
        });

        threadMove.start();
    }

    // =========================
    // Stop (recomendado)
    // =========================
    protected void stopAutoClick() {
        checkThreadAutoClick = false;
        checkPlayButtonAutoClick = false;
        checkThreadMove = false;
        running = false;
        if (threadAutoClick != null) threadAutoClick.interrupt();
        if (threadMove != null) threadMove.interrupt();
    }

    // =========================
    // Helpers (micro-lock no Robot)
    // =========================
    private void tapKey(int keyCode, int holdMs) throws InterruptedException {
        if (!checkThreadMove) return;

        synchronized (robotLock) {
            robot.keyPress(keyCode);
        }

        Thread.sleep(holdMs);

        synchronized (robotLock) {
            robot.keyRelease(keyCode);
        }
    }

    private void safeRelease(int keyCode) {
        synchronized (robotLock) {
            robot.keyRelease(keyCode);
        }
    }

    @Override
    public void start(Stage stage) {
        StackPane loadingOverlay = new StackPane();
        loadingOverlay.setVisible(false);
        loadingOverlay.setManaged(false);
        loadingOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        loadingOverlay.setStyle("-fx-background-color: #08000F;");

        Image loadingGif = new Image(getClass().getResource("/imagens/loading.gif").toExternalForm());
        ImageView gifView = new ImageView(loadingGif);
        gifView.setFitWidth(120);
        // serve pra nao deformar o gif
        gifView.setPreserveRatio(true);
        Label loadingLabel = new Label("Look behind you...");
        loadingLabel.getStyleClass().add("label-loading");
        // container vertical
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.setSpacing(10);

        box.getChildren().addAll(gifView, loadingLabel);

        // adiciona no overlay
        loadingOverlay.getChildren().add(box);

        loadingOverlay.setVisible(true);
        // O layout considera ele
        loadingOverlay.setManaged(true);

        warmUp();

        Media LaughMedia = new Media(getClass().getResource("/Laugh.mp3").toExternalForm());
        MediaPlayer player = new MediaPlayer(LaughMedia);

        player.play();

        player.setOnEndOfMedia(player::dispose);

        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(4));
        pauseTransition.setOnFinished(event -> {
            loadingOverlay.setVisible(false);
            loadingOverlay.setManaged(false);
        });
        pauseTransition.play();

        music = new Music();
        music.start();
        music.pause();

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
           music.pause();
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

        Image namesOpenIcon = new Image(getClass().getResource("/imagens/names_open.png").toExternalForm());
        ImageView namesOpenIconView = new ImageView(namesOpenIcon);
        namesOpenIconView.setFitHeight(15);
        namesOpenIconView.setFitWidth(15);

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

        Button configSkeletonButton = new Button();
        configSkeletonButton.setGraphic(configIconView);
        configSkeletonButton.getStyleClass().add("etc");

        VBox configSkeletonVBox = new VBox();
        configSkeletonVBox.setSpacing(8);
        configSkeletonVBox.setAlignment(Pos.TOP_CENTER);
        configSkeletonVBox.getStyleClass().add("configSkeletonVBox");
        configSkeletonVBox.setPadding(new Insets(4, 7, 7, 4));

        Label title = new Label("AntiAFK Config");
        title.getStyleClass().add("title");
        title.setPadding(new Insets(5, 7, 7, 5));
        title.setMinHeight(50);
        title.setPrefHeight(50);
        title.setMaxHeight(50);

        title.setMinWidth(400);
        title.setPrefWidth(40);
        title.setMaxWidth(40);
        title.setAlignment(Pos.CENTER);

        GridPane configSkeletonGridPane = new GridPane();
        configSkeletonGridPane.setMinSize(400, 80);
        configSkeletonGridPane.setPrefSize(400, 80);
        configSkeletonGridPane.setMaxSize(400, 80);
        configSkeletonGridPane.setVgap(8);
        configSkeletonGridPane.setHgap(8);

        configSkeletonGridPane.getStyleClass().add("GridPane-all");

        Label configTimePauseMoveLabel = new Label("Delay before moving (seconds)");
        GridPane.setMargin(configTimePauseMoveLabel, new Insets(9,0,0,10));
        configTimePauseMoveLabel.getStyleClass().add("label-all");

        Spinner<Integer> configTimePauseMoveSpinner = new Spinner<>(0,999999999,10);
        configTimePauseMoveSpinner.setMaxSize(80,35);
        GridPane.setMargin(configTimePauseMoveSpinner, new Insets(10,0,0,0));

        configTimePauseMoveSpinner.setEditable(true);
        configTimePauseMoveSpinner.valueProperty().addListener((ov, oldValue, newValue) -> {
            pauseMove = newValue * 1000;
        });

        configSkeletonGridPane.add(configTimePauseMoveLabel, 0, 0);
        configSkeletonGridPane.add(configTimePauseMoveSpinner, 1, 0);

        configSkeletonVBox.getChildren().addAll(title,configSkeletonGridPane);

        configSkeletonButton.pressedProperty().addListener((obs, oldVal, pressed) -> {
            if (pressed) {
                configIconView.setScaleX(0.9);
                configIconView.setScaleY(0.9);
            } else {
                configIconView.setScaleX(1);
                configIconView.setScaleY(1);
            }
        });

        CustomMenuItem configMenuItem = new CustomMenuItem(configSkeletonVBox, false);
        ContextMenu configContextMenu = new ContextMenu(configMenuItem);


        configSkeletonButton.setOnAction(e -> {
            if(configContextMenu.isShowing()) {
                configContextMenu.hide();
            } else {
                configContextMenu.show(configSkeletonButton, Side.BOTTOM, -405, 5);
            }
        });

        HBox configHBox = new HBox();
        configHBox.setPadding(new Insets(8,0,0,3));
        configHBox.setPickOnBounds(false);

        configHBox.setAlignment(Pos.TOP_LEFT);

        configHBox.getChildren().add(configSkeletonButton);

        Button namesButton = new Button();
        namesButton.setGraphic(namesIconView);
        namesButton.getStyleClass().add("button-all");

        Label namesLabel = new Label("> Vanished (Slowed) by mkl\n> Pure Imagination Orchestra by Adonal Michel");
        namesLabel.setAlignment(Pos.TOP_LEFT);
        namesLabel.setPadding(new Insets(4, 7, 7, 4));

        CustomMenuItem namesItem = new CustomMenuItem(namesLabel, false);
        ContextMenu namesResult = new ContextMenu(namesItem);
        namesResult.getStyleClass().add("names-menu");

        namesButton.setOnAction(e -> {
            if(namesResult.isShowing()) {
                namesResult.hide();
                namesButton.setGraphic(namesIconView);
            } else {
                namesButton.setGraphic(namesOpenIconView);
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
        playAndStopButton.setGraphic(playIconView);
        playAndStopButton.setOnAction(e -> {
            if (!playMusic) {
                playAndStopButton.setGraphic(stopIconView);
                playMusic = true;
                music.play();
            }
            else {
                playAndStopButton.setGraphic(playIconView);
                playMusic = false;
                music.pause();
            }
        });
        playAndStopButton.getStyleClass().add("button-all");

        Button volumeBtn = new Button();
        volumeBtn.setGraphic(volumMedianIconView);
        volumeBtn.getStyleClass().add("button-all");

        Slider volumeSlider = new Slider(0,1,0.5);
        music.volume(0.5);
        volumeSlider.setFocusTraversable(false);
        volumeSlider.setPrefWidth(110);

        volumeSlider.valueProperty().addListener((ov, oldV, newV) -> {
            newVolum = newV;
            if(newV != null){
                music.volume(newV);
            } else {
                music.volume(0.5);
            }
            if(newV.doubleValue() > 0.5) {volumeBtn.setGraphic(volumMaxIconView);}
            if(newV.doubleValue() < 0.5 && newV.doubleValue() > 0.2) {volumeBtn.setGraphic(volumMedianIconView);}
            if(newV.doubleValue() < 0.2 && newV.doubleValue() > 0.0) {volumeBtn.setGraphic(volumLowIconView);}
            if(newV.doubleValue() == 0.0) {volumeBtn.setGraphic(muteMusicIconView);}
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

                if (checkThreadAutoClick || checkPlayButtonAutoClick) {
                    stopAutoClick();
                } else {
                    if (!checkPlayButtonAutoClick) {
                        metodThreadAutoClick();
                        if(antiAFK){
                            metodThreadMove();
                        }
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
            checkPlayButtonAutoClick = true;
            metodThreadAutoClick();
            if(antiAFK){
                metodThreadMove();
            }

        });

        Button stopAutoClickButton = new Button("Stop AutoClick");
        stopAutoClickButton.getStyleClass().add("button-all");
        stopAutoClickButton.setOnAction(event -> {
            stopAutoClick();
        });

        GridPane configGridPane = new GridPane();
        configGridPane.setAlignment(Pos.CENTER);
        configGridPane.setPrefSize(525, 148);
        configGridPane.setMaxSize(525, 148);
        configGridPane.setVgap(8);
        configGridPane.setHgap(20);
        configGridPane.getStyleClass().add("GridPane-all");

        Label repetitionsLabel = new Label("Number of repetitions:");
        repetitionsLabel.getStyleClass().add("label-all");

        // cria um bloco com seta pra aumentar e diminuir os numeros
        Spinner<Integer> repetitionsSpinner = new Spinner<>(0, 999999999, 5000);
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

        Label clickSpeedLabel = new Label("Time of pause between clicks (milliseconds):");
        clickSpeedLabel.getStyleClass().add("label-all");

        Spinner<Integer> clickSpeedSpinner = new Spinner<>(0, 999999999, 6);
        clickSpeedSpinner.setEditable(true);
        clickSpeedSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            clickSpeed = newValue;
        });

        RadioButton antiAFKButton = new RadioButton("AntiAFK");
        antiAFKButton.getStyleClass().add("antiAFK");
        antiAFKButton.setMaxHeight(20);
        antiAFKButton.setOnAction(event -> {
            antiAFK = antiAFKButton.isSelected();
        });

        configGridPane.add(repetitionsLabel, 0, 0);
        configGridPane.add(repetitionsSpinner, 1, 0);
        configGridPane.add(clickSpeedLabel, 0, 1);
        configGridPane.add(clickSpeedSpinner, 1, 1);
        configGridPane.add(timeBeforePlayLabel, 0, 2);
        configGridPane.add(timeBeforePlaySpinner, 1, 2);
        configGridPane.add(antiAFKButton, 0, 3);

        // Adiciona os botoes
        centerVBOX.getChildren().addAll(setKeyButton, playAutoClickButton, stopAutoClickButton, configGridPane);

        rootStackPane.getChildren().addAll(centerVBOX, configHBox, soundHBox,loadingOverlay);
        // scene -> conteudo de dentro da janela, guarda toda a arvore visual
        // Scene(centerVBOX, 400, 400) - > define layout e tamanho
        Scene scene = new Scene(rootStackPane, 550, 550);

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
            robotMove = new Robot();
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
        else {volume(0.5);}
    }
    public void returnMusic() {
        if (playlist == null || playlist.isEmpty()) return;
        // index = (index - 1 + playlist.size()) % playlist.size();
        index = (index - 1) % playlist.size();
        if(index < 0) index = 0;
        playIndex(index);
        if(Main.newVolum != null) {volume(Main.newVolum);}
        else {volume(0.5);}
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