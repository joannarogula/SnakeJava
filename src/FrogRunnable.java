import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * Represents a frog in the game.
 * Implements the Runnable interface to allow the frog to run on a separate thread.
 */
public class FrogRunnable implements Runnable {
    private final GamePanel gamePanel;
    private final Random random;
    private final Semaphore semaphore;
    private final Semaphore semaphoreReady;
    private final int WIDTH;
    private final int HEIGHT;
    private final int ELEMENTS;
    private final int UNIT_SIZE;
    private int currentDirection;
    private int movesInCurrentDirection;
    private static final int MAX_MOVES_IN_ONE_DIRECTION = 10; // liczba ruchów w jednym kierunku


    /**
     * Constructs a FrogRunnable object.
     *
     * @param gamePanel      the game panel instance
     * @param semaphore      the semaphore to control thread execution
     * @param semaphoreReady the semaphore to signal thread readiness
     */
    public FrogRunnable(GamePanel gamePanel, Semaphore semaphore, Semaphore semaphoreReady) {
        this.gamePanel = gamePanel;
        this.random = new Random();
        this.semaphore = semaphore;
        this.semaphoreReady = semaphoreReady;
        this.WIDTH = gamePanel.WIDTH;
        this.HEIGHT = gamePanel.HEIGHT;
        this.ELEMENTS = gamePanel.ELEMENTS;
        this.UNIT_SIZE = gamePanel.UNIT_SIZE;
        this.currentDirection = random.nextInt(4);
        this.movesInCurrentDirection = 0;
        
    }

    /**
     * The main run method for the frog thread.
     * Acquires the semaphore, moves the frog, then releases the semaphore.
     */
    @Override
    public void run() {
        while (gamePanel.isRunning) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            moveFrog();

            semaphoreReady.release();
        }
    }

    /**
     * Moves the frog to a new position.
     * The new position is determined randomly and checked to be free of obstacles.
     */
    private void moveFrog() {
        if (movesInCurrentDirection >= MAX_MOVES_IN_ONE_DIRECTION) {
            currentDirection = random.nextInt(4); // Zmień kierunek
            movesInCurrentDirection = 0;
        }

        int newX = gamePanel.xFrog;
        int newY = gamePanel.yFrog;

        switch (currentDirection) {
            case 0:
                newY -= GamePanel.UNIT_SIZE; // Move up
                break;
            case 1:
                newX += GamePanel.UNIT_SIZE; // Move right
                break;
            case 2:
                newY += GamePanel.UNIT_SIZE; // Move down
                break;
            case 3:
                newX -= GamePanel.UNIT_SIZE; // Move left
                break;
        }

        if (newX >= 0 && newX < GamePanel.WIDTH && newY >= 0 && newY < GamePanel.HEIGHT) {
            gamePanel.xFrog = newX;
            gamePanel.yFrog = newY;
            movesInCurrentDirection++;
        } else {
            // Jeśli nowa pozycja jest poza granicami, zmień kierunek
            currentDirection = random.nextInt(4);
            movesInCurrentDirection = 0;
        }
    }

    /**
     * Checks if a given position is free from obstacles.
     *
     * @param x the x-coordinate to check
     * @param y the y-coordinate to check
     * @return true if the position is free, false otherwise
     */
    private boolean isFree(int x, int y) {
        int xCenter = WIDTH / 2;
        int yCenter = HEIGHT / 2;
        if ((x >= xCenter - UNIT_SIZE && x <= xCenter + UNIT_SIZE &&
                y <= yCenter + 15 * UNIT_SIZE && y >= yCenter - 15 * UNIT_SIZE) ||
                (x >= xCenter - 15 * UNIT_SIZE && x <= xCenter + 15 * UNIT_SIZE &&
                        y <= yCenter + UNIT_SIZE && y >= yCenter - UNIT_SIZE)) {
            return false;
        }

        if (x < 0 || x >= GamePanel.WIDTH || y < 0 || y >= GamePanel.HEIGHT) {
            return false;
        }

        for (int i = 0; i < gamePanel.segments; i++) {
            if (x == gamePanel.xCoord[i] && y == gamePanel.yCoord[i]) {
                return false;
            }
        }

        if (gamePanel.ai1Alive) {
            for (int i = 0; i < gamePanel.ai1Segments; i++) {
                if (x == gamePanel.ai1XCoord[i] && y == gamePanel.ai1YCoord[i]) {
                    return false;
                }
            }
        }

        if (gamePanel.ai2Alive) {
            for (int i = 0; i < gamePanel.ai2Segments; i++) {
                if (x == gamePanel.ai2XCoord[i] && y == gamePanel.ai2YCoord[i]) {
                    return false;
                }
            }
        }

        for (int i = 0; i < GamePanel.ELEMENTS; i++) {
            if (x == gamePanel.xFruits[i] && y == gamePanel.yFruits[i]) {
                return false;
            }
        }
        return true;
    }
}
