
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener {

    static final int WIDTH = 500;
    static final int HEIGHT = 500;
    static final int UNIT_SIZE = 10;
    static final int ELEMENTS = 3;
    static final int OBSTACLES_NUM = 8;
    static final int GAME_UNITS = (WIDTH * HEIGHT) / UNIT_SIZE;
    static final int DELAY = 30;
    static final String DATAFILE = "record.dat";

    final int xCoord[] = new int[GAME_UNITS];
    final int yCoord[] = new int[GAME_UNITS];
    final int aiXCoord[] = new int[GAME_UNITS];
    final int aiYCoord[] = new int[GAME_UNITS];

    final int xFruits[] = new int[ELEMENTS];
    final int yFruits[] = new int[ELEMENTS];

    int segments = 6;
    int aiSegments = 6;
    int eatenFruits;
    int aiEatenFruits;
    int record;
    int xFrog;
    int yFrog;
    int[][] obstaclesX;
    int[][] obstaclesY;
    boolean isRunning = false;
    boolean gamerWon = false;
    boolean aiWon = false;
    Timer timer;
    Random random;
    Direction direction = Direction.RIGHT;
    Direction aiDirection = Direction.LEFT;
    Direction frogDirection = Direction.LEFT;
    File datafile;
    private int speed = 3; // Określa, co ile ruchów węża ma być wykonany

    private int moveCounter = 0; // Licznik ruchów węża

    int xDirection = UNIT_SIZE;
    int yDirection = 0;

    private boolean gameStarted = false;

    Action turnUpAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (!gameStarted) {
                gameStarted = true;
            }
            if (direction != Direction.DOWN) {
                direction = Direction.UP;
                xDirection = 0;
                yDirection = -UNIT_SIZE;
            }
        }
    };

    Action turnDownAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (!gameStarted) {
                gameStarted = true;
            }
            if (direction != Direction.UP) {
                direction = Direction.DOWN;
                xDirection = 0;
                yDirection = UNIT_SIZE;
            }
        }
    };

    Action turnLeftAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (!gameStarted) {
                gameStarted = true;
            }
            if (direction != Direction.RIGHT) {
                direction = Direction.LEFT;
                xDirection = -UNIT_SIZE;
                yDirection = 0;
            }
        }
    };

    Action turnRightAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (!gameStarted) {
                gameStarted = true;
            }
            if (direction != Direction.LEFT) {
                direction = Direction.RIGHT;
                xDirection = UNIT_SIZE;
                yDirection = 0;
            }
        }
    };

    private void initializePlayerSnake() {
        // Ustawienie losowej pozycji głowy węża w bezpiecznym zakresie
        xCoord[0] = random.nextInt((WIDTH / UNIT_SIZE) - 2) * UNIT_SIZE + UNIT_SIZE;
        yCoord[0] = random.nextInt((HEIGHT / UNIT_SIZE) - 2) * UNIT_SIZE + UNIT_SIZE;

        // Ustawienie segmentów ciała węża za głową
        for (int i = 1; i < segments; i++) {
            xCoord[i] = xCoord[0] - i * UNIT_SIZE;
            yCoord[i] = yCoord[0];
        }
    }

    private void placeFruits() {
        for (int i = 0; i < ELEMENTS; i++) {
            xFruits[i] = random.nextInt((WIDTH / UNIT_SIZE)) * UNIT_SIZE;
            // Losowo generujemy pozycje dla jabłek
            while (xFruits[i] < 1 || xFruits[i] > WIDTH - 2) {
                xFruits[i] = random.nextInt((WIDTH / UNIT_SIZE)) * UNIT_SIZE;
            }
            yFruits[i] = random.nextInt((HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
            while (yFruits[i] < 1 || yFruits[i] > HEIGHT - 2) {
                yFruits[i] = random.nextInt((HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
            }
        }
    }

    private void move() {
        for (int i = segments; i > 0; i--) {
            xCoord[i] = xCoord[i - 1];
            yCoord[i] = yCoord[i - 1];
        }

        xCoord[0] += xDirection;
        yCoord[0] += yDirection;
    }

    private void checkFruit() {
        for (int i = 0; i < ELEMENTS; i++) {
            if (xCoord[0] == xFruits[i] && yCoord[0] == yFruits[i]) {
                // Wąż zjadł jabłko, więc zwiększamy długość węża
                segments++;
                // Generujemy nowe położenie jabłek

                xFruits[i] = random.nextInt((WIDTH / UNIT_SIZE)) * UNIT_SIZE;
                while (xFruits[i] < 1 || xFruits[i] > WIDTH - 2) {
                    xFruits[i] = random.nextInt((WIDTH / UNIT_SIZE)) * UNIT_SIZE;
                }
                yFruits[i] = random.nextInt((HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
                while (yFruits[i] < 1 || yFruits[i] > HEIGHT - 2) {
                    yFruits[i] = random.nextInt((HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
                }
                // Zwiększamy wynik (lub robimy coś innego)
                // increaseScore();
            }
        }
    }

    private void checkCollisions() {
        // Sprawdzanie kolizji z krawędziami planszy
        if (xCoord[0] < 0 || xCoord[0] >= WIDTH || yCoord[0] < 0 || yCoord[0] >= HEIGHT) {
            isRunning = false;
        }

        // Sprawdzanie kolizji z samym sobą
        for (int i = segments; i > 0; i--) {
            if ((xCoord[0] == xCoord[i]) && (yCoord[0] == yCoord[i])) {
                isRunning = false;
            }
        }

        // Sprawdzanie kolizji z przeszkodami
        // for (int i = 0; i < obstaclesX.length; i++) {
        //     for (int j = 0; j < obstaclesX[i].length; j++) {
        //         if (xCoord[0] == obstaclesX[i][j] && yCoord[0] == obstaclesY[i][j]) {
        //             isRunning = false;
        //         }
        //     }
        // }
        // Zatrzymanie timera, jeśli gra się skończyła
        if (!isRunning) {
            timer.stop();
        }
    }

    GamePanel() {
        random = new Random();
        record = 0;  // Ustawienie domyślnej wartości rekordu na 0

        datafile = new File(DATAFILE);
        if (datafile.exists()) {
            try (Scanner istream = new Scanner(datafile)) {
                if (istream.hasNextInt()) {
                    record = istream.nextInt();
                }
            } catch (FileNotFoundException e) {
                System.out.println("Cannot open file");
            }
        } else {
            System.out.println("File not found, starting with default record = 0");
        }

        this.setSize(WIDTH, HEIGHT);
        this.setLocation(100, 0);
        this.setBackground(Color.DARK_GRAY);
        this.setFocusable(true);
        this.requestFocusInWindow(true);

        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "turnUp");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "turnDown");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "turnLeft");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "turnRight");

        this.getActionMap().put("turnUp", turnUpAction);
        this.getActionMap().put("turnDown", turnDownAction);
        this.getActionMap().put("turnLeft", turnLeftAction);
        this.getActionMap().put("turnRight", turnRightAction);

        initializePlayerSnake();
        placeFruits();

        for (int i = 0; i < aiSegments; i++) {
            aiXCoord[i] = WIDTH;
            aiYCoord[i] = HEIGHT;
        }

        start();
    }

    public void start() {
        isRunning = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    // @Override
    // public void actionPerformed(ActionEvent e) {
    //     // Kod, który ma się wykonać w odpowiedzi na akcję
    // }
    public void actionPerformed(ActionEvent e) {
        if (!gameStarted) {
            return; // Nie wykonuj ruchu, jeśli gra się jeszcze nie rozpoczęła
        }
        if (isRunning) {

            moveCounter++; // Inkrementuj licznik ruchów

            if (moveCounter >= speed) {
                // Wykonaj ruch węża tylko co speed ruchów
                move();
                checkCollisions();
                checkFruit();
                moveCounter = 0; // Zresetuj licznik ruchów
            }
            repaint();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (isRunning) {
            // Rysowanie owoców
            g.setColor(Color.RED);
            for (int i = 0; i < ELEMENTS; i++) {
                g.fillRect(xFruits[i], yFruits[i], UNIT_SIZE, UNIT_SIZE);
            }

            // Rysowanie węża gracza
            for (int i = 0; i < segments; i++) {
                if (i == 0) {
                    g.setColor(Color.GREEN); // Głowa węża
                } else {
                    g.setColor(new Color(45, 180, 0)); // Ciało węża
                }
                g.fillRect(xCoord[i], yCoord[i], UNIT_SIZE, UNIT_SIZE);
            }

            // Rysowanie węża AI
            for (int i = 0; i < aiSegments; i++) {
                if (i == 0) {
                    g.setColor(Color.YELLOW); // Głowa węża AI
                } else {
                    g.setColor(new Color(180, 180, 0)); // Ciało węża AI
                }
                g.fillRect(aiXCoord[i], aiYCoord[i], UNIT_SIZE, UNIT_SIZE);
            }
        } else {
            showGameOver(g);
        }
    }

    private void showGameOver(Graphics g) {
        String message = gamerWon ? "You won!" : (aiWon ? "AI won!" : "Game Over");
        g.setColor(Color.RED);
        g.setFont(new Font("Helvetica", Font.BOLD, 40));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString(message, (WIDTH - metrics.stringWidth(message)) / 2, HEIGHT / 2);
    }
};
