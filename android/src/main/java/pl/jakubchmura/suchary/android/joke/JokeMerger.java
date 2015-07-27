package pl.jakubchmura.suchary.android.joke;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JokeMerger {

    public static List<Joke> merge(List<Joke> jokes) {
        List<Joke> merged = new ArrayList<>();
        for (Joke joke : jokes) {
            if (!merged.contains(joke)) {
                merged.add(joke);
            } else {
                Joke saved = merged.get(merged.indexOf(joke));
                if (joke.getDate().after(saved.getDate())) {
                    merged.remove(saved);
                    merged.add(joke);
                }
            }
        }
        return merged;
    }

    public static List<Joke> update(List<Joke> oldJokes, List<Joke> newJokes) {
        Set<Joke> updated = new HashSet<>(oldJokes);
        for (Joke joke : newJokes) {
            if (updated.contains(joke)) {
                updated.add(joke);
            }
        }
        return new ArrayList<>(updated);
    }

}
