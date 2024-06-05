import java.util.Random;

public class AiSnakeRunnable implements Runnable {
    private final GamePanel gamePanel;
    private final Random random;

    public AiSnakeRunnable(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.random = new Random();
    }

    @Override
    public void run() {
        while (gamePanel.isRunning()) {
            makeAiMove();
            gamePanel.moveAiSnake();
            gamePanel.checkAiFruit();
            gamePanel.checkAiCollisions();
            try {
                Thread.sleep(GamePanel.DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    

    private void makeAiMove() {
        Direction newDirection = gamePanel.getAiDirection();
        int minDistance = Integer.MAX_VALUE;

        for (Direction dir : Direction.values()) {
            int newX = gamePanel.getAiXCoord(0) + dir.getXOffset() * GamePanel.UNIT_SIZE;
            int newY = gamePanel.getAiYCoord(0) + dir.getYOffset() * GamePanel.UNIT_SIZE;

            if (newX >= 0 && newX < GamePanel.WIDTH && newY >= 0 && newY < GamePanel.HEIGHT) {
                int distance = Math.abs(newX - gamePanel.getXFruits(0)) + Math.abs(newY - gamePanel.getYFruits(0));
                if (distance < minDistance) {
                    minDistance = distance;
                    newDirection = dir;
                }
            }
        }

        gamePanel.setAiDirection(newDirection);
        gamePanel.setAiXDirection(newDirection.getXOffset() * GamePanel.UNIT_SIZE);
        gamePanel.setAiYDirection(newDirection.getYOffset() * GamePanel.UNIT_SIZE);
    }
}
