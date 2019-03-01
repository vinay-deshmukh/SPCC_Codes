package spcc2;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static spcc2.Lexer.PAT_LINE;

enum Category{
//    LINE, PHRASE, NOUN, VERB, ACTION, SPACE
}
class Lexer{
    static final String ZERO_SPACE = "[\\s]*";
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


    /*
    // unnecessary improvement
    static final String INPUT =
            "^" + ZERO_SPACE +
                    "[" + LINE + "]*" + ZERO_SPACE + "$";
    */

    Map<String, String> sym_table = new HashMap<>();
    List<String> keywords = new ArrayList<>();
    List<String> nouns = new ArrayList<>();
    List<String> verbs = new ArrayList<>();


    Lexer(){    }

    public void parseLine(String line) {

//        Pattern p = Pattern.compile("if", Pattern.CASE_INSENSITIVE);
//        Matcher m = p.matcher(line);
//        m.find();
//        String keyw = m.group();
//
//        if(! sym_table.containsKey("if")) {
//            keywords.add("<K, " + keywords.size() + ">");
//            sym_table.put("if", keywords.get( keywords.size() - 1));
//        }

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

public class SPCC_E2 {
    static Scanner sc = new Scanner(System.in);
    public static void main(String[] args) {
        Lexer lexer = new Lexer();
        String input =
                "If dogs hate cats then they chase. " +
                "If cats like milk then they drink.";
        //TODO: replace with scan in()



        Matcher matcher = PAT_LINE.matcher(input);

        while(matcher.find()){
            String line = matcher.group();
            System.out.println("Line: " + line);
            lexer.parseLine(line);
        }

    }
}
