import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the high scores for the game.
 */
public class HighScoreManager {
    private static final String DATAFILE = "record.dat";
    private List<Integer> highScores;

    /**
     * Constructs a HighScoreManager and loads high scores from the data file.
     */
    public HighScoreManager() {
        loadHighScores();
    }

    /**
     * Loads high scores from the data file.
     * If the file does not exist or an error occurs, initializes an empty high scores list.
     */
    private void loadHighScores() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATAFILE))) {
            highScores = (ArrayList<Integer>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            highScores = new ArrayList<>();
        }
    }

    /**
     * Saves the current high scores to the data file.
     */
    private void saveHighScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATAFILE))) {
            oos.writeObject(highScores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the high scores with a new score.
     * The list is sorted in descending order and truncated to keep only the top 3 scores.
     *
     * @param newScore the new score to add
     */
    public void updateHighScores(int newScore) {
        highScores.add(newScore);
        Collections.sort(highScores, Collections.reverseOrder());
        if (highScores.size() > 3) {
            highScores = new ArrayList<>(highScores.subList(0, 3));
        }
        saveHighScores();
    }

    /**
     * Clears all high scores.
     */
    public void clearHighScores() {
        highScores.clear();
        saveHighScores();
    }

    /**
     * Returns the list of high scores.
     * @return the list of high scores
     */
    public List<Integer> getHighScores() {
        return highScores;
    }
}