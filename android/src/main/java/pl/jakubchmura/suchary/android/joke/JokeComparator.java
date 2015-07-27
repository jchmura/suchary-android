package pl.jakubchmura.suchary.android.joke;

import java.util.Comparator;

public class JokeComparator implements Comparator<Joke> {

    @Override
    public int compare(Joke lhs, Joke rhs) {
        return lhs.compareTo(rhs);
    }
}
