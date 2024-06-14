import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * Represents a player-controlled snake in the game.
 * Implements the Runnable interface to allow the snake to run on a separate thread.
 */
public class PlayerSnakeRunnable implements Runnable {
    private final GamePanel gamePanel;
    private final Semaphore semaphore;
    private final Semaphore semaphoreReady;

    private final Random random;
    private final int WIDTH;
    private final int HEIGHT;
    private final int ELEMENTS;
    private final int UNIT_SIZE;

    /**
     * Constructs a PlayerSnakeRunnable object.
     *
     * @param gamePanel      the game panel instance
     * @param semaphore      the semaphore to control thread execution
     * @param semaphoreReady the semaphore to signal thread readiness
     */
    public PlayerSnakeRunnable(GamePanel gamePanel, Semaphore semaphore, Semaphore semaphoreReady) {
        this.gamePanel = gamePanel;
        this.semaphore = semaphore;
        this.semaphoreReady = semaphoreReady;

        this.random = new Random();
        this.WIDTH = GamePanel.WIDTH;
        this.HEIGHT = GamePanel.HEIGHT;
        this.ELEMENTS = GamePanel.ELEMENTS;
        this.UNIT_SIZE = GamePanel.UNIT_SIZE;
    }

    /**
     * The main run method for the player snake thread.
     * Acquires the semaphore, performs movement, fruit checking, and collision checking, then releases the semaphore.
     */
    @Override
    public void run() {
        while (gamePanel.isRunning) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            move();
            checkFruit();
            checkCollisions();

            semaphoreReady.release();
        }
    }

    /**
     * Moves the snake by updating its coordinates based on the current direction.
     */
    private void move() {
        for (int i = gamePanel.segments; i > 0; i--) {
            gamePanel.xCoord[i] = gamePanel.xCoord[i - 1];
            gamePanel.yCoord[i] = gamePanel.yCoord[i - 1];
        }

        gamePanel.xCoord[0] = gamePanel.xCoord[0] + gamePanel.xDirection;
        gamePanel.yCoord[0] = gamePanel.yCoord[0] + gamePanel.yDirection;
    }

    /**
     * Checks if the snake has eaten a fruit.
     * If a fruit is eaten, increases the snake's length and updates the fruit's position.
     */
    public void checkFruit() {
        for (int i = 0; i < ELEMENTS; i++) {
            if (gamePanel.xCoord[0] == gamePanel.xFruits[i] && gamePanel.yCoord[0] == gamePanel.yFruits[i]) {
                gamePanel.segments++;
                gamePanel.eatenObjects++;
                gamePanel.xFruits[i] = random.nextInt((WIDTH / UNIT_SIZE)) * UNIT_SIZE;
                gamePanel.yFruits[i] = random.nextInt((HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
            }
        }

        if (gamePanel.xCoord[0] == gamePanel.xFrog && gamePanel.yCoord[0] == gamePanel.yFrog) {
            gamePanel.segments++;
            gamePanel.eatenObjects++;
            gamePanel.initializeFrog();
        }
    }

    /**
     * Checks if the snake has collided with the game boundaries, itself, or another snake.
     * Sets the player's alive status to false if a collision is detected.
     */
    public void checkCollisions() {
        int xCenter = WIDTH / 2;
        int yCenter = HEIGHT / 2;
        if (gamePanel.xCoord[0] >= xCenter - UNIT_SIZE && gamePanel.xCoord[0] <= xCenter + UNIT_SIZE &&
                gamePanel.yCoord[0] <= yCenter + 15 * UNIT_SIZE && gamePanel.yCoord[0] >= yCenter - 15 * UNIT_SIZE) {
            gamePanel.gamerAlive = false;
        }
        if (gamePanel.xCoord[0] >= xCenter - 15 * UNIT_SIZE && gamePanel.xCoord[0] <= xCenter + 15 * UNIT_SIZE &&
                gamePanel.yCoord[0] <= yCenter + UNIT_SIZE && gamePanel.yCoord[0] >= yCenter - UNIT_SIZE) {
            gamePanel.gamerAlive = false;
        }

        if (gamePanel.xCoord[0] < 0 || gamePanel.xCoord[0] >= WIDTH || gamePanel.yCoord[0] < 0 || gamePanel.yCoord[0] >= HEIGHT) {
            gamePanel.gamerAlive = false;
        }

        for (int i = gamePanel.segments; i > 0; i--) {
            if ((gamePanel.xCoord[0] == gamePanel.xCoord[i]) && (gamePanel.yCoord[0] == gamePanel.yCoord[i])) {
                gamePanel.gamerAlive = false;
            }
        }

        if (gamePanel.ai1Alive) {
            for (int i = gamePanel.ai1Segments; i > 0; i--) {
                if ((gamePanel.xCoord[0] == gamePanel.ai1XCoord[i]) && (gamePanel.yCoord[0] == gamePanel.ai1YCoord[i])) {
                    gamePanel.gamerAlive = false;
                }
            }
        }

        if (gamePanel.ai2Alive) {
            for (int i = gamePanel.ai2Segments; i > 0; i--) {
                if ((gamePanel.xCoord[0] == gamePanel.ai2XCoord[i]) && (gamePanel.yCoord[0] == gamePanel.ai2YCoord[i])) {
                    gamePanel.gamerAlive = false;
                }
            }
        }

        if (gamePanel.xCoord[0] == gamePanel.xFrog && gamePanel.yCoord[0] == gamePanel.yFrog) {
            gamePanel.gamerAlive = false;
        }
    }
}
