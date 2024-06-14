import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import javax.swing.*;

/**
 * GamePanel represents the main game panel where the snake game is rendered and controlled.
 */
public class GamePanel extends JPanel implements ActionListener {
    static final int WIDTH = 600;
    static final int HEIGHT = 600;
    static final int UNIT_SIZE = 10;
    static final int ELEMENTS = 5;
    static final int GAME_UNITS = (WIDTH * HEIGHT) / UNIT_SIZE;
    static final int DELAY = 100;

    int[] xCoord = new int[GAME_UNITS];
    int[] yCoord = new int[GAME_UNITS];
    int[] ai1XCoord = new int[GAME_UNITS];
    int[] ai1YCoord = new int[GAME_UNITS];
    int[] ai2XCoord = new int[GAME_UNITS];
    int[] ai2YCoord = new int[GAME_UNITS];

    int[] xFruits = new int[ELEMENTS];
    int[] yFruits = new int[ELEMENTS];

    int segments = 6;
    int ai1Segments = 6;
    int ai2Segments = 6;
    int eatenObjects = 0;
    int aiEatenFruits;
    int xFrog;
    int yFrog;
    boolean isRunning = false;
    boolean gamerAlive = true;
    boolean ai1Alive = true;
    boolean ai2Alive = true;
    Timer timer;
    Random random;
    Direction direction = Direction.RIGHT;
    Direction ai1Direction = Direction.LEFT;
    Direction ai2Direction = Direction.LEFT;

    int xDirection = UNIT_SIZE;
    int yDirection = 0;
    int ai1XDirection = UNIT_SIZE;
    int ai1YDirection = 0;
    int ai2XDirection = UNIT_SIZE;
    int ai2YDirection = 0;

    boolean gameStarted = false;

    private Thread playerThread;
    private Thread ai1Thread;
    private Thread ai2Thread;
    private Thread frogThread;

    Semaphore playerSemaphore = new Semaphore(0);
    Semaphore semaphoreReady = new Semaphore(0);
    Semaphore ai1Semaphore = new Semaphore(0);
    Semaphore ai2Semaphore = new Semaphore(0);
    Semaphore frogSemaphore = new Semaphore(0);

    HighScoreManager manager = new HighScoreManager();
    List<Integer> highScores = manager.getHighScores();
    List<Point> obstacle = new ArrayList<>();

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

    /**
     * Initializes the player's snake with its starting position and segments.
     */
    private void initializePlayerSnake() {
        xCoord[0] = findX();
        yCoord[0] = findY();

        for (int i = 1; i < segments; i++) {
            xCoord[i] = xCoord[0] - i * UNIT_SIZE;
            yCoord[i] = yCoord[0];
        }
    }

    /**
     * Initializes the AI snakes with their starting positions and segments.
     */
    private void initializeAiSnake() {
        ai1XCoord[0] = findX();
        ai1YCoord[0] = findY();
        ai2XCoord[0] = findX();
        ai2YCoord[0] = findY();

        for (int i = 1; i < ai1Segments; i++) {
            ai1XCoord[i] = ai1XCoord[0] - i * UNIT_SIZE;
            ai1YCoord[i] = ai1YCoord[0];
        }
        for (int i = 1; i < ai2Segments; i++) {
            ai2XCoord[i] = ai2XCoord[0] - i * UNIT_SIZE;
            ai2YCoord[i] = ai2YCoord[0];
        }
    }

    /**
     * Initializes the frog with a random position on the game board.
     */
    public void initializeFrog() {
        xFrog = findX();
        yFrog = findY();
    }

    /**
     * Places fruits randomly on the game board.
     */
    private void placeFruits() {
        for (int i = 0; i < ELEMENTS; i++) {
            xFruits[i] = findX();
            yFruits[i] = findY();
        }
    }

    /**
     * Finds a random x-coordinate within the game board boundaries, avoiding the central obstacle area.
     * @return a valid x-coordinate
     */
    private int findX() {
        int x;
        int xCenter = WIDTH / 2;

        do {
            x = random.nextInt((WIDTH / UNIT_SIZE) - 1) * UNIT_SIZE;
        } while ((x >= xCenter - UNIT_SIZE && x <= xCenter + UNIT_SIZE) ||
                (x >= xCenter - 15 * UNIT_SIZE && x <= xCenter + 15 * UNIT_SIZE));

        return x;
    }

    /**
     * Finds a random y-coordinate within the game board boundaries, avoiding the central obstacle area.
     * @return a valid y-coordinate
     */
    private int findY() {
        int y;
        int yCenter = HEIGHT / 2;

        do {
            y = random.nextInt((HEIGHT / UNIT_SIZE) - 1) * UNIT_SIZE;
        } while ((y <= yCenter + 15 * UNIT_SIZE && y >= yCenter - 15 * UNIT_SIZE) ||
                (y <= yCenter + UNIT_SIZE && y >= yCenter - UNIT_SIZE));

        return y;
    }

    /**
     * Places a cross-shaped obstacle in the center of the game board.
     */
    private void placeObstacle() {
        int size = 15;
        int centerX = WIDTH / 2;
        int centerY = HEIGHT / 2;

        obstacle.add(new Point(centerX, centerY - size * UNIT_SIZE));
        obstacle.add(new Point(centerX + UNIT_SIZE, centerY - size * UNIT_SIZE));
        for( int i = size; i > 0; i-- ) {
            obstacle.add(new Point(centerX + UNIT_SIZE, centerY - i * UNIT_SIZE));
        }
        for( int i = size; i > 0; i-- ) {
            obstacle.add(new Point(centerX + i * UNIT_SIZE, centerY - UNIT_SIZE));
        }
        obstacle.add(new Point(centerX + size * UNIT_SIZE, centerY));
        for( int i = size; i > 0; i-- ) {
            obstacle.add(new Point(centerX + i * UNIT_SIZE, centerY + UNIT_SIZE));
        }
        obstacle.add(new Point(centerX, centerY + size * UNIT_SIZE));
        for( int i = size; i > 0; i-- ) {
            obstacle.add(new Point(centerX + UNIT_SIZE, centerY + i * UNIT_SIZE));
        }
        obstacle.add(new Point(centerX - size * UNIT_SIZE, centerY));
        for( int i = size; i > 0; i-- ) {
            obstacle.add(new Point(centerX - UNIT_SIZE, centerY + i * UNIT_SIZE));
        }
        for( int i = size; i > 0; i-- ) {
            obstacle.add(new Point(centerX + i * UNIT_SIZE, centerY + UNIT_SIZE));
        }
        for( int i = size; i > 0; i-- ) {
            obstacle.add(new Point(centerX - i * UNIT_SIZE, centerY + UNIT_SIZE));
        }
        for( int i = size; i > 0; i-- ) {
            obstacle.add(new Point(centerX - i * UNIT_SIZE, centerY - UNIT_SIZE));
        }
        for( int i = size; i > 0; i-- ) {
            obstacle.add(new Point(centerX - UNIT_SIZE, centerY - i * UNIT_SIZE));
        }
    }

    /**
     * Constructor for GamePanel. Initializes the game board, player and AI snakes, fruits, and frog.
     * Sets up key bindings for controlling the player snake.
     * Starts the game timer and threads for player, AI snakes, and frog.
     */
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

        placeObstacle();
        initializePlayerSnake();
        initializeAiSnake();
        placeFruits();
        initializeFrog();

        isRunning = true;

        playerThread = new Thread(new PlayerSnakeRunnable(this, playerSemaphore, semaphoreReady));
        ai1Thread = new Thread(new AiSnakeRunnable(this, 1, ai1Semaphore, semaphoreReady));
        ai2Thread = new Thread(new AiSnakeRunnable(this, 2, ai2Semaphore, semaphoreReady));
        frogThread = new Thread(new FrogRunnable(this, frogSemaphore, semaphoreReady));

        playerThread.start();
        ai1Thread.start();
        ai2Thread.start();
        frogThread.start();

        timer = new Timer(DELAY, this);
        timer.start();
    }

    /**
     * Main game loop. Updates game state at each timer tick and start threads.
     * @param e the ActionEvent triggered by the timer
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (isRunning) {
            if (ai1Alive && ai2Alive) {
                playerSemaphore.release();
                ai1Semaphore.release();
                ai2Semaphore.release();
                frogSemaphore.release();

                try {
                    semaphoreReady.acquire(4);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            else if (ai1Alive) {
                ai2Thread = null;

                playerSemaphore.release();
                ai1Semaphore.release();
                frogSemaphore.release();

                try {
                    semaphoreReady.acquire(3);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            else if (ai2Alive) {
                ai1Thread = null;

                playerSemaphore.release();
                ai2Semaphore.release();
                frogSemaphore.release();

                try {
                    semaphoreReady.acquire(3);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            else {
                manager.updateHighScores(eatenObjects);
                isRunning = false;
            }
            if (!gamerAlive) {
                manager.updateHighScores(eatenObjects);
                isRunning = false;
            }

            repaint();
        }
    }

    /**
     * Displays the top 3 results from the file.
     * @param g the Graphics object used for drawing
     */
    private void showHighScores(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Helvetica", Font.BOLD, 30));
        FontMetrics metrics = getFontMetrics(g.getFont());
        String scoreTitle = "Top 3 scores:";
        g.drawString(scoreTitle, (WIDTH - metrics.stringWidth(scoreTitle)) / 2, HEIGHT / 2 + 100);

        highScores = manager.getHighScores();

        for (int i = 0; i < highScores.size(); i++) {
            String scoreText = (i + 1) + ": " + highScores.get(i);
            g.drawString(scoreText, (WIDTH - metrics.stringWidth(scoreText)) / 2, HEIGHT / 2 + 140 + (i * 40));
        }
    }

    /**
     * Paints the game board, including snakes, fruits, frog, and obstacle.
     * Also displays the current score and game over screen if applicable.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /**
     * Draws the game elements (snakes, fruits, frog, obstacle) and score on the game board.
     * @param g the Graphics object used for drawing
     */
    private void draw(Graphics g) {
        if (isRunning) {
            g.setColor(Color.GRAY);
            for (Point obstacle : obstacle) {
                g.fillRect(obstacle.x, obstacle.y, UNIT_SIZE, UNIT_SIZE);
            }
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
            if (ai1Alive) {
                for (int i = 0; i < ai1Segments; i++) {
                    if (i == 0) {
                        g.setColor(Color.BLUE);
                        g.fillRect(ai1XCoord[i], ai1YCoord[i], UNIT_SIZE, UNIT_SIZE);
                    } else {
                        g.setColor(new Color(100, 100, 255));
                        g.fillRect(ai1XCoord[i], ai1YCoord[i], UNIT_SIZE, UNIT_SIZE);
                    }
                }
            }
            if (ai2Alive) {
                for (int i = 0; i < ai2Segments; i++) {
                    if (i == 0) {
                        g.setColor(Color.YELLOW);
                        g.fillRect(ai2XCoord[i], ai2YCoord[i], UNIT_SIZE, UNIT_SIZE);
                    } else {
                        g.setColor(new Color(250, 250, 100));
                        g.fillRect(ai2XCoord[i], ai2YCoord[i], UNIT_SIZE, UNIT_SIZE);
                    }
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

    /**
     * Displays the screen after the game: game result, buttons and top scores.
     */
    private void showGameOver(Graphics g) {
        String finalMessage = gamerAlive ? "You won!" : "You lost!";

        g.setColor(Color.RED);
        g.setFont(new Font("Helvetica", Font.BOLD, 50));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString(finalMessage, (WIDTH - metrics.stringWidth(finalMessage)) / 2, 100);

        showHighScores(g);

        JButton restartButton = new JButton("Restart");
        restartButton.setBounds((WIDTH - 100) / 2, 150, 100, 30);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
            }
        });

        JButton exitButton = new JButton("Exit");
        exitButton.setBounds((WIDTH - 100) / 2, 180, 100, 30);
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

    /**
     * Restarts the game, updating the variables to the initial game state.
     */
    private void restartGame() {
        this.removeAll();
        segments = 6;
        ai1Segments = 6;
        ai2Segments = 6;
        eatenObjects = 0;
        aiEatenFruits = 0;
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

        playerThread = new Thread(new PlayerSnakeRunnable(this, playerSemaphore, semaphoreReady));
        ai1Thread = new Thread(new AiSnakeRunnable(this, 1, ai1Semaphore, semaphoreReady));
        ai2Thread = new Thread(new AiSnakeRunnable(this, 2, ai2Semaphore, semaphoreReady));
        frogThread = new Thread(new FrogRunnable(this, frogSemaphore, semaphoreReady));

        playerThread.start();
        ai1Thread.start();
        ai2Thread.start();
        frogThread.start();
    }
}
