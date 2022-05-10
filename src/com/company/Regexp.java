package com.company;

import lombok.Builder;
import lombok.Getter;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

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

@Builder
@Getter
class TokenDescription {
    boolean isAtomic;
    Pattern pattern;
}

class Lexer {
    private Map<String, TokenDescription> lex = new HashMap<>();
    //private static List<String> charsToRemove = new ArrayList<>();


    Lexer() {

        lex.put("KEY_WORD_VAR", TokenDescription.builder().pattern(Pattern.compile("VAR")).isAtomic(false).build());
        lex.put("VARIABLE", TokenDescription.builder().pattern(Pattern.compile("[a-z][a-z0-9]*")).isAtomic(false).build());
        lex.put("END", TokenDescription.builder().pattern(Pattern.compile(";")).isAtomic(true).build());
        lex.put("DIGIT", TokenDescription.builder().pattern(Pattern.compile("^0|([1-9][0-9]*)")).isAtomic(false).build());
        lex.put("IF", TokenDescription.builder().pattern(Pattern.compile("if")).isAtomic(false).build());
        lex.put("ELSE", TokenDescription.builder().pattern(Pattern.compile("else")).isAtomic(false).build());
        lex.put("FOR", TokenDescription.builder().pattern(Pattern.compile("for")).isAtomic(false).build());
        lex.put("COMMENT", TokenDescription.builder().pattern(Pattern.compile("//")).isAtomic(false).build());
        lex.put("ASSIGN", TokenDescription.builder().pattern(Pattern.compile("=")).isAtomic(true).build());
        lex.put("ADD", TokenDescription.builder().pattern(Pattern.compile("\\+")).isAtomic(true).build());
        lex.put("Error: unknown element", TokenDescription.builder().pattern(Pattern.compile("[$.`'{}<>]")).isAtomic(false).build());
        lex.put("OPEN_BRACKET", TokenDescription.builder().pattern(Pattern.compile("[\\(]")).isAtomic(true).build());
        lex.put("CLOSE_BRACKET", TokenDescription.builder().pattern(Pattern.compile("[\\)]")).isAtomic(true).build());


        //charsToRemove.add(" ");
    }

    private Optional<Token> detectAtomic(char symbol) {
        List<String> atomicLexKeys = lex.entrySet().stream().filter(l -> l.getValue().isAtomic == true).map(l -> l.getKey()).collect(Collectors.toList());
        Matcher matcher;
        for (String atomicKey : atomicLexKeys) {
            matcher = lex.get(atomicKey).getPattern().matcher(symbol + "");


            if (matcher.find()) {
                return Optional.of(new Token(atomicKey, symbol + ""));
            }
        }
        return Optional.empty();
    }

    public List<Token> lexTheInput(List<String> stringList) {
        List<Token> tokenList = new ArrayList<>();

        //split all input by ;
        String allInp = stringList.stream().reduce((accumulator, inpString) -> accumulator+inpString).get();
        List<String> inputFormatted = Arrays.asList(allInp.split(";")).stream().map(inp -> inp+";").collect(Collectors.toList());

        for (int position = 0; position < inputFormatted.size(); position++) {

            String commandLine = inputFormatted.get(position);
//            commandLine = prepareString(commandLine);
//            System.out.println(commandLine);

            String acc = "";
            for (int i = commandLine.length() - 1; i >= 0; i--) {
                char c = commandLine.charAt(i);

                //check if char is already can be atomic token
                Optional<Token> atomic = detectAtomic(c);
                if (atomic.isPresent()) {
                    tokenList.add(atomic.get());
                    continue;
                }

                //accum all symbols app to whitespace and skip all trailing whitespaces
                for (int k = 0; true; k++) {
                    int furtherCharArrow = i - k;
                    if (furtherCharArrow >= 0) {
                        char furtherChar = commandLine.charAt(furtherCharArrow);

                        //if our next symbol is atomic we should break the cycle
                        // it is the end of current expression and start of new
                        if (detectAtomic(furtherChar).isPresent()) {
                            i = furtherCharArrow + 1;
                            break;
                        }

                        if (furtherChar != ' ') {
                            acc += furtherChar;
                        } else {
                            i = furtherCharArrow;
                            break;
                        }
                    } else {
                        i = furtherCharArrow;
                        break;
                    }
                }


                //reverse accumulator before  matching against regexp
                StringBuilder builder = new StringBuilder();
                builder.append(acc);
                acc = builder.reverse().toString();
                // System.out.println("I'm current accumulator: " + acc);

                //found tokens in accum
                for (String key : lex.keySet()) {
                    Matcher matcher;
                    matcher = lex.get(key).getPattern().matcher(acc);


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

//    /***
//     * Deletes all not neseccesary symbols in command line of programm
//     * @param commandLine
//     * @return
//     */
//    private static String prepareString(String commandLine) {
//        String res = commandLine;
//        for (String str : charsToRemove) {
//            res = commandLine.replace(str, "");
//        }
//        return res;
//    }
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
        pairLexems.add(new PairLexem("ADD", "VARIABLE"));
        pairLexems.add(new PairLexem("VARIABLE", "ADD"));

        pairLexems.add(new PairLexem("ADD", "OPEN_BRACKET"));

        pairLexems.add(new PairLexem("OPEN_BRACKET", "VARIABLE"));
        pairLexems.add(new PairLexem("OPEN_BRACKET", "DIGIT"));

        pairLexems.add(new PairLexem("VARIABLE", "CLOSE_BRACKET"));
        pairLexems.add(new PairLexem("DIGIT", "CLOSE_BRACKET"));

        pairLexems.add(new PairLexem("CLOSE_BRACKET", "ADD"));


        pairLexems.add(new PairLexem("CLOSE_BRACKET", "END"));
        pairLexems.add(new PairLexem("DIGIT", "END"));
        pairLexems.add(new PairLexem("VARIABLE", "END"));

        pairLexems.add(new PairLexem("END", "KEY_WORD_VAR"));
        pairLexems.add(new PairLexem("END", "VARIABLE"));




// VAR abc = 100 + ( 5 + 1 )
// VAR abc=100+(5+1)

    }

    private boolean isRightPairLexem(Token tokenLeft, Token tokenRight) {
        for (PairLexem p : pairLexems) {
            if ((tokenRight.getType().equals(p.tokenRight)) && (tokenLeft.getType().equals(p.tokenLeft))) {
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