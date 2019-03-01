package first_follow;

import java.util.*;

class Production{
    String head;
    List<String> body;

    public Production(String head, String[] body) {
        this.head = head;
        this.body = Arrays.asList(body);
    }

    @Override
    public String toString() {
        return "Production{" +
                "head='" + head + '\'' +
                ", body=" + body +
                '}';
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
    Map<String, Boolean> followCalled = new HashMap<>();

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
            Set<String> tt = new HashSet<>();
            tt.add(t);
            mapFirst.put(t, tt);
        }

    }

    List<String> getFirstSets(){
        List<String> ans = new ArrayList<>();
        for(String nt : this.NonTerminals){
//            System.out.println(nt + "=" + firstSet(nt));
            ans.add("First(" + nt + ")=" + this.firstSet(nt).toString());
        }

        return ans;
    }

    Set<String> firstSet(String symbol){

        for(Production p : productionList){
            if(p.head.equals(symbol)){
                // for each production of symbol

                //region Add EPS to first set
                if(p.body.contains(EPS)) {
                    mapFirst.get(p.head).add(EPS);
                }
                //endregion

                if(p.body.size() > 0) {
                    //region if production begins with terminal, add terminal to set
                    if(this.Terminals.contains(p.body.get(0))){
                        mapFirst.get(p.head).add(p.body.get(0));
                    }
                    //endregion

                    //region First(A) = First(P) where A -> PX, so find First(P)
                    Set s = mapFirst.get(p.head);
                    Set f = firstSet(p.body.get(0));
                    if(null != f)
                        s.addAll(f);
                    //endregion
                }
            }
        }

        return mapFirst.get(symbol);
    }

    List<String> getFollowSets(){
        List<String> ans = new ArrayList<>();

        allFollows();

        for(String nt : this.NonTerminals){
            //ans.add("Follow(" + nt + ")=");// + this.followSet(nt).toString());
            ans.add("Follow(" + nt + ")=" + mapFollow.get(nt).toString());
        }
        return ans;
    }

    void allFollows(){

        for(String nt : this.NonTerminals){
            // init prevFollow with mapFollow values
            prevFollow.put(nt, new HashSet<>( mapFollow.get(nt) ) ) ;
            // keep track of Follow(A) calling and
            // allow only one invocation every loop
            followCalled.put(nt, false);
        }

        // https://stackoverflow.com/questions/29197332/how-to-find-first-and-follow-sets-of-a-recursive-grammar/29200860#29200860
        while( true ){

            for(String nt : this.NonTerminals){
                Set followNT = followSet(nt);
                mapFollow.get(nt).addAll( followNT );
            }

            // compare prevFollow and newer mapFollow for equality
            // if both are equal then Follow has been found
            if( compareMaps(prevFollow, mapFollow)){
                break;
            }
            // else, update prevFollow with newer values of mapFollow
            for(Map.Entry<String, Set<String>> entry : prevFollow.entrySet()){
                String key = entry.getKey();
                entry.setValue( mapFollow.get(key) );
            }

            // and reset followCalled
            for(Map.Entry<String, Boolean> entry : followCalled.entrySet()){
                entry.setValue(false);
            }

        }
    }

    boolean compareMaps(Map<String, Set<String>> a1, Map<String, Set<String>> a2){
        for(String k1 : a1.keySet()){
            Set s1 = a1.get(k1);
            Set s2 = a2.get(k1);
            if( null == s2){
                // key not found in second map
                return false;
            }
            if( ! s1.equals(s2)){
                return false;
            }
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

                int pos = p.body.indexOf(symbol);

                // finding Follow(B)
                if(pos == p.body.size() - 1){
                    // A -> Q B
                    // then Follow(B) |= Follow(A) except EPS

                    if(null == followCalled.get(symbol)){
                        // if symbol doesn't exist in map
                        throw new RuntimeException("NT=" + symbol + " doesn't exist in followCalled");
                    }

                    Set<String> followA;
                    if( followCalled.get(symbol) ){
                        // if Follow(followA) has already been called, then skip this iteration
                        followA = prevFollow.get(p.head);
                    }
                    else{
                        followA = followSet(p.head);
                        followCalled.put(symbol, true);
                    }

                    // Make note that Follow(followA) has been called now
//                    Set<String> followA = followSet(p.head);

                    mapFollow.get(symbol).addAll(followA);

                }
                else{
                    // A -> Q B P
                    // Follow(B) |= First(P) except EPS
                    String nextSymbol = p.body.get( p.body.indexOf(symbol) + 1 );
                    Set firstNext = mapFirst.get(nextSymbol);
                    mapFollow.get(symbol).addAll(firstNext);
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

        Grammar g = new Grammar(Terminals, NonTerminals, list_productions, startSymbol);

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
