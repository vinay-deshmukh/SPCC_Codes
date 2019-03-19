package parser;
import first_follow.Grammar;
import first_follow.Production;
import java.util.*;

public class Parser {
    public static void main(String[] args) {
        //region take input
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter start symbol:");
        String startSymbol = sc.nextLine();

        System.out.print("Enter non terminals:");
        List<String> NonTerminals = Arrays.asList( sc.nextLine().split(" ") );

        System.out.print("Enter terminals:");
        List<String> Terminals = Arrays.asList( sc.nextLine().split(" ") );

        System.out.print("Enter no of productions:");
        int pn = Integer.parseInt( sc.nextLine() );

        System.out.println("Enter productions:");
        List<Production> list_productions = new ArrayList<>();
        for(int pi = 0; pi < pn; pi++){
            String[] pp = sc.nextLine().trim().split("=");
            String head = pp[0].trim();
            String [] body = pp[1].trim().split(" ");
            list_productions.add(new Production(head, body));
        }
        //endregion

        Grammar g = new Grammar(
                Terminals, NonTerminals, list_productions, startSymbol);

        LL_Parser ll_parser = new LL_Parser(g);
        ll_parser.printParseTable();

        System.out.print("\nEnter string:");
        String input_string = sc.nextLine();
        ll_parser.parseString(input_string);
    }
}
//region INPUT
/*
Input:
E
E T F E' T'
+ * ( ) id
8
E = T E'
E' = + T E'
E' = 9
T = F T'
T' = * F T'
T' = 9
F = ( E )
F = id
id + id * id
 */
//endregion

//region OUTPUT
/*
Enter start symbol:E
Enter non terminals:E T F E' T'
Enter terminals:+ * ( ) id
Enter no of productions:8
Enter productions:
E = T E'
E' = + T E'
E' = 9
T = F T'
T' = * F T'
T' = 9
F = ( E )
F = id
Parsing table
 NonTerminal |            + |            * |            ( |            ) |           id |            $ |
           E |            - |            - |    E -> T E' |            - |    E -> T E' |            - |
           T |            - |            - |    T -> F T' |            - |    T -> F T' |            - |
           F |            - |            - |   F -> ( E ) |            - |      F -> id |            - |
          E' | E' -> + T E' |            - |            - |      E' -> 9 |            - |      E' -> 9 |
          T' |      T' -> 9 | T' -> * F T' |            - |      T' -> 9 |            - |      T' -> 9 |

Enter string:id + id * id
Matched         |          Stack|           Input|                 Action
                |            E $|  id + id * id $|
id              |         T E' $|  id + id * id $|       Output E -> T E'
id              |      F T' E' $|  id + id * id $|       Output T -> F T'
id              |     id T' E' $|  id + id * id $|         Output F -> id
id +            |        T' E' $|     + id * id $|               Match id
id +            |           E' $|     + id * id $|         Output T' -> 9
id +            |       + T E' $|     + id * id $|    Output E' -> + T E'
id + id         |         T E' $|       id * id $|                Match +
id + id         |      F T' E' $|       id * id $|       Output T -> F T'
id + id         |     id T' E' $|       id * id $|         Output F -> id
id + id *       |        T' E' $|          * id $|               Match id
id + id *       |    * F T' E' $|          * id $|    Output T' -> * F T'
id + id * id    |      F T' E' $|            id $|                Match *
id + id * id    |     id T' E' $|            id $|         Output F -> id
id + id * id $  |        T' E' $|               $|               Match id
id + id * id $  |           E' $|               $|         Output T' -> 9
id + id * id $  |              $|               $|         Output E' -> 9

 */
//endregion