import java.util.Random;

public class PlayerSnakeRunnable implements Runnable {
    private final GamePanel gamePanel;
    private final Random random;

    public PlayerSnakeRunnable(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.random = new Random();
    }

    @Override
    public void run() {
        while (gamePanel.isRunning()) {
            move();
            gamePanel.checkFruit();
            gamePanel.checkCollisions();
            gamePanel.repaint();
            try {
                Thread.sleep(GamePanel.DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        gamePanel.repaint();
    }

    private void move() {
        for (int i = gamePanel.getSegments(); i > 0; i--) {
            gamePanel.setXCoord(i, gamePanel.getXCoord(i - 1));
            gamePanel.setYCoord(i, gamePanel.getYCoord(i - 1));
        }

        gamePanel.setXCoord(0, gamePanel.getXCoord(0) + gamePanel.getXDirection());
        gamePanel.setYCoord(0, gamePanel.getYCoord(0) + gamePanel.getYDirection());
    }
}
