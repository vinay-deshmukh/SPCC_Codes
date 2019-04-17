package code_gen;

public class RunCodeGen {
    public static void main(String[] args) {
        String input = "a = b\n" +
                "f = c + d\n" +
                "e = a - f\n" +
                "g = b * c\n";
        String[] lines = input.split("\n");

        System.out.println("INPUT:");
        System.out.println(input);
        System.out.println("MACHINE CODE:");
        System.out.println(new CodeGeneration(lines).getMachineCode());
    }
}
/*
INPUT:
a = b
f = c + d
e = a - f
g = b * c

MACHINE CODE:
MOV R0,  b
MOV  a, R0
MOV R0,  c
ADD R0,  d
MOV  f, R0
MOV R0,  a
SUB R0,  f
MOV  e, R0
MOV R0,  b
MUL R0,  c
MOV  g, R0
 */