package code_gen;

import java.util.ArrayList;
import java.util.List;

public class CodeGeneration {
    private List<String> machine_code = new ArrayList<>();
    private final String MOV = "MOV %2s, %2s";
    private final String OP  = "OPE %2s, %2s";
    public CodeGeneration(String[] lines) {
        for(String oneline: lines){
            String[] splitted = oneline.trim().split("=");
            String lhs = splitted[0].trim();
            String rhs = splitted[1].trim();
            String[] arg_op_arg = rhs.split(" ");

            if(arg_op_arg.length == 1){
                // Simple copy instruction
                String arg = arg_op_arg[0];
                machine_code.add(String.format(MOV, "R0", arg));
                machine_code.add(String.format(MOV, lhs, "R0"));
            }
            else if(arg_op_arg.length == 3){
                // result = arg1 op arg2
                String arg1 = arg_op_arg[0];
                String op = arg_op_arg[1];
                String arg2 = arg_op_arg[2];

                String operation_str = "";
                switch(op){
                    case "+": operation_str = "ADD"; break;
                    case "-": operation_str = "SUB"; break;
                    case "*": operation_str = "MUL"; break;
                    case "/": operation_str = "DIV"; break;
                    default: throw new RuntimeException("invalid operator");
                }

                machine_code.add(String.format(MOV, "R0", arg1));
                machine_code.add(
                        String.format(
                                OP.replaceAll("OPE", operation_str)
                                , "R0", arg2
                        )
                );
                machine_code.add(String.format(MOV, lhs, "R0"));
            }
            else{
                throw new RuntimeException("RHS is not valid!");
            }
        }
    }

    String getMachineCode(){
        return String.join("\n", machine_code);
    }
}
