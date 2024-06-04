
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener {

    static final int WIDTH = 500;
    static final int HEIGHT = 500;
    static final int UNIT_SIZE = 10;
    static final int ELEMENTS = 5;
    static final int OBSTACLES_NUM = 8;
    static final int GAME_UNITS = (WIDTH * HEIGHT) / UNIT_SIZE;
    static final int DELAY = 60;
    static final String DATAFILE = "record.dat";

    final int xCoord[] = new int[GAME_UNITS];
    final int yCoord[] = new int[GAME_UNITS];
    final int aiXCoord[] = new int[GAME_UNITS];
    final int aiYCoord[] = new int[GAME_UNITS];

    final int xFruits[] = new int[ELEMENTS];
    final int yFruits[] = new int[ELEMENTS];

    int segments = 6;
    int aiSegments = 6;
    int eatenFruits;
    int aiEatenFruits;
    int record;
    int xFrog;
    int yFrog;
    int[][] obstaclesX;
    int[][] obstaclesY;
    boolean isRunning = false;
    boolean gamerWon = false;
    boolean aiWon = false;
    Timer timer;
    Random random;
    Direction direction = Direction.RIGHT;
    Direction aiDirection = Direction.LEFT;
    Direction frogDirection = Direction.LEFT;
    File datafile;

    int xDirection = UNIT_SIZE;
    int yDirection = 0;

    Action turnUpAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (direction != Direction.DOWN) {
                direction = Direction.UP;
                xDirection = 0;
                yDirection = -UNIT_SIZE;
            }
        }
    };

    Action turnDownAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (direction != Direction.UP) {
                direction = Direction.DOWN;
                xDirection = 0;
                yDirection = UNIT_SIZE;
            }
        }
    };

    Action turnLeftAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (direction != Direction.RIGHT) {
                direction = Direction.LEFT;
                xDirection = -UNIT_SIZE;
                yDirection = 0;
            }
        }
    };

    Action turnRightAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (direction != Direction.LEFT) {
                direction = Direction.RIGHT;
                xDirection = UNIT_SIZE;
                yDirection = 0;
            }
        }
    };

    private void move() {
        for (int i = segments; i > 0; i--) {
            xCoord[i] = xCoord[i - 1];
            yCoord[i] = yCoord[i - 1];
        }

        xCoord[0] += xDirection;
        yCoord[0] += yDirection;
    }



GamePanel() {
        random = new Random();
        record = 0;  // Ustawienie domyślnej wartości rekordu na 0

        datafile = new File(DATAFILE);
        if (datafile.exists()) {
            try (Scanner istream = new Scanner(datafile)) {
                if (istream.hasNextInt()) {
                    record = istream.nextInt();
                }
            } catch (FileNotFoundException e) {
                System.out.println("Cannot open file");
            }
        } else {
            System.out.println("File not found, starting with default record = 0");
        }

        this.setSize(WIDTH, HEIGHT);
        this.setLocation(100, 0);
        this.setBackground(Color.DARK_GRAY);
        this.setFocusable(true);
        this.requestFocusInWindow(true);

        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "turnUp");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "turnDown");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "turnLeft");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "turnRight");

        this.getActionMap().put("turnUp", turnUpAction);
        this.getActionMap().put("turnDown", turnDownAction);
        this.getActionMap().put("turnLeft", turnLeftAction);
        this.getActionMap().put("turnRight", turnRightAction);

        for (int i = 0; i < aiSegments; i++) {
            aiXCoord[i] = WIDTH;
            aiYCoord[i] = HEIGHT;
        }

        for (int i = 0; i < ELEMENTS; i++) {
            xFruits[i] = -1;
            yFruits[i] = -1;
        }

        start();
    }

    public void start() {
        isRunning = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    // @Override
    // public void actionPerformed(ActionEvent e) {
    //     // Kod, który ma się wykonać w odpowiedzi na akcję
    // }
    public void actionPerformed(ActionEvent e) {
    if (isRunning) {
        move();
        // checkCollisions();
        // checkFruit();
    }
    repaint();
}

    @Override
protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (isRunning) {
            // Rysowanie owoców
            g.setColor(Color.RED);
            for (int i = 0; i < ELEMENTS; i++) {
                g.fillRect(xFruits[i], yFruits[i], UNIT_SIZE, UNIT_SIZE);
            }

            // Rysowanie węża gracza
            for (int i = 0; i < segments; i++) {
                if (i == 0) {
                    g.setColor(Color.GREEN); // Głowa węża
                } else {
                    g.setColor(new Color(45, 180, 0)); // Ciało węża
                }
                g.fillRect(xCoord[i], yCoord[i], UNIT_SIZE, UNIT_SIZE);
            }

            // Rysowanie węża AI
            for (int i = 0; i < aiSegments; i++) {
                if (i == 0) {
                    g.setColor(Color.YELLOW); // Głowa węża AI
                } else {
                    g.setColor(new Color(180, 180, 0)); // Ciało węża AI
                }
                g.fillRect(aiXCoord[i], aiYCoord[i], UNIT_SIZE, UNIT_SIZE);
            }
        } else {
            showGameOver(g);
        }
    }

    private void showGameOver(Graphics g) {
        String message = gamerWon ? "You won!" : (aiWon ? "AI won!" : "Game Over");
        g.setColor(Color.RED);
        g.setFont(new Font("Helvetica", Font.BOLD, 40));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString(message, (WIDTH - metrics.stringWidth(message)) / 2, HEIGHT / 2);
    }
};

// import java.awt.*;
// import java.awt.event.*;
// import java.io.File;
// import java.io.FileNotFoundException;
// import java.util.Random;
// import java.util.Scanner;
// import javax.swing.*;

// /**
//  * @class GamePanel
//  * Plansza do gry
//  */

// public class GamePanel extends JPanel implements ActionListener 
// {
//   static final int WIDTH = 500;
//   static final int HEIGHT = 500;
//   static final int UNIT_SIZE = 10;
//   static final int ELEMENTS = 5;
//   static final int OBSTACLES_NUM = 8;
//   static final int GAME_UNITS = (WIDTH * HEIGHT) / UNIT_SIZE;
//   static final int DELAY = 60;
//   static final String DATAFILE = "record.dat";

//   final int xCoord[] = new int[GAME_UNITS];
//   final int yCoord[] = new int[GAME_UNITS];
//   final int aiXCoord[] = new int[GAME_UNITS];
//   final int aiYCoord[] = new int[GAME_UNITS];

//   final int xFruits[] = new int[ELEMENTS];
//   final int yFruits[] = new int[ELEMENTS];

//   int segments = 6;
//   int aiSegments = 6;
//   int eatenFruits;
//   int aiEatenFruits;
//   int record;
//   int xFrog;
//   int yFrog;
//   int[][] obstaclesX;
//   int[][] obstaclesY;
//   boolean isRunning = false;
//   boolean gamerWon = false;
//   boolean aiWon = false;
//   Timer timer;
//   Random random;
//   Direction direction = Direction.RIGHT;
//   Direction aiDirection = Direction.LEFT;
//   Direction frogDirection = Direction.LEFT;
//   File datafile;

//   Action turnUpAction = new AbstractAction() {
//     public void actionPerformed(ActionEvent e) {
//       if (direction != Direction.DOWN) {
//         direction = Direction.UP;
//       }
//     }
//   };

//   Action turnDownAction = new AbstractAction() {
//     public void actionPerformed(ActionEvent e) {
//       if (direction != Direction.UP) {
//         direction = Direction.DOWN;
//       }
//     }
//   };

//   Action turnLeftAction = new AbstractAction() {
//     public void actionPerformed(ActionEvent e) {
//       if (direction != Direction.RIGHT) {
//         direction = Direction.LEFT;
//       }
//     }
//   };

//   Action turnRightAction = new AbstractAction() {
//     public void actionPerformed(ActionEvent e) {
//       if (direction != Direction.LEFT) {
//         direction = Direction.RIGHT;
//       }
//     }
//   };

//   /**
//   * @brief konstruktor - inicjalizuje obsługę klawiszy oraz współrzędne węża AI
//   * Ustawia początkowe współrzędne owoców
//   * Wczytuje z pliku informację o najlepszym wyniku
//   * Wywołuje metodę start()
//   */

//   GamePanel() 
//   {
//     random = new Random();

//     Scanner istream = null;
//     datafile = new File(DATAFILE);
    
//     try {
//       istream = new Scanner(datafile);
//     } catch (FileNotFoundException e) {
//       System.out.println("Cannot open file");
//     }

//     // record = istream.nextInt();

//     this.setSize(WIDTH, HEIGHT);
//     this.setLocation(100, 0);
//     this.setBackground(Color.DARK_GRAY);
//     this.setFocusable(true);
//     this.requestFocusInWindow(true);

//     this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "turnUp");
//     this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "turnDown");
//     this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "turnLeft");
//     this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "turnRight");

//     this.getActionMap().put("turnUp", turnUpAction);
//     this.getActionMap().put("turnDown", turnDownAction);
//     this.getActionMap().put("turnLeft", turnLeftAction);
//     this.getActionMap().put("turnRight", turnRightAction);
  
//     for (int i = 0; i < aiSegments; i++) {
//       aiXCoord[i] = WIDTH;
//       aiYCoord[i] = HEIGHT;
//     }

//     for (int i = 0; i < ELEMENTS; i++) {
//       xFruits[i] = -1;
//       yFruits[i] = -1;
//     }

//     start();
//   }

//   /**
//    * @brief Ustawia początkowe położenie obiektów na planszy
//    * Inicjalizuje timer, na którym oparte jest działanie programu
//    */

//   public void start() {
//     isRunning = true;
//     timer = new Timer(DELAY, this);
//     timer.start();
//   }

//   public void actionPerformed(ActionEvent e) {
//         // Kod, który ma się wykonać w odpowiedzi na akcję
//     }
// }
