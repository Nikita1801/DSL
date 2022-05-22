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

        if(bool){
            ConvertToPolishNotation convertToPolishNotation = new ConvertToPolishNotation();
            List<Token> polis = convertToPolishNotation.getPolish(tokenList);
            System.out.println("Converted tokens to polish notation: " + polis);

            Machine machine = new Machine();
            String result = machine.vmachine(polis);
            System.out.println("Machine result: " + result);
        }


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
        lex.put("MINUS", TokenDescription.builder().pattern(Pattern.compile("\\-")).isAtomic(true).build());
        lex.put("MULT", TokenDescription.builder().pattern(Pattern.compile("\\*")).isAtomic(true).build());
        lex.put("DIVIDE", TokenDescription.builder().pattern(Pattern.compile("\\/")).isAtomic(true).build());
        lex.put("Error: unknown element", TokenDescription.builder().pattern(Pattern.compile("[$.`'{}<>]")).isAtomic(false).build());
        lex.put("OPEN_BRACKET", TokenDescription.builder().pattern(Pattern.compile("[\\(]")).isAtomic(true).build());
        lex.put("CLOSE_BRACKET", TokenDescription.builder().pattern(Pattern.compile("[\\)]")).isAtomic(true).build());
        lex.put("PRINT", TokenDescription.builder().pattern(Pattern.compile("PRINT")).isAtomic(true).build());


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
        pairLexems.add(new PairLexem("ASSIGN", "OPEN_BRACKET"));
        pairLexems.add(new PairLexem("DIGIT", "ADD"));
        pairLexems.add(new PairLexem("DIGIT", "MULT"));
        pairLexems.add(new PairLexem("ADD", "DIGIT"));
        pairLexems.add(new PairLexem("MULT", "DIGIT"));
        pairLexems.add(new PairLexem("ADD", "VARIABLE"));
        pairLexems.add(new PairLexem("MULT", "VARIABLE"));
        pairLexems.add(new PairLexem("VARIABLE", "ADD"));

        pairLexems.add(new PairLexem("ADD", "OPEN_BRACKET"));
        pairLexems.add(new PairLexem("MULT", "OPEN_BRACKET"));

        pairLexems.add(new PairLexem("OPEN_BRACKET", "VARIABLE"));
        pairLexems.add(new PairLexem("OPEN_BRACKET", "DIGIT"));

        pairLexems.add(new PairLexem("VARIABLE", "CLOSE_BRACKET"));
        pairLexems.add(new PairLexem("DIGIT", "CLOSE_BRACKET"));

        pairLexems.add(new PairLexem("CLOSE_BRACKET", "ADD"));
        pairLexems.add(new PairLexem("CLOSE_BRACKET", "MULT"));


        pairLexems.add(new PairLexem("CLOSE_BRACKET", "END"));
        pairLexems.add(new PairLexem("DIGIT", "END"));
        pairLexems.add(new PairLexem("VARIABLE", "END"));

        pairLexems.add(new PairLexem("END", "KEY_WORD_VAR"));
        pairLexems.add(new PairLexem("END", "VARIABLE"));
        pairLexems.add(new PairLexem("END", "PRINT"));

        pairLexems.add(new PairLexem("PRINT", "VARIABLE"));
        pairLexems.add(new PairLexem("PRINT", "DIGIT"));





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

    public boolean bracketsRight(List<Token> tokenList) {
        int openBrackets = 0;
        int closeBrackets = 0;

        for (Token token: tokenList) {
            switch (token.getType()){
                case "OPEN_BRACKET": { openBrackets++; break;}
                case "CLOSE_BRACKET": {closeBrackets++; break;}
                default: break;
            }
        }
        if (openBrackets == closeBrackets) {
            return true;
        }
        else {return false;}
    }
}


class ConvertToPolishNotation {
    public int priority(Token token) {
        if (token.getType().equals("DIVIDE") || token.getType().equals("MULT")) {return 10;}
        if (token.getType().equals("ADD") || token.getType().equals("MINUS")) {return 9;}
        if (token.getType().equals("ASSIGN") || token.getType().equals("PRINT")) {return 8;}
        else { return 0; }

    }

    public List<Token> getPolish (List<Token> nePolish){
        boolean toPrint = false;

        List<Token> preparedNePolish = new ArrayList<>();

        List<Token> buffer = new ArrayList<>();
        for (int i = 0; i<nePolish.size(); i++){
            if(i == 0) continue;
            if(nePolish.get(i).getType().equals("END")){
                buffer.add(0, new Token("END", ";"));
                Collections.reverse(buffer);
                preparedNePolish.addAll(buffer);
                buffer = new ArrayList<>();
                continue;
            }
            if(i == nePolish.size()-1){
                buffer.add(0, new Token("END", ";"));
                buffer.add(nePolish.get(i));
                Collections.reverse(buffer);
                preparedNePolish.addAll(buffer);
                buffer = new ArrayList<>();
                continue;
            }

            buffer.add(nePolish.get(i));
        }

        List<Token> alreadyPolish = new ArrayList<Token>();
        Stack<Token> stack = new Stack<Token>();

        for (Token token : preparedNePolish) {
            if (token.getType().equals("PRINT")) {toPrint = true;}
            else if (token.getType().equals("END") ) { while(stack.size() != 0) {alreadyPolish.add(stack.pop());}}
            else if (token.getType().equals("DIGIT") || token.getType().equals("VARIABLE")) { alreadyPolish.add(token);}
            else if (token.getType().equals("OPEN_BRACKETS")) { stack.push(token);}
            else if (token.getType().equals("CLOSE_BRACKETS")) {
                while (!stack.peek().getType().equals("OPEN_BRACKET")  || stack.size() == 0) {
                    alreadyPolish.add(stack.pop());
                    stack.pop();
                }
            }
            //else if (token.getType() == "")
            else if (token.getType().equals("ADD") || token.getType().equals("MINUS") || token.getType().equals("DIVIDE")
                    || token.getType().equals("MULT") || token.getType().equals("ASSIGN") ) {
                if (stack.size() == 0) {  stack.push(token); }
                else if ( stack.peek().getType().equals("OPEN_BRACKETS")) { stack.push(token); }
                else if (priority(token) <= priority(stack.peek())) {
                    while (stack.size() != 0 && priority(token) <= priority(stack.peek())) {
                        if (stack.peek().getType().equals("OPEN_BRACKET")) { break; }
                        alreadyPolish.add(stack.pop());
                    }
                    stack.push(token);
                }
                else if (priority(token) > priority(stack.peek())) {stack.push(token);}
            }


        }
        if (toPrint) { alreadyPolish.add(new Token("PRINT", "PRINT"));}
        while (stack.size() != 0) {
            alreadyPolish.add(stack.pop());
        }
        return alreadyPolish;
    }

}

class Machine {
    private Stack<VarHolder> stack = new Stack<VarHolder>();
    private List<VarHolder> vars = new ArrayList<VarHolder>();

    boolean isExist(String var) {
        for (VarHolder v: vars) {
            if (v.name.equals(var)) {return true;}
        }
        return false;
    }
    double getVarHolderByName(String type) {
        for (VarHolder var : vars) {
            if (var.name.equals(type)) {return var.value;}
        }
        return 0f;
    }

    int getVarHolderByNameIndex(String type) {
        for (int i = 0; i<vars.size(); i++) {
            if (vars.get(i).name.equals(type)) {return i;}
        }
        return -1;
    }
    public String vmachine(List<Token> polishString) {
        String output = "";
        for (Token token: polishString) {
            switch (token.getType()){
                case "PRINT":
                    output += stack.pop().value;
                    break;
                case "VARIABLE":
                {
                    if(!isExist(token.getValue())){
                        stack.push(new VarHolder( token.getValue(), 0f));
                        vars.add(new VarHolder(token.getValue(), 0f));
                    }
                    else { stack.push(new VarHolder( token.getValue(), getVarHolderByName(token.getValue()))); }
                break;
                }
                case "DIGIT":
                    stack.push(new VarHolder("", Double.parseDouble(token.getValue())));
                    break;
                //case arOPT
                case "ADD":
                    double op1 = stack.pop().value;
                    double op2 = stack.pop().value;
                    stack.push(new VarHolder("", (op1 + op2)));
                    break;
                case "MULT": {
                    double multOp1 = stack.pop().value;
                    double multOp2 = stack.pop().value;
                    stack.push(new VarHolder("", (multOp1 * multOp2)));
                    break;
                }
                    // case equal
                case "ASSIGN": {
                    double opb = stack.pop().value;
                    String opa = stack.pop().name;

                    int a = getVarHolderByNameIndex(opa);
                    vars.set(a, new VarHolder(opa, opb));
                }

                case "END": {stack.clear(); break;}
                default: { break; }
            }
        }
        return output;
    }
}
