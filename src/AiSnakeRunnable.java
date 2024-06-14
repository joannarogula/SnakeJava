import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * Represents the AI-controlled snake's behavior in the game.
 * Implements Runnable to allow the snake to run in its own thread.
 */
public class AiSnakeRunnable implements Runnable {
    private final GamePanel gamePanel;
    private final Random random;
    private final int aiId;
    private final Semaphore semaphore;
    private final Semaphore semaphoreReady;
    private final int WIDTH;
    private final int HEIGHT;
    private final int ELEMENTS;
    private final int UNIT_SIZE;

    /**
     * Constructs an AiSnakeRunnable with the specified game panel, AI identifier, and semaphores.
     *
     * @param gamePanel      the game panel where the snake operates
     * @param aiId           the identifier for the AI snake (1 or 2)
     * @param semaphore      the semaphore used to control AI snake movement
     * @param semaphoreReady the semaphore used to signal the AI snake is ready for the next action
     */
    public AiSnakeRunnable(GamePanel gamePanel, int aiId, Semaphore semaphore, Semaphore semaphoreReady) {
        this.gamePanel = gamePanel;
        this.aiId = aiId;
        this.semaphore = semaphore;
        this.semaphoreReady = semaphoreReady;
        this.random = new Random();

        this.WIDTH = gamePanel.WIDTH;
        this.HEIGHT = gamePanel.HEIGHT;
        this.ELEMENTS = gamePanel.ELEMENTS;
        this.UNIT_SIZE = gamePanel.UNIT_SIZE;
    }

    /**
     * Runs the AI snake's main loop, acquiring the semaphore, making a move, checking for fruit,
     * checking for collisions, and releasing the semaphore.
     */
    @Override
    public void run() {
        while (gamePanel.isRunning) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            makeAiMove();
            checkAiFruit(aiId);
            checkAiCollisions(aiId);

            semaphoreReady.release();
        }
    }

    /**
     * Makes a move for the AI snake, determining the optimal direction based on the nearest fruit.
     */
    private void makeAiMove() {
        int minDistanceFruit = Integer.MAX_VALUE;
        int minDistanceTarget = Integer.MAX_VALUE;
        Direction newDirection;
        int X;
        int Y;
        int targetX;
        int targetY;

        if(aiId == 1) {
            newDirection = gamePanel.ai1Direction;
            X = gamePanel.ai1XCoord[0];
            Y = gamePanel.ai1YCoord[0];
            targetX = X;
            targetY = Y;
        }
        else {
            newDirection = gamePanel.ai2Direction;
            X = gamePanel.ai2XCoord[0];
            Y = gamePanel.ai2YCoord[0];
            targetX = X;
            targetY = Y;
        }

        // Znajdowanie najbliższego owocu
        for (int i = 0; i < GamePanel.ELEMENTS; i++) {
            int fruitX = gamePanel.xFruits[i];
            int fruitY = gamePanel.yFruits[i];
            int distance = Math.abs(X - fruitX) + Math.abs(Y - fruitY);

            if (distance < minDistanceFruit) {
                minDistanceFruit = distance;
                targetX = fruitX;
                targetY = fruitY;
            }
        }

        for (Direction dir : Direction.values()) {
            int newX = X + dir.getXOffset() * GamePanel.UNIT_SIZE;
            int newY = Y + dir.getYOffset() * GamePanel.UNIT_SIZE;

            if (isSafe(newX, newY)) {
                int distance = Math.abs(newX - targetX) + Math.abs(newY - targetY);
                if (distance < minDistanceTarget) {
                    minDistanceTarget = distance;
                    newDirection = dir;
                }
            }
        }

        if(aiId == 1) {
            gamePanel.ai1Direction = newDirection;
            gamePanel.ai1XDirection = newDirection.getXOffset() * GamePanel.UNIT_SIZE;
            gamePanel.ai1YDirection = newDirection.getYOffset() * GamePanel.UNIT_SIZE;

            for (int i = gamePanel.ai1Segments; i > 0; i--) {
                gamePanel.ai1XCoord[i] = gamePanel.ai1XCoord[i - 1];
                gamePanel.ai1YCoord[i] = gamePanel.ai1YCoord[i - 1];
            }

            gamePanel.ai1XCoord[0] = gamePanel.ai1XCoord[0] + gamePanel.ai1XDirection;
            gamePanel.ai1YCoord[0] = gamePanel.ai1YCoord[0] + gamePanel.ai1YDirection;
        } else if (aiId == 2) {
            gamePanel.ai2Direction = newDirection;
            gamePanel.ai2XDirection = newDirection.getXOffset() * GamePanel.UNIT_SIZE;
            gamePanel.ai2YDirection = newDirection.getYOffset() * GamePanel.UNIT_SIZE;

            for (int i = gamePanel.ai2Segments; i > 0; i--) {
                gamePanel.ai2XCoord[i] = gamePanel.ai2XCoord[i - 1];
                gamePanel.ai2YCoord[i] = gamePanel.ai2YCoord[i - 1];
            }

            gamePanel.ai2XCoord[0] = gamePanel.ai2XCoord[0] + gamePanel.ai2XDirection;
            gamePanel.ai2YCoord[0] = gamePanel.ai2YCoord[0] + gamePanel.ai2YDirection;
        }
    }

    /**
     * Checks if the specified coordinates are safe for the AI snake to move to.
     *
     * @param x the x-coordinate to check
     * @param y the y-coordinate to check
     * @return true if the coordinates are safe, false otherwise
     */
    private boolean isSafe(int x, int y) {
        int xCenter = WIDTH / 2;
        int yCenter = HEIGHT / 2;
        if (x >= xCenter - UNIT_SIZE && x <= xCenter + UNIT_SIZE &&
                y <= yCenter + 15 * UNIT_SIZE && y >= yCenter - 15 * UNIT_SIZE) {
            return false;
        }
        if (x >= xCenter - 15 * UNIT_SIZE && x <= xCenter + 15 * UNIT_SIZE &&
                y <= yCenter + UNIT_SIZE && y >= yCenter - UNIT_SIZE) {
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

        return true;
    }

    /**
     * Checks if the AI snake has eaten a fruit, and updates its state accordingly.
     *
     * @param aiId the identifier for the AI snake (1 or 2)
     */
    public void checkAiFruit(int aiId) {
        if (aiId == 1) {
            for (int i = 0; i < ELEMENTS; i++) {
                if (gamePanel.ai1XCoord[0] == gamePanel.xFruits[i] && gamePanel.ai1YCoord[0] == gamePanel.yFruits[i]) {
                    gamePanel.ai1Segments++;
                    gamePanel.xFruits[i] = gamePanel.findX();
                    gamePanel.yFruits[i] = gamePanel.findY();
                }
            }
        } else {
            for (int i = 0; i < ELEMENTS; i++) {
                if (gamePanel.ai2XCoord[0] == gamePanel.xFruits[i] && gamePanel.ai2YCoord[0] == gamePanel.yFruits[i]) {
                    gamePanel.ai2Segments++;
                    gamePanel.xFruits[i] = gamePanel.findX();
                    gamePanel.yFruits[i] = gamePanel.findY();
                }
            }
        }
    }

    /**
     * Checks for collisions between the AI snake and other game elements (field boundaries, player snake, other AI snake).
     *
     * @param aiId the identifier for the AI snake (1 or 2)
     */
    public void checkAiCollisions(int aiId) {
        int xCenter = WIDTH / 2;
        int yCenter = HEIGHT / 2;
        if (aiId == 1) {
            if (gamePanel.ai1XCoord[0] >= xCenter - UNIT_SIZE && gamePanel.ai1XCoord[0] <= xCenter + UNIT_SIZE &&
                    gamePanel.ai1YCoord[0] <= yCenter + 15 * UNIT_SIZE && gamePanel.ai1YCoord[0] >= yCenter - 15 * UNIT_SIZE) {
                gamePanel.ai1Alive = false;
            }
            if (gamePanel.ai1XCoord[0] >= xCenter - 15 * UNIT_SIZE && gamePanel.ai1XCoord[0] <= xCenter + 15 * UNIT_SIZE &&
                    gamePanel.ai1YCoord[0] <= yCenter + UNIT_SIZE && gamePanel.ai1YCoord[0] >= yCenter - UNIT_SIZE) {
                gamePanel.ai1Alive = false;
            }

            if (gamePanel.ai1XCoord[0] < 0 || gamePanel.ai1XCoord[0] >= WIDTH || gamePanel.ai1YCoord[0] < 0 || gamePanel.ai1YCoord[0] >= HEIGHT) {
                gamePanel.ai1Alive = false;
            }

            for (int i = gamePanel.ai1Segments; i > 0; i--) {
                if ((gamePanel.ai1XCoord[0] == gamePanel.ai1XCoord[i]) && (gamePanel.ai1YCoord[0] == gamePanel.ai1YCoord[i])) {
                    gamePanel.ai1Alive = false;
                    break;
                }
            }

            if (gamePanel.ai2Alive) {
                for (int i = gamePanel.ai2Segments; i > 0; i--) {
                    if ((gamePanel.ai1XCoord[0] == gamePanel.ai2XCoord[i]) && (gamePanel.ai1YCoord[0] == gamePanel.ai2YCoord[i])) {
                        gamePanel.ai1Alive = false;
                        break;
                    }
                }
            }

            for (int i = gamePanel.segments; i > 0; i--) {
                if ((gamePanel.ai1XCoord[0] == gamePanel.xCoord[i]) && (gamePanel.ai1YCoord[0] == gamePanel.yCoord[i])) {
                    gamePanel.ai1Alive = false;
                    break;
                }
            }

        } else if (aiId == 2) {
            if (gamePanel.ai2XCoord[0] >= xCenter - UNIT_SIZE && gamePanel.ai2XCoord[0] <= xCenter + UNIT_SIZE &&
                    gamePanel.ai2YCoord[0] <= yCenter + 15 * UNIT_SIZE && gamePanel.ai2YCoord[0] >= yCenter - 15 * UNIT_SIZE) {
                gamePanel.gamerAlive = false;
            }
            if (gamePanel.ai2XCoord[0] >= xCenter - 15 * UNIT_SIZE && gamePanel.ai2XCoord[0] <= xCenter + 15 * UNIT_SIZE &&
                    gamePanel.ai2YCoord[0] <= yCenter + UNIT_SIZE && gamePanel.ai2YCoord[0] >= yCenter - UNIT_SIZE) {
                gamePanel.gamerAlive = false;
            }
            if (gamePanel.ai2XCoord[0] < 0 || gamePanel.ai2XCoord[0] >= WIDTH || gamePanel.ai2YCoord[0] < 0 || gamePanel.ai2YCoord[0] >= HEIGHT) {
                gamePanel.ai2Alive = false;
            }

            for (int i = gamePanel.ai2Segments; i > 0; i--) {
                if ((gamePanel.ai2XCoord[0] == gamePanel.ai2XCoord[i]) && (gamePanel.ai2YCoord[0] == gamePanel.ai2YCoord[i])) {
                    gamePanel.ai2Alive = false;
                    break;
                }
            }

            if (gamePanel.ai1Alive) {
                for (int i = gamePanel.ai1Segments; i > 0; i--) {
                    if ((gamePanel.ai2XCoord[0] == gamePanel.ai1XCoord[i]) && (gamePanel.ai2YCoord[0] == gamePanel.ai1YCoord[i])) {
                        gamePanel.ai2Alive = false;
                        break;
                    }
                }
            }

            for (int i = gamePanel.segments; i > 0; i--) {
                if ((gamePanel.ai2XCoord[0] == gamePanel.xCoord[i]) && (gamePanel.ai2YCoord[0] == gamePanel.yCoord[i])) {
                    gamePanel.ai2Alive = false;
                    break;
                }
            }
        }
    }
}
