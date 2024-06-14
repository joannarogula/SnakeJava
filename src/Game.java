import javax.swing.JFrame;

/**
 * Main class for starting the Snake game.
 */
public class Game {
    /**
     * Main method to create and display the game window.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Utworzenie głównego okna aplikacji
        JFrame frame = new JFrame();
        frame.setTitle("Game - Snake");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        // Utworzenie instancji GamePanel
        GamePanel gamePanel = new GamePanel();
        frame.add(gamePanel);

        // Dopasowanie rozmiaru okna do rozmiaru panelu
        frame.pack();
        frame.setSize(GamePanel.WIDTH, GamePanel.HEIGHT+28);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
