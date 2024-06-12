import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Random;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener {

    // ... (pozostała część kodu bez zmian)

    private Thread ai1Thread;
    private Thread ai2Thread;
    private Thread frogThread; // Nowy wątek dla żaby

    private boolean gameStarted = false;

    private void initializeFrog() {
        xFrog = random.nextInt((WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        yFrog = random.nextInt((HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
    }

    public GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);

        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "turnUp");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "turnDown");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "turnLeft");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "turnRight");

        this.getActionMap().put("turnUp", turnUpAction);
        this.getActionMap().put("turnDown", turnDownAction);
        this.getActionMap().put("turnLeft", turnLeftAction);
        this.getActionMap().put("turnRight", turnRightAction);

        initializePlayerSnake();
        initializeAiSnake();
        placeFruits();
        initializeFrog(); // Inicjalizacja żaby

        isRunning = true;

        // Start the Player Snake and AI Snakes in separate threads
        Thread playerThread = new Thread(new PlayerSnakeRunnable(this));
        playerThread.start();

        ai1Thread = new Thread(new AiSnakeRunnable(this, 1));
        ai1Thread.start();
        ai2Thread = new Thread(new AiSnakeRunnable(this, 2));
        ai2Thread.start();

        frogThread = new Thread(new FrogRunnable(this)); // Uruchomienie wątku żaby
        frogThread.start();
    }

    private void draw(Graphics g) {
        if (isRunning) {
            for (int i = 0; i < ELEMENTS; i++) {
                g.setColor(Color.RED);
                g.fillOval(xFruits[i], yFruits[i], UNIT_SIZE, UNIT_SIZE);
            }

            g.setColor(Color.RED);
            g.fillRect(xFrog, yFrog, UNIT_SIZE, UNIT_SIZE); // Rysowanie żaby

            for (int i = 0; i < segments; i++) {
                if (i == 0) {
                    g.setColor(Color.GREEN);
                    g.fillRect(xCoord[i], yCoord[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    g.setColor(new Color(45, 180, 0));
                    g.fillRect(xCoord[i], yCoord[i], UNIT_SIZE, UNIT_SIZE);
                }
            }

            for (int i = 0; i < ai1Segments; i++) {
                if (i == 0) {
                    g.setColor(Color.BLUE);
                    g.fillRect(ai1XCoord[i], ai1YCoord[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    g.setColor(new Color(100, 100, 255));
                    g.fillRect(ai1XCoord[i], ai1YCoord[i], UNIT_SIZE, UNIT_SIZE);
                }
            }

            for (int i = 0; i < ai2Segments; i++) {
                if (i == 0) {
                    g.setColor(Color.YELLOW);
                    g.fillRect(ai2XCoord[i], ai2YCoord[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    g.setColor(new Color(250, 250, 100));
                    g.fillRect(ai2XCoord[i], ai2YCoord[i], UNIT_SIZE, UNIT_SIZE);
                }
            }
            Toolkit.getDefaultToolkit().sync();
        } else {
            showGameOver(g);
        }
    }

    public void checkFruit() {
        for (int i = 0; i < ELEMENTS; i++) {
            if (xCoord[0] == xFruits[i] && yCoord[0] == yFruits[i]) {
                segments++;
                xFruits[i] = random.nextInt((WIDTH / UNIT_SIZE)) * UNIT_SIZE;
                yFruits[i] = random.nextInt((HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
            }
        }

        // Sprawdzenie kolizji z żabą
        if (xCoord[0] == xFrog && yCoord[0] == yFrog) {
            segments++;
            initializeFrog();
        }
    }

    public void checkAiFruit(int aiId) {
        if (aiId == 1) {
            for (int i = 0; i < ELEMENTS; i++) {
                if (ai1XCoord[0] == xFruits[i] && ai1YCoord[0] == yFruits[i]) {
                    ai1Segments++;
                    xFruits[i] = random.nextInt((WIDTH / UNIT_SIZE)) * UNIT_SIZE;
                    yFruits[i] = random.nextInt((HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
                }
            }

            // Sprawdzenie kolizji z żabą
            if (ai1XCoord[0] == xFrog && ai1YCoord[0] == yFrog) {
                ai1Segments++;
                initializeFrog();
            }
        } else {
            for (int i = 0; i < ELEMENTS; i++) {
                if (ai2XCoord[0] == xFruits[i] && ai2YCoord[0] == yFruits[i]) {
                    ai2Segments++;
                    xFruits[i] = random.nextInt((WIDTH / UNIT_SIZE)) * UNIT_SIZE;
                    yFruits[i] = random.nextInt((HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
                }
            }

            // Sprawdzenie kolizji z żabą
            if (ai2XCoord[0] == xFrog && ai2YCoord[0] == yFrog) {
                ai2Segments++;
                initializeFrog();
            }
        }
    }

    public void checkCollisions() {
        if (xCoord[0] < 0 || xCoord[0] >= WIDTH || yCoord[0] < 0 || yCoord[0] >= HEIGHT) {
            gamerAlive = false;
        }

        for (int i = segments; i > 0; i--) {
            if ((xCoord[0] == xCoord[i]) && (yCoord[0] == yCoord[i])) {
                gamerAlive = false;
            }
        }

        for (int i = ai1Segments; i > 0; i--) {
            if ((xCoord[0] == ai1XCoord[i]) && (yCoord[0] == ai1YCoord[i])) {
                gamerAlive = false;
            }
        }

        for (int i = ai2Segments; i > 0; i--) {
            if ((xCoord[0] == ai2XCoord[i]) && (yCoord[0] == ai2YCoord[i])) {
                gamerAlive = false;
            }
        }

        if (xCoord[0] == xFrog && yCoord[0] == yFrog) {
            gamerAlive = false;
        }

        if (!gamerAlive) {
            isRunning = false;
        }
    }

    // ... (pozostała część kodu bez zmian)
}
