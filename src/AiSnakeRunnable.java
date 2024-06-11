import java.util.Random;

public class AiSnakeRunnable implements Runnable {
    private final GamePanel gamePanel;
    private final Random random;
    private final int aiId;

    public AiSnakeRunnable(GamePanel gamePanel, int aiId) {
        this.gamePanel = gamePanel;
        this.random = new Random();
        this.aiId = aiId;
    }

    @Override
    public void run() {
        // boolean isAlive = false;
        // if(aiId == 1) {
        //     isAlive = gamePanel.isAi1Alive();
        // }
        // else {
        //     isAlive = gamePanel.isAi2Alive();
        // }

        while (gamePanel.isRunning) {
            makeAiMove();
            // move();
            gamePanel.checkAiFruit(aiId);
            gamePanel.checkAiCollisions(aiId);
            // gamePanel.repaint();
            try {
                Thread.sleep(GamePanel.DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // if(aiId == 1) {
            //     isAlive = gamePanel.isAi1Alive();
            // }
            // else {
            //     isAlive = gamePanel.isAi2Alive();
            // }
        }
        gamePanel.repaint();
    }

    private void makeAiMove() {
        int minDistance = Integer.MAX_VALUE;
        Direction newDirection;
        int X;
        int Y;
        int targetX;
        int targetY;

        if(aiId == 1) {
            newDirection = gamePanel.getAi1Direction();
            X = gamePanel.getAi1XCoord(0);
            Y = gamePanel.getAi1YCoord(0);
            targetX = X;
            targetY = Y;
        }
        else {
            newDirection = gamePanel.getAi2Direction();
            X = gamePanel.getAi2XCoord(0);
            Y = gamePanel.getAi2YCoord(0);
            targetX = X;
            targetY = Y;
        }

        // Znajdowanie najbliższego owocu
        for (int i = 0; i < GamePanel.ELEMENTS; i++) {
            int fruitX = gamePanel.getXFruits(i);
            int fruitY = gamePanel.getYFruits(i);
            int distance = Math.abs(X - fruitX) + Math.abs(Y - fruitY);

            if (distance < minDistance) {
                minDistance = distance;
                targetX = fruitX;
                targetY = fruitY;
            }
        }

        for (Direction dir : Direction.values()) {
            int newX = X + dir.getXOffset() * GamePanel.UNIT_SIZE;
            int newY = Y + dir.getYOffset() * GamePanel.UNIT_SIZE;

            // zmiany ----------
            // if (newX >= 0 && newX < GamePanel.WIDTH && newY >= 0 && newY < GamePanel.HEIGHT) {
            //     int distance = Math.abs(newX - gamePanel.getXFruits(0)) + Math.abs(newY - gamePanel.getYFruits(0));
            //     if (distance < minDistance) {
            //         minDistance = distance;
            //         newDirection = dir;
            //     }
            // }

            if (isSafe(newX, newY)) {
                if (Math.abs(newX - targetX) + Math.abs(newY - targetY) < minDistance) {
                    minDistance = Math.abs(newX - targetX) + Math.abs(newY - targetY);
                    newDirection = dir;
                }
            }
            // zmiany ----------
        }

        if(aiId == 1) {
            gamePanel.setAi1Direction(newDirection);
            gamePanel.setAi1XDirection(newDirection.getXOffset() * GamePanel.UNIT_SIZE);
            gamePanel.setAi1YDirection(newDirection.getYOffset() * GamePanel.UNIT_SIZE);

            for (int i = gamePanel.getAi1Segments(); i > 0; i--) {
                gamePanel.setAi1XCoord(i, gamePanel.getAi1XCoord(i - 1));
                gamePanel.setAi1YCoord(i, gamePanel.getAi1YCoord(i - 1));
            }

            gamePanel.setAi1XCoord(0, gamePanel.getAi1XCoord(0) + gamePanel.getAi1XDirection());
            gamePanel.setAi1YCoord(0, gamePanel.getAi1YCoord(0) + gamePanel.getAi1YDirection());
        }
        else {
            gamePanel.setAi2Direction(newDirection);
            gamePanel.setAi2XDirection(newDirection.getXOffset() * GamePanel.UNIT_SIZE);
            gamePanel.setAi2YDirection(newDirection.getYOffset() * GamePanel.UNIT_SIZE);

            for (int i = gamePanel.getAi2Segments(); i > 0; i--) {
                gamePanel.setAi2XCoord(i, gamePanel.getAi2XCoord(i - 1));
                gamePanel.setAi2YCoord(i, gamePanel.getAi2YCoord(i - 1));
            }

            gamePanel.setAi2XCoord(0, gamePanel.getAi2XCoord(0) + gamePanel.getAi2XDirection());
            gamePanel.setAi2YCoord(0, gamePanel.getAi2YCoord(0) + gamePanel.getAi2YDirection());
        }
    }

    // nowa funkcja
    private boolean isSafe(int x, int y) {
        // Sprawdzanie, czy nowa pozycja nie koliduje z granicami pola gry
        if (x < 0 || x >= GamePanel.WIDTH || y < 0 || y >= GamePanel.HEIGHT) {
            return false;
        }

        // Sprawdzanie, czy nowa pozycja nie koliduje z ciałem węża gracza
        for (int i = 0; i < gamePanel.getSegments(); i++) {
            if (x == gamePanel.getXCoord(i) && y == gamePanel.getYCoord(i)) {
                return false;
            }
        }

        // Sprawdzanie, czy nowa pozycja nie koliduje z ciałem węża AI1
        for (int i = 0; i < gamePanel.getAi1Segments(); i++) {
            if (x == gamePanel.getAi1XCoord(i) && y == gamePanel.getAi1YCoord(i)) {
                return false;
            }
        }

        // Sprawdzanie, czy nowa pozycja nie koliduje z ciałem węża AI2
        for (int i = 0; i < gamePanel.getAi2Segments(); i++) {
            if (x == gamePanel.getAi2XCoord(i) && y == gamePanel.getAi2YCoord(i)) {
                return false;
            }
        }

        return true;
    }

    // private void move() {
    //     for (int i = gamePanel.getAiSegments(); i > 0; i--) {
    //         gamePanel.setAiXCoord(i, gamePanel.getAiXCoord(i - 1));
    //         gamePanel.setAiYCoord(i, gamePanel.getAiYCoord(i - 1));
    //     }

    //     gamePanel.setAiXCoord(0, gamePanel.getAiXCoord(0) + gamePanel.getAiXDirection());
    //     gamePanel.setAiYCoord(0, gamePanel.getAiYCoord(0) + gamePanel.getAiYDirection());
    // }
}
