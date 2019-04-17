package intermediate_code;

import java.util.*;


public class InterCodeGen {

    List<Quadruple> quadrupleList = new ArrayList<>();
    Map<String, String> tripleMap = new HashMap<>();
    // Name of variable, index in tripleList
    List<Triple> tripleList = new ArrayList<>();

    public InterCodeGen(String[] lines) {
        findQuadruples(lines);
        findTriples(lines);
    }

    private void findTriples(String[] lines){
        for(String oneline : lines){
            String[] splitted = oneline.split("=");
            String lhs = splitted[0].trim();
            String rhs = splitted[1].trim();

            String[] arg_op_arg = rhs.split(" ");
//            System.out.println("Triple RHS:" + Arrays.toString(arg_op_arg));
            String arg1="", op="", arg2="";
            try{
                arg1 = arg_op_arg[0];
                op = arg_op_arg[1];
                arg2 = arg_op_arg[2];
            }catch (IndexOutOfBoundsException e){ }

            //region Store result name in tripleMap, so we can retrieve it's pointed location later on
            String resultIndex = this.tripleList.size()+"";
            this.tripleMap.put(lhs, resultIndex);
            //endregion

            //region Try to get pointer for previous calculation if it exists
            String pointToArg1 = this.tripleMap.get(arg1);
            String pointToArg2 = this.tripleMap.get(arg2);

            if(null == pointToArg1)
                // if previous calculation pointer doesnt exist, just use variable name
                pointToArg1 = arg1;
            else // if previous calculation pointer exists, then show it with ()
                pointToArg1 = "(" + pointToArg1 + ")";

            if(null == pointToArg2)
                pointToArg2 = arg2;
            else
                pointToArg2 = "(" + pointToArg2 + ")";

            //endregion

            this.tripleList.add(new Triple(op, pointToArg1, pointToArg2));

        }
    }

    private void findQuadruples(String[] lines){

        for(String oneline : lines){
            String[]splitted = oneline.trim().split("=");
            String lhs = splitted[0].trim();
            String rhs = splitted[1].trim();

            String[] arg_op_arg = rhs.split(" ");
//            System.out.println("Quad RHS:" + Arrays.toString(arg_op_arg));
            String arg1="", op="", arg2="";
            try{
                arg1 = arg_op_arg[0];
                op = arg_op_arg[1];
                arg2 = arg_op_arg[2];
            }catch (IndexOutOfBoundsException e){ }

            this.quadrupleList.add(new Quadruple(op, arg1, arg2, lhs));
        }

    }
}

// This class only holds 4 Strings
class Quadruple{
    private String op, arg1, arg2, result;

    public Quadruple(String op, String arg1, String arg2, String result) {
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
    }

    @Override
    public String toString() {
        return String.format("| %4s | %4s | %4s | %4s |",
                op, arg1, arg2, result);
    }
}

// This class only holds 3 Strings
class Triple{
    private String op, arg1, arg2;

    public Triple(String op, String arg1, String arg2) {
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    @Override
    public String toString() {
        return String.format("| %4s | %4s | %4s |",
                op, arg1, arg2);
    }
}


/*
INPUT:
a = b
f = c + d
e = a - f
g = b * c
=============================
Quadruples:
=============================
|      |    b |      |    a |
|    + |    c |    d |    f |
|    - |    a |    f |    e |
|    * |    b |    c |    g |
=============================
Triples:
=============================
0 |      |    b |      |
1 |    + |    c |    d |
2 |    - |  (0) |  (1) |
3 |    * |    b |    c |
=============================
=============================
Indirect triples:
=============================
Listing of pointers to triples:
31 |  (0)
32 |  (1)
33 |  (2)
34 |  (3)
Actual triples:
0 |      |    b |      |
1 |    + |    c |    d |
2 |    - |  (0) |  (1) |
3 |    * |    b |    c |
=============================

 */