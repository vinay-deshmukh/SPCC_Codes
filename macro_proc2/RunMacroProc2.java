package macro_proc2;

public class RunMacroProc2 {
    public static void main(String[] args) {
        //region input
        String input = "START\n" +
                "\n" +
                "MACRO\n" +
                "ADD &ARG1, &ARG2\n" +
                "L 1, &ARG1\n" +
                "A 1, &ARG2\n" +
                "MEND\n" +
                "\n" +
                "MACRO\n" +
                "SUB &ARG3, &ARG4\n" +
                "L 1, &ARG3\n" +
                "S 1, &ARG4\n" +
                "MEND\n" +
                "\n" +
                "ADD DATA1, DATA2\n" +
                "SUB DATA1, DATA2\n" +
                "\n" +
                "DATA1 DC F'9'\n" +
                "DATA2 DC F'5'\n" +
                "\n" +
                "END\n" +
                " ";
        //endregion

        MacroProc2 macroProc = new MacroProc2(input);

    }
}
