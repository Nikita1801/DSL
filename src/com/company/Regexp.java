package com.company;

import java.util.*;
import java.util.regex.*;

public class Regexp {



    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // String testString = "VAR size = 100 + 1";


        List<String> stringList = new LinkedList<>();
        List<Token> tokenList = new LinkedList<>();

        for (int line = 0; true; line++) {
            stringList.add(line, scanner.nextLine());
            Boolean checkIsEmpty = stringList.get(line).isEmpty();
            if (checkIsEmpty) {
                stringList.remove(line);
                break;
            }
        }

        //use lexer to get tokens
        Lexer lex = new Lexer();
        tokenList = lex.lexTheInput(stringList);
        for (Token token : tokenList) {
            System.out.println(token);
        }

        //user parser
        Parser parser = new Parser();
        Boolean bool = parser.parse(tokenList);
        System.out.println("Parser: " + bool);
    }


}


class Lexer{
    private static Map<String, Pattern> lex = new HashMap<>();
    private static List<String> charsToRemove = new ArrayList<>();

    static {

        lex.put("KEY_WORD_VAR", Pattern.compile("VAR"));
        lex.put("VARIABLE", Pattern.compile("[a-z][a-z0-9]*"));
        lex.put("DIGIT", Pattern.compile("^0|([1-9][0-9]*)"));
        lex.put("IF", Pattern.compile("if"));
        lex.put("ELSE", Pattern.compile("else"));
        lex.put("FOR", Pattern.compile("for"));
        lex.put("COMMENT", Pattern.compile("//"));
        lex.put("ASSIGN", Pattern.compile("="));
        lex.put("ADD", Pattern.compile("\\+"));
        lex.put("Error: unknown element", Pattern.compile("[$.`'{}<>]"));


        charsToRemove.add(" ");
    }

    public List<Token> lexTheInput(List<String> stringList) {
        List<Token> tokenList = new ArrayList<>();
        for (int position = 0; position < stringList.size(); position++) {

            String commandLine = stringList.get(position);
//            commandLine = prepareString(commandLine);
//            System.out.println(commandLine);

            String acc = "";
            for (int i = commandLine.length() - 1; i >= 0; i--) {
                char c = commandLine.charAt(i);

                //accum all symbols app to whitespace and skip all trailing whitespaces
                for (int k = 0; true; k++) {
                    if (i - k >= 0 && commandLine.charAt(i - k) != ' ') {
                        acc += commandLine.charAt(i - k);
                    } else {
                        i = i - k;
                        break;
                    }
                }

                //prepare accumulator before  matching against regexp
                StringBuilder builder = new StringBuilder();
                builder.append(acc);
                acc = builder.reverse().toString();
                // System.out.println("I'm current accumulator: " + acc);

                //found tokens in accum
                for (String key : lex.keySet()) {
                    Matcher matcher;
                    matcher = lex.get(key).matcher(acc);


                    while (matcher.find()) {
                        String currentValue = acc.substring(matcher.start(), matcher.end());
                        tokenList.add(new Token(key, currentValue));
                    }

                }
                acc = "";

            }


        }
        return tokenList;
    }

    /***
     * Deletes all not neseccesary symbols in command line of programm
     * @param commandLine
     * @return
     */
    private static String prepareString(String commandLine) {
        String res = commandLine;
        for (String str : charsToRemove) {
            res = commandLine.replace(str, "");
        }
        return res;
    }
}

class Token {

    private String type;
    private String value;

    public Token(String type, String value) {
        this.type = type;
        this.value = value;

    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                '}';

    }

}

class PairLexem {
    public String tokenLeft;
    public String tokenRight;

    public PairLexem(String token1, String token2) {
        this.tokenLeft = token1;
        this.tokenRight = token2;
    }
}


class Parser {

    List<PairLexem> pairLexems = new ArrayList<>();

    public Parser() {

        pairLexems.add(new PairLexem("KEY_WORD_VAR", "VARIABLE"));
        pairLexems.add(new PairLexem("VARIABLE", "ASSIGN"));
        pairLexems.add(new PairLexem("ASSIGN", "DIGIT"));
        pairLexems.add(new PairLexem("DIGIT", "ADD"));
        pairLexems.add(new PairLexem("ADD", "DIGIT"));


    }

    private boolean isRightPairLexem(Token tokenLeft, Token tokenRight) {
        for (PairLexem p : pairLexems) {
            if((tokenRight.getType().equals(p.tokenRight)) && (tokenLeft.getType().equals(p.tokenLeft))){
                return true;
            }
        }
        return false;
    }

    public boolean parse(List<Token> tokenList) {
        if (tokenList == null) {
            return true;
        }
        if (tokenList.stream().anyMatch(token -> token.getType().equals("Error: unknown element"))) {
            return false;
        }
        for (int i = 0; i <= tokenList.size() - 2; i++) {
            if (isRightPairLexem(tokenList.get(i + 1), tokenList.get(i)) == false) { // Reversed pair lexems
                return false;
            }
        }
        return true;
    }
}