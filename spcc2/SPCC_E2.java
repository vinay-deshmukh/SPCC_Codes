package spcc2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static spcc2.Lexer.PAT_LINE;

class Lexer{
    //static final String ZERO_SPACE = "[\\s]*";
    static final String SPACE = "[\\s]+";
    static final String NOUN = "[a-z]+";
    static final String VERB = "(hate|like)";
    static final String ACTION = "they" + SPACE + NOUN;
    static final String PHRASE =
            NOUN + SPACE +
            VERB + SPACE +
            NOUN;
    static final String DOT = "\\.";
    static final String LINE =
            "(" +
            "if" + SPACE + PHRASE + SPACE +
            "then" + SPACE + ACTION + DOT
            +")|"
            + DOT; // either the whole thing or just the dot


    static final Pattern PAT_LINE = Pattern.compile(LINE, Pattern.CASE_INSENSITIVE);
    static final Pattern PAT_PHRASE = Pattern.compile(PHRASE, Pattern.CASE_INSENSITIVE);
    static final Pattern PAT_ACTION = Pattern.compile(ACTION, Pattern.CASE_INSENSITIVE);
    static final Pattern PAT_NOUN = Pattern.compile(NOUN, Pattern.CASE_INSENSITIVE);
    static final Pattern PAT_VERB = Pattern.compile(VERB, Pattern.CASE_INSENSITIVE);


    Lexer(){    }

    public void parseLine(String line) {

        Matcher mat_phrase = PAT_PHRASE.matcher(line);
        mat_phrase.find();
        String pattern = mat_phrase.group();
        System.out.println("Phrase: " + pattern);
        parsePattern(pattern);

        Matcher mat_action = PAT_ACTION.matcher(line);
        mat_action.find();
        System.out.println("Action: " + mat_action.group());
    }

    void parsePattern(String pattern){
        Matcher match_noun = PAT_NOUN.matcher(pattern);
        match_noun.find();
        String noun1 = match_noun.group();


        Matcher match_verb = PAT_VERB.matcher(pattern);
        match_verb.find();
        String verb = match_verb.group();

        match_noun.find(match_verb.end());//start finding after end of verb
        String noun2 = match_noun.group();

        System.out.println("Noun1: " + noun1);
        System.out.println("Verb : " + verb);
        System.out.println("Noun2: " + noun2);
    }
}

class Lexer2{
    static final String SPACE = "[\\s]+";
    static final String NOUN = "[a-z]+";
    static final String VERB = "(hate|like)";
    static final String ACTION = "they" + SPACE + NOUN;
    static final String PHRASE =
            NOUN + SPACE +
                    VERB + SPACE +
                    NOUN;
    static final String DOT = "\\.";
    static final String LINE =
            "(" +
                    "if" + SPACE + PHRASE + SPACE +
                    "then" + SPACE + ACTION + DOT
                    +")";
                    //+ DOT; // either the whole thing or just the dot

    Pattern full = Pattern.compile(
            "(?<line>if\\s+" +
                    "+(?<phrase>" +
                        "(?<noun1>[a-z]+)\\s+" +
                        "(?<verb>hate|like)\\s+" +
                        "(?<noun2>[a-z]+)\\s+" +
                    ")"+
                    "then\\s+" +
                    "(?<action>" +
                        "they\\s+" +
                        "(?<noun3>[a-z]+)" +
                    ")" +
            "\\.)",
            Pattern.CASE_INSENSITIVE);
}

public class SPCC_E2 {
    public static void main(String[] args) {
        Lexer lexer = new Lexer();
        String input =
                "If dogs hate cats then they chase. " +
                "If cats like milk then they drink.";

        //region First approach, many sub regexes
        Matcher matcher = PAT_LINE.matcher(input);
        System.out.println("approach 1");
        while(matcher.find()){
            String line = matcher.group();
            System.out.println("Line: " + line);
            lexer.parseLine(line);
        }
        //endregion

        //region Second approach with roided out single regex
        System.out.println("\n\napproach 2");
        Lexer2 lexer2 = new Lexer2();
        Matcher matcher_full = lexer2.full.matcher(input);
        while(matcher_full.find()){
            System.out.println("Line:" + matcher_full.group("line"));
            System.out.println("Phrase:" + matcher_full.group("phrase"));
            System.out.println("Noun1:" + matcher_full.group("noun1"));
            System.out.println("Verb:" + matcher_full.group("verb"));
            System.out.println("Noun2:" + matcher_full.group("noun2"));
            System.out.println("Action:" + matcher_full.group("action"));
        }
        //endregion

    }
}
/*
Output:
Line: If dogs hate cats then they chase.
Phrase: dogs hate cats
Noun1: dogs
Verb : hate
Noun2: cats
Action: they chase
Line: If cats like milk then they drink.
Phrase: cats like milk
Noun1: cats
Verb : like
Noun2: milk
Action: they drink

 */
