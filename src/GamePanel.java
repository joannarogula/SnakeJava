
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener {

    static final int WIDTH = 600;
    static final int HEIGHT = 600;
    static final int UNIT_SIZE = 10;
    static final int ELEMENTS = 5;
    static final int OBSTACLES_NUM = 8;
    static final int GAME_UNITS = (WIDTH * HEIGHT) / UNIT_SIZE;
    static final int DELAY = 80;
    static final String DATAFILE = "record.dat";
    private ArrayList<Integer> highScores = new ArrayList<>();
    
    final int xCoord[] = new int[GAME_UNITS];
    final int yCoord[] = new int[GAME_UNITS];
    final int ai1XCoord[] = new int[GAME_UNITS];
    final int ai1YCoord[] = new int[GAME_UNITS];
    final int ai2XCoord[] = new int[GAME_UNITS];
    final int ai2YCoord[] = new int[GAME_UNITS];

    final int xFruits[] = new int[ELEMENTS];
    final int yFruits[] = new int[ELEMENTS];

    int segments = 6;
    int ai1Segments = 6;
    int ai2Segments = 6;
    int eatenObjects = 0;
    int aiEatenFruits;
    int record;
    int xFrog;
    int yFrog;
    int[][] obstaclesX;
    int[][] obstaclesY;
    boolean isRunning = false;
    // boolean gamerWon = false;
    // boolean aiWon = false;
    boolean gamerAlive = true;
    boolean ai1Alive = true;
    boolean ai2Alive = true;
    Timer timer;
    Random random;
    Direction direction = Direction.RIGHT;
    Direction ai1Direction = Direction.LEFT;
    Direction ai2Direction = Direction.LEFT;
    Direction frogDirection = Direction.LEFT;
    File datafile;

    int xDirection = UNIT_SIZE;
    int yDirection = 0;
    int ai1XDirection = UNIT_SIZE;
    int ai1YDirection = 0;
    int ai2XDirection = UNIT_SIZE;
    int ai2YDirection = 0;

    private Thread ai1Thread;
    private Thread ai2Thread;
    private Thread frogThread;

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
        xCoord[0] = random.nextInt((WIDTH / UNIT_SIZE) - 6) * UNIT_SIZE + UNIT_SIZE;
        yCoord[0] = random.nextInt((HEIGHT / UNIT_SIZE) - 6) * UNIT_SIZE + UNIT_SIZE;

        for (int i = 1; i < segments; i++) {
            xCoord[i] = xCoord[0] - i * UNIT_SIZE;
            yCoord[i] = yCoord[0];
        }
    }

    private void initializeAiSnake() {
        ai1XCoord[0] = random.nextInt((WIDTH / UNIT_SIZE) - 6) * UNIT_SIZE + UNIT_SIZE;
        ai1YCoord[0] = random.nextInt((HEIGHT / UNIT_SIZE) - 6) * UNIT_SIZE + UNIT_SIZE;
        ai2XCoord[0] = random.nextInt((WIDTH / UNIT_SIZE) - 6) * UNIT_SIZE + UNIT_SIZE;
        ai2YCoord[0] = random.nextInt((HEIGHT / UNIT_SIZE) - 6) * UNIT_SIZE + UNIT_SIZE;

        for (int i = 1; i < ai1Segments; i++) {
            ai1XCoord[i] = ai1XCoord[0] - i * UNIT_SIZE;
            ai1YCoord[i] = ai1YCoord[0];
        }
        for (int i = 1; i < ai2Segments; i++) {
            ai2XCoord[i] = ai2XCoord[0] - i * UNIT_SIZE;
            ai2YCoord[i] = ai2YCoord[0];
        }
    }

    private void initializeFrog() {
        xFrog = random.nextInt((WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        yFrog = random.nextInt((HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
    }

    private void placeFruits() {
        for (int i = 0; i < ELEMENTS; i++) {
            xFruits[i] = random.nextInt((WIDTH / UNIT_SIZE) - 1) * UNIT_SIZE;
            yFruits[i] = random.nextInt((HEIGHT / UNIT_SIZE) - 1) * UNIT_SIZE;
        }
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

        loadHighScores();
        initializePlayerSnake();
        initializeAiSnake();
        placeFruits();
        initializeFrog();

        isRunning = true;

        // Start the Player Snake and AI Snake in separate threads
        Thread playerThread = new Thread(new PlayerSnakeRunnable(this));
        playerThread.start();

        ai1Thread = new Thread(new AiSnakeRunnable(this, 1));
        ai1Thread.start();
        ai2Thread = new Thread(new AiSnakeRunnable(this, 2));
        ai2Thread.start();

        frogThread = new Thread(new FrogRunnable(this)); // Uruchomienie wątku żaby
        frogThread.start();
    }

    private void loadHighScores() {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATAFILE))) {
        highScores = (ArrayList<Integer>) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
        highScores = new ArrayList<>();
    }
}

private void saveHighScores() {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATAFILE))) {
        oos.writeObject(highScores);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    private void updateHighScores(int newScore) {
    highScores.add(newScore);
    Collections.sort(highScores, Collections.reverseOrder());
    if (highScores.size() > 3) {
        highScores = new ArrayList<>(highScores.subList(0, 3));
    }
    saveHighScores();
}


    private void showHighScores(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Helvetica", Font.BOLD, 16));
        FontMetrics metrics = getFontMetrics(g.getFont());
        String scoreTitle = "Top 3 Scores:";
        g.drawString(scoreTitle, (WIDTH - metrics.stringWidth(scoreTitle)) / 2, HEIGHT / 2 + 50);

        for (int i = 0; i < highScores.size(); i++) {
            String scoreText = (i + 1) + ": " + highScores.get(i);
            g.drawString(scoreText, (WIDTH - metrics.stringWidth(scoreText)) / 2, HEIGHT / 2 + 70 + (i * 20));
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        if (isRunning) {
            for (int i = 0; i < ELEMENTS; i++) {
                g.setColor(Color.RED);
                g.fillOval(xFruits[i], yFruits[i], UNIT_SIZE, UNIT_SIZE);
            }
            g.setColor(new Color(0, 100, 0));
            g.fillRect(xFrog, yFrog, UNIT_SIZE, UNIT_SIZE);
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

            g.setColor(Color.WHITE);
            g.setFont(new Font("Helvetica", Font.BOLD, 20));
            FontMetrics metrics = getFontMetrics(g.getFont());
            String scoreText = "Score: " + eatenObjects;
            g.drawString(scoreText, (WIDTH - metrics.stringWidth(scoreText)) / 2, g.getFont().getSize());

            Toolkit.getDefaultToolkit().sync();
        } else {
            showGameOver(g);
        }
    }

    private void showGameOver(Graphics g) {
        String finalMessage = gamerAlive ? "You won!" : "AI won";
        // String message = gamerWon ? "You won!" : "Game over";
        // String aiMessage = aiWon ? "AI won!" : "";
        // String finalMessage = aiMessage.isEmpty() ? message : message + " " + aiMessage;

        g.setColor(Color.RED);
        g.setFont(new Font("Helvetica", Font.BOLD, 20));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString(finalMessage, (WIDTH - metrics.stringWidth(finalMessage)) / 2, HEIGHT / 2);

        updateHighScores(eatenObjects);
        showHighScores(g);

        // Dodaj przycisk Restart
        JButton restartButton = new JButton("Restart");
        restartButton.setBounds((WIDTH - 100) / 2, (HEIGHT / 2) + 30, 100, 30);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
            }
        });

        // Dodaj przycisk Exit
        JButton exitButton = new JButton("Exit");
        exitButton.setBounds((WIDTH - 100) / 2, (HEIGHT / 2) + 70, 100, 30);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        this.setLayout(null);
        this.add(restartButton);
        this.add(exitButton);
        this.revalidate();
        this.repaint();
    }

    private void restartGame() {
        this.removeAll();
        segments = 6;
        ai1Segments = 6;
        ai2Segments = 6;
        eatenObjects = 0;
        aiEatenFruits = 0;
        // gamerWon = false;
        // aiWon = false;
        gamerAlive = true;
        ai1Alive = true;
        ai2Alive = true;
        direction = Direction.RIGHT;
        ai1Direction = Direction.LEFT;
        ai2Direction = Direction.LEFT;
        xDirection = UNIT_SIZE;
        yDirection = 0;
        ai1XDirection = UNIT_SIZE;
        ai1YDirection = 0;
        ai2XDirection = UNIT_SIZE;
        ai2YDirection = 0;
        gameStarted = false;
        initializePlayerSnake();
        initializeAiSnake();
        initializeFrog();
        placeFruits();
        isRunning = true;

        Thread playerThread = new Thread(new PlayerSnakeRunnable(this));
        playerThread.start();

        ai1Thread = new Thread(new AiSnakeRunnable(this, 1));
        ai1Thread.start();
        ai2Thread = new Thread(new AiSnakeRunnable(this, 2));
        ai2Thread.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isRunning) {
            repaint();
        }
    }

    public void checkFruit() {
        for (int i = 0; i < ELEMENTS; i++) {
            if (xCoord[0] == xFruits[i] && yCoord[0] == yFruits[i]) {
                segments++;
                xFruits[i] = random.nextInt((WIDTH / UNIT_SIZE)) * UNIT_SIZE;
                yFruits[i] = random.nextInt((HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
                eatenObjects++;
            }
        }

        if (xCoord[0] == xFrog && yCoord[0] == yFrog) {
            segments++;
            initializeFrog();
            eatenObjects++;
        }
    }

    public void checkCollisions() {
        if (xCoord[0] < 0 || xCoord[0] >= WIDTH || yCoord[0] < 0 || yCoord[0] >= HEIGHT) {
            gamerAlive = false;
            // isRunning = false;
            // aiWon = true;
            // System.out.println("Przegrales");
        }

        for (int i = segments; i > 0; i--) {
            if ((xCoord[0] == xCoord[i]) && (yCoord[0] == yCoord[i])) {
                gamerAlive = false;
                // isRunning = false;
                // aiWon = true;
            }
        }

        for (int i = ai1Segments; i > 0; i--) {
            if ((xCoord[0] == ai1XCoord[i]) && (yCoord[0] == ai1YCoord[i])) {
                gamerAlive = false;
                // isRunning = false;
                // aiWon = true;
                // System.out.println("AI won!");
            }
        }

        for (int i = ai2Segments; i > 0; i--) {
            if ((xCoord[0] == ai2XCoord[i]) && (yCoord[0] == ai2YCoord[i])) {
                gamerAlive = false;
                // isRunning = false;
                // aiWon = true;
                // System.out.println("AI won!");
            }
        }

        if (xCoord[0] == xFrog && yCoord[0] == yFrog) {
            gamerAlive = false;
        }

        if (!gamerAlive) {
            isRunning = false;
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
        } else {
            for (int i = 0; i < ELEMENTS; i++) {
                if (ai2XCoord[0] == xFruits[i] && ai2YCoord[0] == yFruits[i]) {
                    ai2Segments++;
                    xFruits[i] = random.nextInt((WIDTH / UNIT_SIZE)) * UNIT_SIZE;
                    yFruits[i] = random.nextInt((HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
                }
            }
        }
    }

    public void checkAiCollisions(int aiId) {
        if (aiId == 1) {
            if (ai1XCoord[0] < 0 || ai1XCoord[0] >= WIDTH || ai1YCoord[0] < 0 || ai1YCoord[0] >= HEIGHT) {
                ai1Alive = false;
                ai1Thread = null;
                // isRunning = false;
                // gamerWon = true;
                // aiWon = true;
                // System.out.println("AI 1!");
            }

            for (int i = ai1Segments; i > 0; i--) {
                if ((ai1XCoord[0] == ai1XCoord[i]) && (ai1YCoord[0] == ai1YCoord[i])) {
                    ai1Alive = false;
                    ai1Thread = null;
                    // isRunning = false;
                    // gamerWon = true;
                    // aiWon = true;
                    // System.out.println("AI 2!");
                }
            }

            for (int i = ai2Segments; i > 0; i--) {
                if ((ai1XCoord[0] == ai2XCoord[i]) && (ai1YCoord[0] == ai2YCoord[i])) {
                    ai1Alive = false;
                    ai1Thread = null;
                }
            }

            for (int i = segments; i > 0; i--) {
                if ((ai1XCoord[0] == xCoord[i]) && (ai1YCoord[0] == yCoord[i])) {
                    ai1Alive = false;
                    ai1Thread = null;
                    // isRunning = false;
                    // gamerWon = true;
                    // aiWon = true;
                    // System.out.println("AI 3!");
                }
            }
        } else {
            if (ai2XCoord[0] < 0 || ai2XCoord[0] >= WIDTH || ai2YCoord[0] < 0 || ai2YCoord[0] >= HEIGHT) {
                ai2Alive = false;
                ai2Thread = null;
                // isRunning = false;
                // gamerWon = true;
                // aiWon = true;
                // System.out.println("AI 1!");
            }

            for (int i = ai2Segments; i > 0; i--) {
                if ((ai2XCoord[0] == ai2XCoord[i]) && (ai2YCoord[0] == ai2YCoord[i])) {
                    ai2Alive = false;
                    ai2Thread = null;
                    // isRunning = false;
                    // gamerWon = true;
                    // aiWon = true;
                    // System.out.println("AI 2!");
                }
            }

            for (int i = ai1Segments; i > 0; i--) {
                if ((ai2XCoord[0] == ai1XCoord[i]) && (ai2YCoord[0] == ai1YCoord[i])) {
                    ai2Alive = false;
                    ai2Thread = null;
                }
            }

            for (int i = segments; i > 0; i--) {
                if ((ai2XCoord[0] == xCoord[i]) && (ai2YCoord[0] == yCoord[i])) {
                    ai2Alive = false;
                    ai2Thread = null;
                    // isRunning = false;
                    // gamerWon = true;
                    // aiWon = true;
                    // System.out.println("AI 3!");
                }
            }
        }

        if (!ai1Alive && !ai2Alive) {
            isRunning = false;
        }
    }

    public int getSegments() {
        return segments;
    }

    public int getXCoord(int index) {
        return xCoord[index];
    }

    public int getYCoord(int index) {
        return yCoord[index];
    }

    public void setXCoord(int index, int value) {
        xCoord[index] = value;
    }

    public void setYCoord(int index, int value) {
        yCoord[index] = value;
    }

    public int getXDirection() {
        return xDirection;
    }

    public int getYDirection() {
        return yDirection;
    }

    public int getXFruits(int index) {
        return xFruits[index];
    }

    public int getYFruits(int index) {
        return yFruits[index];
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getAi1Segments() {
        return ai1Segments;
    }

    public int getAi1XCoord(int index) {
        return ai1XCoord[index];
    }

    public int getAi1YCoord(int index) {
        return ai1YCoord[index];
    }

    public void setAi1XCoord(int index, int value) {
        ai1XCoord[index] = value;
    }

    public void setAi1YCoord(int index, int value) {
        ai1YCoord[index] = value;
    }

    public int getAi1XDirection() {
        return ai1XDirection;
    }

    public int getAi1YDirection() {
        return ai1YDirection;
    }

    public Direction getAi1Direction() {
        return ai1Direction;
    }

    public void setAi1Direction(Direction aiDirection) {
        this.ai1Direction = aiDirection;
    }

    public void setAi1XDirection(int aiXDirection) {
        this.ai1XDirection = aiXDirection;
    }

    public void setAi1YDirection(int aiYDirection) {
        this.ai1YDirection = aiYDirection;
    }

    public int getAi2Segments() {
        return ai2Segments;
    }

    public int getAi2XCoord(int index) {
        return ai2XCoord[index];
    }

    public int getAi2YCoord(int index) {
        return ai2YCoord[index];
    }

    public void setAi2XCoord(int index, int value) {
        ai2XCoord[index] = value;
    }

    public void setAi2YCoord(int index, int value) {
        ai2YCoord[index] = value;
    }

    public int getAi2XDirection() {
        return ai2XDirection;
    }

    public int getAi2YDirection() {
        return ai2YDirection;
    }

    public Direction getAi2Direction() {
        return ai2Direction;
    }

    public void setAi2Direction(Direction aiDirection) {
        this.ai2Direction = aiDirection;
    }

    public void setAi2XDirection(int aiXDirection) {
        this.ai2XDirection = aiXDirection;
    }

    public void setAi2YDirection(int aiYDirection) {
        this.ai2YDirection = aiYDirection;
    }

    public boolean isAi1Alive() {
        return ai1Alive;
    }

    public boolean isAi2Alive() {
        return ai2Alive;
    }
}
