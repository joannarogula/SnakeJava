
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
    static final int DELAY = 80;
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

    int xDirection = UNIT_SIZE;
    int yDirection = 0;
    int aiXDirection = UNIT_SIZE;
    int aiYDirection = 0;

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
        xCoord[0] = random.nextInt((WIDTH / UNIT_SIZE) - 6) * UNIT_SIZE + UNIT_SIZE;
        yCoord[0] = random.nextInt((HEIGHT / UNIT_SIZE) - 6) * UNIT_SIZE + UNIT_SIZE;

        // Ustawienie segmentów ciała węża za głową
        for (int i = 1; i < segments; i++) {
            xCoord[i] = xCoord[0] - i * UNIT_SIZE;
            yCoord[i] = yCoord[0];
        }
    }

    private void initializeAiSnake() {
        // Ustawienie losowej pozycji głowy węża w bezpiecznym zakresie
        aiXCoord[0] = random.nextInt((WIDTH / UNIT_SIZE) - 6) * UNIT_SIZE + UNIT_SIZE;
        aiYCoord[0] = random.nextInt((HEIGHT / UNIT_SIZE) - 6) * UNIT_SIZE + UNIT_SIZE;

        // Ustawienie segmentów ciała węża za głową
        for (int i = 1; i < segments; i++) {
            aiXCoord[i] = aiXCoord[0] - i * UNIT_SIZE;
            aiYCoord[i] = aiYCoord[0];
        }
    }

    private void placeFruits() {
        for (int i = 0; i < ELEMENTS; i++) {
            // Losowo generujemy pozycje dla jabłek
            xFruits[i] = random.nextInt((WIDTH / UNIT_SIZE) - 1) * UNIT_SIZE;
            yFruits[i] = random.nextInt((HEIGHT / UNIT_SIZE) - 1) * UNIT_SIZE;
        }
    }

    private void moveAiSnake() {
        int min = 100;
        int nearestFruit = 0;

        for (int i = 0; i < ELEMENTS; i++) {
            int x = Math.abs(xFruits[i] - aiXCoord[0]);
            int y = Math.abs(yFruits[i] - aiYCoord[0]);

            if ((x + y) < min) {
                min = x + y;
                nearestFruit = i;
            }
        }

        // Preferowane kierunki ruchu do najbliższego owocu
        Direction[] preferredDirections = new Direction[2];
        if (xFruits[nearestFruit] > aiXCoord[0]) {
            preferredDirections[0] = Direction.RIGHT;
        } else if (xFruits[nearestFruit] < aiXCoord[0]) {
            preferredDirections[0] = Direction.LEFT;
        } else {
            preferredDirections[0] = null;
        }

        if (yFruits[nearestFruit] > aiYCoord[0]) {
            preferredDirections[1] = Direction.DOWN;
        } else if (yFruits[nearestFruit] < aiYCoord[0]) {
            preferredDirections[1] = Direction.UP;
        } else {
            preferredDirections[1] = null;
        }

        // Wszystkie możliwe kierunki ruchu
        Direction[] allDirections = { Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT };

        for (Direction preferredDirection : preferredDirections) {
            if (preferredDirection != null) {
                if (tryMove(preferredDirection)) {
                    return;
                }
            }
        }

        for (Direction direction : allDirections) {
            if (direction != preferredDirections[0] && direction != preferredDirections[1]) {
                if (tryMove(direction)) {
                    return;
                }
            }
        }
    }

    private boolean tryMove(Direction directionToCheck) {
        int newXDirection = aiXDirection;
        int newYDirection = aiYDirection;

        switch (directionToCheck) {
            case RIGHT:
                if (aiDirection == Direction.LEFT) return false;
                newXDirection = UNIT_SIZE;
                newYDirection = 0;
                break;
            case LEFT:
                if (aiDirection == Direction.RIGHT) return false;
                newXDirection = -UNIT_SIZE;
                newYDirection = 0;
                break;
            case UP:
                if (aiDirection == Direction.DOWN) return false;
                newXDirection = 0;
                newYDirection = -UNIT_SIZE;
                break;
            case DOWN:
                if (aiDirection == Direction.UP) return false;
                newXDirection = 0;
                newYDirection = UNIT_SIZE;
                break;
        }

        int newX = aiXCoord[0] + newXDirection;
        int newY = aiYCoord[0] + newYDirection;

        // Sprawdzanie kolizji z ciałem węża gracza
        for (int i = 0; i < segments; i++) {
            if (newX == xCoord[i] && newY == yCoord[i]) {
                return false;
            }
        }

        // Sprawdzanie kolizji z ciałem samego węża AI
        for (int i = 0; i < aiSegments; i++) {
            if (newX == aiXCoord[i] && newY == aiYCoord[i]) {
                return false;
            }
        }

        aiDirection = directionToCheck;
        aiXDirection = newXDirection;
        aiYDirection = newYDirection;

        for (int i = aiSegments; i > 0; i--) {
            aiXCoord[i] = aiXCoord[i - 1];
            aiYCoord[i] = aiYCoord[i - 1];
        }

        aiXCoord[0] += aiXDirection;
        aiYCoord[0] += aiYDirection;

        return true;
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
    //   System.out.println("CheckFruit");
        for (int i = 0; i < ELEMENTS; i++) {
            if (xCoord[0] == xFruits[i] && yCoord[0] == yFruits[i]) {
                // Wąż zjadł jabłko, więc zwiększamy długość węża
                segments++;
                // Generujemy nowe położenie jabłka
                xFruits[i] = random.nextInt((WIDTH / UNIT_SIZE) - 1) * UNIT_SIZE;
                yFruits[i] = random.nextInt((HEIGHT / UNIT_SIZE) - 1) * UNIT_SIZE;
                // Zwiększamy wynik (lub robimy coś innego)
                // increaseScore();
            }
            if (aiXCoord[0] == xFruits[i] && aiYCoord[0] == yFruits[i]) {
                // Wąż zjadł jabłko, więc zwiększamy długość węża
                aiSegments++;
                // Generujemy nowe położenie jabłka
                xFruits[i] = random.nextInt((WIDTH / UNIT_SIZE)) * UNIT_SIZE;
                yFruits[i] = random.nextInt((HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
                // Zwiększamy wynik (lub robimy coś innego)
                // increaseScore();
            }
        }
    }

    private void checkCollisions() {
        // Sprawdzanie kolizji z krawędziami planszy
        // Dla weza gracza
        if (xCoord[0] < 0 || xCoord[0] >= WIDTH || yCoord[0] < 0 || yCoord[0] >= HEIGHT) {
            isRunning = false;
            System.out.println("Przegrales");
        }
        // Dla weza AI
        if (aiXCoord[0] < 0 || aiXCoord[0] >= WIDTH || aiYCoord[0] < 0 || aiYCoord[0] >= HEIGHT) {
            isRunning = false;
            System.out.println("Wygrales");
        }

        // Sprawdzanie kolizji z samym sobą
        // Dla weza gracza
        for (int i = segments; i > 0; i--) {
            if ((xCoord[0] == xCoord[i]) && (yCoord[0] == yCoord[i])) {
                isRunning = false;
                System.out.println("Przegrales");
            }
        }
        // Dla weza AI
        for (int i = aiSegments; i > 0; i--) {
            if ((aiXCoord[0] == aiXCoord[i]) && (aiYCoord[0] == aiYCoord[i])) {
                isRunning = false;
                System.out.println("Wygrales");
            }
        }

        // Sprawdzanie kolizji z drugim wezem
        for (int i = aiSegments; i > 0; i--) {
            if ((xCoord[0] == aiXCoord[i]) && (yCoord[0] == aiYCoord[i])) {
                isRunning = false;
                System.out.println("Przegrales");
            }
            if ((aiXCoord[0] == xCoord[i]) && (aiYCoord[0] == yCoord[i])) {
                isRunning = false;
                System.out.println("Wygrales");
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
        initializeAiSnake();
        placeFruits();

        // for (int i = 0; i < aiSegments; i++) {
        //     aiXCoord[i] = WIDTH;
        //     aiYCoord[i] = HEIGHT;
        // }

        // for (int i = 0; i < ELEMENTS; i++) {
        //     xFruits[i] = 0;
        //     yFruits[i] = 500;
        // }

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
            move();
            moveAiSnake();
            checkCollisions();
            checkFruit();
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