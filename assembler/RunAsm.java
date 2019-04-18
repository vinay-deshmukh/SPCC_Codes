package assembler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RunAsm {
    public static void main(String[] args) {
        String input = "JOHN START 0\n" +
                "USING *, 15\n" +
                "L 1, FIVE\n" +
                "A 1, FOUR\n" +
                "ST 1, TEMP\n" +
                "FOUR DC F'4'\n" +
                "FIVE DC F'5'\n" +
                "TEMP DS 1F\n" +
                "END";

        // TRIM EACH LINE FOR SPACES
        String[] lines = input.split("\n");

        System.out.println("INPUT:");
        System.out.println(input);

        System.out.println("OUTPUT:");
        Assembler assembler = new Assembler(input);

        System.out.println("PASS 1");
        System.out.println(String.join("\n", assembler.pass1LOC));
        System.out.println();

        System.out.println("PASS 2:");
        System.out.println(String.join("\n", assembler.pass2LOC));
    }
}
/*
INPUT:
JOHN START 0
USING *, 15
L 1, FIVE
A 1, FOUR
ST 1, TEMP
FOUR DC F'4'
FIVE DC F'5'
TEMP DS 1F
END
 */

/*
INPUT:

 */