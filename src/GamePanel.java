import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Random;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener {

    static final int WIDTH = 500;
    static final int HEIGHT = 500;
    static final int UNIT_SIZE = 10;
    static final int ELEMENTS = 5;
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
        xCoord[0] = random.nextInt((WIDTH / UNIT_SIZE) - 6) * UNIT_SIZE + UNIT_SIZE;
        yCoord[0] = random.nextInt((HEIGHT / UNIT_SIZE) - 6) * UNIT_SIZE + UNIT_SIZE;

        for (int i = 1; i < segments; i++) {
            xCoord[i] = xCoord[0] - i * UNIT_SIZE;
            yCoord[i] = yCoord[0];
        }
    }

    private void initializeAiSnake() {
        aiXCoord[0] = random.nextInt((WIDTH / UNIT_SIZE) - 6) * UNIT_SIZE + UNIT_SIZE;
        aiYCoord[0] = random.nextInt((HEIGHT / UNIT_SIZE) - 6) * UNIT_SIZE + UNIT_SIZE;

        for (int i = 1; i < segments; i++) {
            aiXCoord[i] = aiXCoord[0] - i * UNIT_SIZE;
            aiYCoord[i] = aiYCoord[0];
        }
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

        initializePlayerSnake();
        initializeAiSnake();
        placeFruits();

        isRunning = true;

        // Start the Player Snake and AI Snake in separate threads
        Thread playerThread = new Thread(new PlayerSnakeRunnable(this));
        playerThread.start();

        Thread aiThread = new Thread(new AiSnakeRunnable(this));
        aiThread.start();
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
            // g.setColor(Color.RED);
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
            for (int i = 0; i < aiSegments; i++) {
                if (i == 0) {
                    g.setColor(Color.BLUE);
                    g.fillRect(aiXCoord[i], aiYCoord[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    g.setColor(new Color(0, 0, 255));
                    g.fillRect(aiXCoord[i], aiYCoord[i], UNIT_SIZE, UNIT_SIZE);
                }
            }
            Toolkit.getDefaultToolkit().sync();
        } else {
            showGameOver(g);
        }
    }

    private void showGameOver(Graphics g) {
        String message = gamerWon ? "You won!" : "Game over";
        String aiMessage = aiWon ? "AI won!" : "";
        String finalMessage = aiMessage.isEmpty() ? message : message + " " + aiMessage;

        g.setColor(Color.RED);
        g.setFont(new Font("Helvetica", Font.BOLD, 20));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString(finalMessage, (WIDTH - metrics.stringWidth(finalMessage)) / 2, HEIGHT / 2);

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
        aiSegments = 6;
        eatenFruits = 0;
        aiEatenFruits = 0;
        gamerWon = false;
        aiWon = false;
        direction = Direction.RIGHT;
        aiDirection = Direction.LEFT;
        xDirection = UNIT_SIZE;
        yDirection = 0;
        aiXDirection = UNIT_SIZE;
        aiYDirection = 0;
        gameStarted = false;
        initializePlayerSnake();
        initializeAiSnake();
        placeFruits();
        isRunning = true;

        Thread playerThread = new Thread(new PlayerSnakeRunnable(this));
        playerThread.start();

        Thread aiThread = new Thread(new AiSnakeRunnable(this));
        aiThread.start();
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
            }
        }
    }

    public void checkCollisions() {
        if (xCoord[0] < 0 || xCoord[0] >= WIDTH || yCoord[0] < 0 || yCoord[0] >= HEIGHT) {
            isRunning = false;
            System.out.println("Przegrales");
        }

        for (int i = segments; i > 0; i--) {
            if ((xCoord[0] == xCoord[i]) && (yCoord[0] == yCoord[i])) {
                isRunning = false;
                System.out.println("Przegrales");
            }
        }

        for (int i = aiSegments; i > 0; i--) {
            if ((xCoord[0] == aiXCoord[i]) && (yCoord[0] == aiYCoord[i])) {
                isRunning = false;
                aiWon = true;
                System.out.println("AI won!");
            }
        }
    }

    public void checkAiFruit() {
        for (int i = 0; i < ELEMENTS; i++) {
            if (aiXCoord[0] == xFruits[i] && aiYCoord[0] == yFruits[i]) {
                aiSegments++;
                xFruits[i] = random.nextInt((WIDTH / UNIT_SIZE)) * UNIT_SIZE;
                yFruits[i] = random.nextInt((HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
            }
        }
    }

    public void checkAiCollisions() {
        if (aiXCoord[0] < 0 || aiXCoord[0] >= WIDTH || aiYCoord[0] < 0 || aiYCoord[0] >= HEIGHT) {
            isRunning = false;
            aiWon = true;
            System.out.println("AI 1!");
        }

        for (int i = aiSegments; i > 0; i--) {
            if ((aiXCoord[0] == aiXCoord[i]) && (aiYCoord[0] == aiYCoord[i])) {
                isRunning = false;
                aiWon = true;
                System.out.println("AI 2!");
            }
        }

        for (int i = segments; i > 0; i--) {
            if ((aiXCoord[0] == xCoord[i]) && (aiYCoord[0] == yCoord[i])) {
                isRunning = false;
                aiWon = true;
                System.out.println("AI 3!");
            }
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

    public int getAiSegments() {
        return aiSegments;
    }

    public int getAiXCoord(int index) {
        return aiXCoord[index];
    }

    public int getAiYCoord(int index) {
        return aiYCoord[index];
    }

    public void setAiXCoord(int index, int value) {
        aiXCoord[index] = value;
    }

    public void setAiYCoord(int index, int value) {
        aiYCoord[index] = value;
    }

    public int getAiXDirection() {
        return aiXDirection;
    }

    public int getAiYDirection() {
        return aiYDirection;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public Direction getAiDirection() {
        return aiDirection;
    }

    public void setAiDirection(Direction aiDirection) {
        this.aiDirection = aiDirection;
    }

    public int getXFruits(int index) {
        return xFruits[index];
    }

    public int getYFruits(int index) {
        return yFruits[index];
    }

    public void setAiXDirection(int aiXDirection) {
        this.aiXDirection = aiXDirection;
    }

    public void setAiYDirection(int aiYDirection) {
        this.aiYDirection = aiYDirection;
    }
}