import java.util.Random;

public class FrogRunnable implements Runnable {
    private final GamePanel gamePanel;
    private final Random random;
    private int currentDirection;
    private int movesInCurrentDirection;
    private static final int MAX_MOVES_IN_ONE_DIRECTION = 10; // liczba ruchów w jednym kierunku

    public FrogRunnable(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.random = new Random();
        this.currentDirection = random.nextInt(4);
        this.movesInCurrentDirection = 0;
    }

    @Override
    public void run() {
        while (gamePanel.isRunning()) {
            moveFrog();
            gamePanel.repaint();
            try {
                Thread.sleep(GamePanel.DELAY * 3); // Żaba porusza się co dwa razy wolniej niż węże
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void moveFrog() {
        if (movesInCurrentDirection >= MAX_MOVES_IN_ONE_DIRECTION) {
            changeDirection();
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

        // Sprawdzanie, czy nowa pozycja żaby jest w granicach planszy
        if (isPositionValid(newX, newY)) {
            gamePanel.xFrog = newX;
            gamePanel.yFrog = newY;
            movesInCurrentDirection++;
        } else {
            // Jeśli nowa pozycja jest poza granicami, zmień kierunek
            changeDirection();
        }
    }

    private boolean isPositionValid(int x, int y) {
        return x >= 0 && x < GamePanel.WIDTH && y >= 0 && y < GamePanel.HEIGHT;
    }

    private void changeDirection() {
        currentDirection = random.nextInt(4);
    }
}
