package first_follow;

import java.util.*;

class Production{
    final String head;
    final List<String> body;

    public Production(String head, String[] body) {
        this.head = head;
        this.body = Arrays.asList(body);
    }

    @Override
    public String toString() {
        return head + " -> " + String.join(" ",body);
    }
}

class Grammar{

    private List<String> Terminals;
    private List<String> NonTerminals;
    private List<Production> productionList;
    private String startSymbol;

    public static String EPS = "9";
    public static String DOLLAR = "$";

    Map<String, Set<String>> mapFirst = new HashMap<>();
    Map<String, Set<String>> mapFollow = new HashMap<>();

    Map<String, Set<String>> prevFollow = new HashMap<>();
    Map<Production, Boolean> followCalled = new HashMap<>();

    Grammar(
            List<String> terminals,
            List<String> nonterms,
            List<Production> productionList,
            String startSymbol
    ){
        this.Terminals = terminals;
        this.NonTerminals = nonterms;
        this.productionList = productionList;
        this.startSymbol = startSymbol;

        for(String nt: this.NonTerminals){
            // init the maps
            mapFirst.put(nt, new HashSet<>());
            mapFollow.put(nt, new HashSet<>());
        }

        for(String t : this.Terminals){
            // Init First(terminal) = terminal
            mapFirst.put(t, new HashSet<>(Collections.singletonList(t)));
        }
    }

    List<String> getFirstSets(){
        List<String> ans = new ArrayList<>();
        for(String nt : this.NonTerminals){
            ans.add("First(" + nt + ")=" + this.firstSet(nt).toString());
        }
        return ans;
    }

    Set<String> firstSet(String symbol){

        for(Production p : productionList){
            if(p.head.equals(symbol)){
                // For each production of symbol
                //region Add EPS to first set
                if(p.body.contains(EPS)) {
                    mapFirst.get(p.head).add(EPS);
                }
                //endregion
                if(p.body.size() > 0) {
                    //region First(A) = First(P) where A -> PX, so find First(P)
                    // this block also handles productions begin with terminal,
                    // since mapFirst has entries for terminals as well
                    Set<String> f = firstSet(p.body.get(0));
                    // firstSet(EPS) returns null, so we need a null check here
                    if(null != f)
                        mapFirst.get(p.head).addAll(f);
                    //endregion
                }
            }
        }

        return mapFirst.get(symbol);
    }

    List<String> getFollowSets(){
        List<String> ans = new ArrayList<>();
        allFollows(); // Main follow finder function

        for(String nt : this.NonTerminals){
            ans.add("Follow(" + nt + ")=" + mapFollow.get(nt).toString());
        }
        return ans;
    }

    void allFollows(){

        for(String nt : this.NonTerminals){
            // init prevFollow with mapFollow values
            prevFollow.put(nt, new HashSet<>( mapFollow.get(nt) ) ) ;
        }

        for(Production p : this.productionList){
            // Keep track of Follow(production) calls
            followCalled.put(p, false);
        }

        // https://stackoverflow.com/questions/29197332/how-to-find-first-and-follow-sets-of-a-recursive-grammar/29200860#29200860
        while( true ){

            for(String nt : this.NonTerminals){
                Set followNT = followSet(nt);
                mapFollow.get(nt).addAll( followNT );
            }

            // if both are equal, then Follow has been found
            if( areMapsEqual(prevFollow, mapFollow)){
                break;
            }
            // else, update prevFollow with newer values of mapFollow
            for(Map.Entry<String, Set<String>> entry : prevFollow.entrySet()){
                String key = entry.getKey();
                entry.setValue( mapFollow.get(key) );
            }

            // and reset followCalled
            for(Map.Entry<Production, Boolean> entry : followCalled.entrySet()){
                entry.setValue(false);
            }
        }
    }

    boolean areMapsEqual(Map<String, Set<String>> a1, Map<String, Set<String>> a2){
        for(String k1 : a1.keySet()){
            Set s1 = a1.get(k1);
            Set s2 = a2.get(k1);
            // key not found in second map
            if( null == s2  || !s1.equals(s2) ) return false;
        }
        return true;
    }

    Set<String> followSet(String symbol){
        //region Add $ to start symbol
        if(symbol.equals(this.startSymbol))
            mapFollow.get(symbol).add(DOLLAR);
        //endregion

        for(Production p : this.productionList){
            if(p.body.contains(symbol)){

                if( p.body.indexOf(symbol) == p.body.size() - 1 ){
                    //region A -> Q B then Follow(B) |= Follow(A) except EPS

                    if(null == followCalled.get(p)){
                        // if symbol doesn't exist in map
                        throw new RuntimeException("P=" + p + "; NT=" + symbol + " doesn't exist in followCalled");
                    }

                    Set<String> followA = new HashSet<>();

                    if( followCalled.get(p) ){
                        // if Follow(followA) has already been called, then skip this iteration
                        followA = prevFollow.get(p.head);
                    }
                    else{
                        // Make note that Follow(followA) has been called now
                        followCalled.put(p, true);

                        // only try to find follow
                        // if production doesn't begin with current symbol
                        // ie avoid right recursion
                        if(! symbol.equals(p.head))
                            followA = followSet(p.head);

                    }

                    mapFollow.get(symbol).addAll(followA);
                //endregion
                }
                else{
                    //region A -> Q B P then Follow(B) |= First(P) and if EPS in First(P) Follow(B) |= Follow(A) except EPS

                    int nextSymbolIndex = p.body.indexOf(symbol) + 1;
                    String nextSymbol = p.body.get( nextSymbolIndex );
                    Set firstNext = mapFirst.get(nextSymbol);
                    mapFollow.get(symbol).addAll(firstNext);

                    while( firstNext.contains(EPS) && (p.body.size()-1 >= nextSymbolIndex ) ){
                        // if First(nextSymbol) contained epsilon, we need to check the symbol after that

                        nextSymbol = p.body.get( nextSymbolIndex );
                        firstNext = mapFirst.get(nextSymbol);
                        mapFollow.get(symbol).addAll(firstNext);
                        nextSymbolIndex++;
                    }

                    if(nextSymbolIndex == p.body.size()){
                        // eps was found in all followers
                        // so now find Follow() of LNT
                        mapFollow.get(symbol).addAll(mapFollow.get(p.head));
                    }
                    //endregion
                }
            }
        }
        // Remove EPS
        mapFollow.get(symbol).remove(EPS);
        return mapFollow.get(symbol);
    }
}

public class FirstFollow {

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

        //region FIRST
        System.out.println("FIRSTS\n");
        System.out.println(String.join("\n", g.getFirstSets()));
        //endregion

        //region FOLLOW
        System.out.println("FOLLOWS\n");
        System.out.println(String.join("\n", g.getFollowSets()));
        //endregion
    }
}
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
 */