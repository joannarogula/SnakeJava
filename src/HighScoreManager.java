import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighScoreManager {
    private static final String DATAFILE = "record.dat";
    private List<Integer> highScores;

    public HighScoreManager() {
        loadHighScores();
    }

    private void loadHighScores() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATAFILE))) {
            highScores = (ArrayList<Integer>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            highScores = new ArrayList<>();
        }
    }

    private void saveHighScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATAFILE))) {
            oos.writeObject(highScores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateHighScores(int newScore) {
        highScores.add(newScore);
        Collections.sort(highScores, Collections.reverseOrder());
        if (highScores.size() > 3) {
            highScores = new ArrayList<>(highScores.subList(0, 3));
        }
        saveHighScores();
    }

    public void clearHighScores() {
        highScores.clear();
        saveHighScores();
    }

    public List<Integer> getHighScores() {
        return highScores;
    }
}