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
        return "Production:= " + head + " -> " + String.join(" ",body);
    }
}

class Grammar{
    static void logit(String msg){
        System.out.println(msg);
    }

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
                logit("BEGIN FOLLOW(NT=" + nt + "):");
                Set followNT = followSet(nt);
                mapFollow.get(nt).addAll( followNT );
                logit("END FOLLOW(NT=" + nt + ")= " + mapFollow.get(nt));
                logit("------------");
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

                logit("Symbol: " + symbol + ", " + p);
                if( p.body.indexOf(symbol) == p.body.size() - 1 ){
                    //region A -> Q B then Follow(B) |= Follow(A) except EPS
                    logit(symbol + " is last sym");

                    if(null == followCalled.get(p)){
                        // if symbol doesn't exist in map
                        throw new RuntimeException("P=" + p + "; NT=" + symbol + " doesn't exist in followCalled");
                    }

                    Set<String> followA = new HashSet<>();

                    if( followCalled.get(p) ){
                        // if Follow(followA) has already been called, then skip this iteration
                        logit("Follow(" + symbol + ") already exists!");
                        followA = prevFollow.get(p.head);
                    }
                    else{
                        // Make note that Follow(followA) has been called now
                        followCalled.put(p, true);
                        logit("Follow(" + symbol + ") doesn't exist! FIND");


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
                    logit(symbol + " has followers");

                    int nextSymbolIndex = p.body.indexOf(symbol) + 1;
                    String nextSymbol = p.body.get( nextSymbolIndex );
                    Set firstNext = mapFirst.get(nextSymbol);
                    mapFollow.get(symbol).addAll(firstNext);

                    logit("First(" + nextSymbol + ")=" + mapFirst.get(nextSymbol));

                    while( firstNext.contains(EPS) && (p.body.size()-1 >= nextSymbolIndex ) ){
                        // if First(nextSymbol) contained epsilon, we need to check the symbol after that

                        logit("EPS found! finding next");
                        nextSymbol = p.body.get( nextSymbolIndex );
                        firstNext = mapFirst.get(nextSymbol);
                        mapFollow.get(symbol).addAll(firstNext);
                        logit("First(" + nextSymbol + ")=" + mapFirst.get(nextSymbol));
                        nextSymbolIndex++;
                    }
                    logit("EPS finding finish!");

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

        //region TAKE INPUT
        String aaaaNonTerminals = "E T F E' T'";
        List<String> NonTerminals = Arrays.asList(aaaaNonTerminals.split(" "));

        String aaaaTerminals = "+ * ( ) id";
        List<String> Terminals = Arrays.asList(aaaaTerminals.split(" "));

        String grammar[] = {
                // write productions where each symbol is space separated
                "E = T E'",
                "E' = + T E'",
                "E' = " + Grammar.EPS,
                "T = F T'",
                "T' = * F T'",
                "T' = " + Grammar.EPS,
                "F = ( E )",
                "F = id",
        };

        String startSymbol = "E";
        //endregion


        //region CONVERT INPUT TO PRODUCTIONS
        List<Production> list_productions = new ArrayList<>();
        for(String line : grammar){
            String head = line.split("=")[0].trim();
            String [] body = line.split("=")[1].trim().split(" ");
            list_productions.add(new Production(head, body));
        }
        //endregion

        Grammar g = new Grammar(
                Terminals, NonTerminals, list_productions, startSymbol);

        //region OUTPUTS
        System.out.println("Terminals:" + String.join(",", Terminals) );
        System.out.println("Non Terminals:" + String.join(",", NonTerminals));

        System.out.println("Productions:");
        for(Production p : list_productions)
            System.out.println(p);

        //endregion

        //region FIRST
        System.out.println("\n\n");
        System.out.println("FIRSTS\n");
        for(String s : g.getFirstSets()){
            System.out.println(s);
        }
        //endregion

        //region FOLLOW
        System.out.println("\n\n");
        System.out.println("FOLLOWS\n");
        for(String s : g.getFollowSets()){
            System.out.println(s);
        }
        //endregion

    }
}
