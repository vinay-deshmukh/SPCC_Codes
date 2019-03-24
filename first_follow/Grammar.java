package first_follow;

import java.util.*;

public class Grammar{

    public List<String> Terminals;
    public List<String> NonTerminals;
    public List<Production> productionList;
    public String startSymbol;

    public static String EPS = "9";
    public static String DOLLAR = "$";

    public Map<String, Set<String>> mapFirst = new HashMap<>();
    public Map<String, Set<String>> mapFollow = new HashMap<>();

    Map<String, Set<String>> prevFollow = new HashMap<>();
    Map<String, Boolean> followCalled = new HashMap<>();

    public Grammar(
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
        mapFirst.put(Grammar.EPS, new HashSet<>(Collections.singleton(Grammar.EPS)));

        for(String nt: this.NonTerminals){
            this.firstSet(nt); //find firsts asap after construction
        }
        allFollows(); // find follows asap after construction
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

                //region First(A) = First(P) where A -> PX, so find First(P), while First(P) contains EPS
                int index = 0;
                while(index < p.body.size()){
                    Set<String> s = firstSet(p.body.get(index));
                    mapFirst.get(p.head).addAll(s);
                    if(! s.contains(Grammar.EPS)){
                        break;
                    }
                    index++;
                }
                //endregion
            }
        }

        return mapFirst.get(symbol);
    }

    List<String> getFollowSets(){
        List<String> ans = new ArrayList<>();
        //allFollows(); // Main follow finder function

        for(String nt : this.NonTerminals){
            ans.add("Follow(" + nt + ")=" + mapFollow.get(nt).toString());
        }
        return ans;
    }

    void allFollows(){

        for(String nt : this.NonTerminals){
            // init prevFollow with mapFollow values
            prevFollow.put(nt, new HashSet<>( mapFollow.get(nt) ) ) ;
            followCalled.put(nt, false);
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
            for(Map.Entry<String, Boolean> entry : followCalled.entrySet()){
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

                    if(null == followCalled.get(p.head)){
                        // if symbol doesn't exist in map
                        throw new RuntimeException("P=" + p + "; NT=" + symbol + " doesn't exist in followCalled");
                    }

                    Set<String> followA = new HashSet<>();

                    if( followCalled.get(p.head) ){
                        // if Follow(followA) has already been called, then skip this iteration
                        followA = prevFollow.get(p.head);
                    }
                    else{
                        // Make note that Follow(followA) has been called now
                        followCalled.put(p.head, true);

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

/*
S
S A B
i j
4
S = A B
A = 9
A = i
B = j


 */