package pt.ipleiria.estg.dei.sentinel;

import java.util.Comparator;

public class CompareDates implements Comparator<Value> {
    @Override
    public int compare(Value o1, Value o2) {
        return o1.getDate().compareTo(o2.getDate());
    }
}
