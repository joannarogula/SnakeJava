import java.util.Random;
import java.util.concurrent.Semaphore;

public class FrogRunnable implements Runnable {
    private final GamePanel gamePanel;
    private final Random random;
    private final Semaphore semaphore;
    private final Semaphore semaphoreReady;
    private final int WIDTH;
    private final int HEIGHT;
    private final int ELEMENTS;
    private final int UNIT_SIZE;


    public FrogRunnable(GamePanel gamePanel, Semaphore semaphore, Semaphore semaphoreReady) {
        this.gamePanel = gamePanel;
        this.random = new Random();
        this.semaphore = semaphore;
        this.semaphoreReady = semaphoreReady;
        this.WIDTH = gamePanel.WIDTH;
        this.HEIGHT = gamePanel.HEIGHT;
        this.ELEMENTS = gamePanel.ELEMENTS;
        this.UNIT_SIZE = gamePanel.UNIT_SIZE;
    }

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

    private void moveFrog() {
        int X = gamePanel.xFrog;
        int Y = gamePanel.yFrog;
        int newX;
        int newY;

        do {
            int d = random.nextInt(4);
            Direction dir = Direction.values()[d];

            newX = X + dir.getXOffset() * UNIT_SIZE;
            newY = Y + dir.getYOffset() * UNIT_SIZE;
        } while (!isFree(newX, newY));

        gamePanel.xFrog = newX;
        gamePanel.yFrog = newY;
    }

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

        // Sprawdzanie, czy nowa pozycja nie koliduje z ciałem węża AI1
        if (gamePanel.ai1Alive) {
            for (int i = 0; i < gamePanel.ai1Segments; i++) {
                if (x == gamePanel.ai1XCoord[i] && y == gamePanel.ai1YCoord[i]) {
                    return false;
                }
            }
        }

        // Sprawdzanie, czy nowa pozycja nie koliduje z ciałem węża AI2
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
