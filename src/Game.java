import javax.swing.JFrame;

public class Game {
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
        frame.setSize(GamePanel.WIDTH, GamePanel.HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
