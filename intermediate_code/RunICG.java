package intermediate_code;

public class RunICG {
    public static void main(String[] args) {
        String input = "a = b\n" +
                "f = c + d\n" +
                "e = a - f\n" +
                "g = b * c";
        String[] lines = input.split("\n");
        InterCodeGen icg = new InterCodeGen(lines);

        System.out.println("INPUT:");
        System.out.println(input);

        String delimiter = "=============================";

        //region Quadruples
        System.out.println(delimiter);
        System.out.println("Quadruples:");
        System.out.println(delimiter);
        for(Quadruple q: icg.quadrupleList){
            System.out.println(q);
        }
        //endregion

        //region Triples
        System.out.println(delimiter);
        System.out.println("Triples:");
        System.out.println(delimiter);
        for(int ti=0; ti< icg.tripleList.size(); ti++){
            System.out.println(ti + " " + icg.tripleList.get(ti).toString());
        }
        System.out.println(delimiter);
        //endregion

        //region Indirect triples
        System.out.println(delimiter);
        System.out.println("Indirect triples:");
        System.out.println(delimiter);
        System.out.println("Listing of pointers to triples:");
        for(int pi=31, ti=0; ti<icg.tripleList.size(); ti++, pi++){
            System.out.printf("%2d | %4s\n",
                    pi,
                    "(" + ti + ")"
            );
        }
        System.out.println("Actual triples:");
        for(int ti=0; ti< icg.tripleList.size(); ti++){
            System.out.println(ti + " " + icg.tripleList.get(ti).toString());
        }
        System.out.println(delimiter);
        //endregion

    }
}
