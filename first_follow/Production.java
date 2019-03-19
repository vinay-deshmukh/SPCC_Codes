package first_follow;

import java.util.Arrays;
import java.util.List;

public class Production{
    public final String head;
    public final List<String> body;

    public Production(String head, String[] body) {
        this.head = head;
        this.body = Arrays.asList(body);
    }

    @Override
    public String toString() {
        return head + " -> " + String.join(" ",body);
    }
}

