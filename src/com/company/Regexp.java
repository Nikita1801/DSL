package com.company;

import lombok.Builder;
import lombok.Getter;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

public class Regexp {
    /*
    IF ( 5 < 6) { VAR a = 10 };
    IF (3 > 7) {PRINT 5};

    VAR a = 10 * 5;
    IF (a > 40) {PRINT a - 5};

    VAR a = 10;
    WHILE (a > 2) { a = a -1};
    PRINT a;

    VAR abc = 100 + 5 * 2;
    VAR result = 50;
    PRINT abc;
    PRINT result;

    LINKEDLIST a;
    a ADD 2;
    a ADD 3;
    LISTOUT a;
    a DELETE 3;
    a CONTAINS 2;
    LISTOUT a;

    */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // String testString = "VAR size = 100 + 1";


        List<String> stringList = new LinkedList<>();
        List<List<Token>> tokenList = new LinkedList<>();

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
        Boolean bool = false;
        for (List<Token> tokens : tokenList) {
            System.out.println("____________________________");

            System.out.println(Arrays.toString(tokens.toArray()));

            //use parser
            Parser parser = new Parser();
            bool = parser.parse(tokens);
            System.out.println("Parser: " + bool);
            if(!bool) break;

            System.out.println("____________________________");
            System.out.println();
        }

        if (bool) {
            Machine machine = new Machine();
            for (List<Token> tokens : tokenList) {
                System.out.println("____________________________");

                String result = "RESULT IS BLANK. MAYBE PROBLEM";
                ConvertToPolishNotation convertToPolishNotation = new ConvertToPolishNotation();
                if (tokens.stream().filter(x -> x.getType().startsWith("LINKED_LIST")).count()>0){
                    Collections.reverse(tokens);
                    result = machine.vmachine(tokens);
                }
                else if (tokens.stream().filter(x -> x.getType().equals("IF") || x.getType().equals("WHILE")).count() > 0) {
                    System.out.println("The command contains special keyword IF or WHILE");
                    // обработка токенов своеобразно для спец конструкций
                    List<Token> reversed = convertToPolishNotation.prepareTokensByReverse(tokens);

                    List<Token> toMachine = new ArrayList<>();
                    toMachine.add(reversed.get(0));// it would be IF or WHILE

                    //get condition and convert it to polis
                    int indexOfOB = reversed.indexOf(reversed.stream().filter(t-> t.getType().equals("OPEN_BRACKET")).findFirst().get());
                    int indexOfCB = reversed.indexOf(reversed.stream().filter(t-> t.getType().equals("CLOSE_BRACKET")).findFirst().get());
                    List<Token> betweenBrackets = reversed.subList(indexOfOB+1,indexOfCB);// get all that  inside () in IF
                    toMachine.add(reversed.get(indexOfOB));
                    toMachine.addAll(convertToPolishNotation.getPolish(betweenBrackets));
                    toMachine.add(reversed.get(indexOfCB));

                    //get body and convert it to polis
                    int indexOfCOB = reversed.indexOf(reversed.stream().filter(t-> t.getType().equals("OPEN_BRACE")).findFirst().get());
                    int indexOfCCB = reversed.indexOf(reversed.stream().filter(t-> t.getType().equals("CLOSE_BRACE")).findFirst().get());
                    List<Token> betweenCurledBrackets = reversed.subList(indexOfCOB+1,indexOfCCB);// get all that  inside {} in IF;
                    toMachine.add(reversed.get(indexOfCOB));
                    toMachine.addAll(convertToPolishNotation.getPolish(betweenCurledBrackets));
                    toMachine.add(reversed.get(indexOfCCB));

                    result = machine.vmachine(toMachine);



                } else {
                    List<Token> polis = convertToPolishNotation.getPolish(convertToPolishNotation.prepareTokensByReverse(tokens));
                    System.out.println("Converted tokens to polish notation: " + polis);

                    result = machine.vmachine(polis);
                }
                System.out.println("Machine result: " + result);

                System.out.println("____________________________");
                System.out.println();
            }
        }


    }


}

@Builder
@Getter
class TokenDescription {
    boolean isAtomic;
    Pattern pattern;
}

@Getter
class Lexer {
    private Map<String, TokenDescription> lex = new HashMap<>();
    //private static List<String> charsToRemove = new ArrayList<>();



    Lexer() {

        lex.put("KEY_WORD_VAR", TokenDescription.builder().pattern(Pattern.compile("VAR")).isAtomic(false).build());
        lex.put("VARIABLE", TokenDescription.builder().pattern(Pattern.compile("[a-z][a-zA-Z0-9]*")).isAtomic(false).build());
        lex.put("END", TokenDescription.builder().pattern(Pattern.compile(";")).isAtomic(true).build());
        lex.put("DIGIT", TokenDescription.builder().pattern(Pattern.compile("^0|([1-9][0-9]*)")).isAtomic(false).build());
        lex.put("COMMENT", TokenDescription.builder().pattern(Pattern.compile("//")).isAtomic(false).build());
        lex.put("ASSIGN", TokenDescription.builder().pattern(Pattern.compile("=")).isAtomic(true).build());
        lex.put("ADD", TokenDescription.builder().pattern(Pattern.compile("\\+")).isAtomic(true).build());
        lex.put("MINUS", TokenDescription.builder().pattern(Pattern.compile("\\-")).isAtomic(true).build());
        lex.put("MULT", TokenDescription.builder().pattern(Pattern.compile("\\*")).isAtomic(true).build());
        lex.put("DIVIDE", TokenDescription.builder().pattern(Pattern.compile("\\/")).isAtomic(true).build());
        lex.put("Error: unknown element", TokenDescription.builder().pattern(Pattern.compile("[$.`']")).isAtomic(false).build());
        lex.put("OPEN_BRACKET", TokenDescription.builder().pattern(Pattern.compile("[\\(]")).isAtomic(true).build());
        lex.put("CLOSE_BRACKET", TokenDescription.builder().pattern(Pattern.compile("[\\)]")).isAtomic(true).build());
        lex.put("PRINT", TokenDescription.builder().pattern(Pattern.compile("PRINT")).isAtomic(true).build());
        lex.put("IF", TokenDescription.builder().pattern(Pattern.compile("IF")).isAtomic(false).build());
        lex.put("WHILE", TokenDescription.builder().pattern(Pattern.compile("WHILE")).isAtomic(false).build());
        lex.put("LESS", TokenDescription.builder().pattern(Pattern.compile("<")).isAtomic(true).build());
        lex.put("MORE", TokenDescription.builder().pattern(Pattern.compile(">")).isAtomic(true).build());

        lex.put("OPEN_BRACE", TokenDescription.builder().pattern(Pattern.compile("\\{")).isAtomic(true).build());
        lex.put("CLOSE_BRACE", TokenDescription.builder().pattern(Pattern.compile("\\}")).isAtomic(true).build());
        //LinkedList
        lex.put("LINKED_LIST", TokenDescription.builder().pattern(Pattern.compile("LINKEDLIST")).isAtomic(false).build());
        lex.put("LINKED_LIST_ADD", TokenDescription.builder().pattern(Pattern.compile("ADD")).isAtomic(false).build());
        lex.put("LINKED_LIST_DELETE", TokenDescription.builder().pattern(Pattern.compile("DELETE")).isAtomic(false).build());
        lex.put("LINKED_LIST_CONTAINS", TokenDescription.builder().pattern(Pattern.compile("CONTAINS")).isAtomic(false).build());
        lex.put("LINKED_LIST_PRINTLL", TokenDescription.builder().pattern(Pattern.compile("LISTOUT")).isAtomic(false).build());


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

    public List<List<Token>> lexTheInput(List<String> stringList) {
        List<List<Token>> tokenList = new ArrayList<>();

        //split all input by ;
        String allInp = stringList.stream().reduce((accumulator, inpString) -> accumulator + inpString).get();
        List<String> inputFormatted = Arrays.asList(allInp.split(";")).stream().map(inp -> inp + ";").collect(Collectors.toList());

        for (int position = 0; position < inputFormatted.size(); position++) {

            String commandLine = inputFormatted.get(position);
            if (commandLine.isBlank()) {
                continue;
            }

            //adding new list of token from current line to global list
            List<Token> tokensInLine = new ArrayList<>();
            tokenList.add(tokensInLine);
//            commandLine = prepareString(commandLine);
//            System.out.println(commandLine);

            String acc = "";
            for (int i = commandLine.length() - 1; i >= 0; i--) {
                char c = commandLine.charAt(i);

                //check if char is already can be atomic token
                Optional<Token> atomic = detectAtomic(c);
                if (atomic.isPresent()) {
                    tokensInLine.add(atomic.get());
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
                        tokensInLine.add(new Token(key, currentValue));
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
        pairLexems.add(new PairLexem("ASSIGN", "VARIABLE"));
        pairLexems.add(new PairLexem("ASSIGN", "OPEN_BRACKET"));

        pairLexems.add(new PairLexem("DIGIT", "ADD"));
        pairLexems.add(new PairLexem("DIGIT", "MINUS"));
        pairLexems.add(new PairLexem("DIGIT", "MULT"));
        pairLexems.add(new PairLexem("DIGIT", "DIVIDE"));

        pairLexems.add(new PairLexem("ADD", "DIGIT"));
        pairLexems.add(new PairLexem("MINUS", "DIGIT"));
        pairLexems.add(new PairLexem("MULT", "DIGIT"));
        pairLexems.add(new PairLexem("DIVIDE", "DIGIT"));

        pairLexems.add(new PairLexem("ADD", "VARIABLE"));
        pairLexems.add(new PairLexem("MINUS", "VARIABLE"));
        pairLexems.add(new PairLexem("DIVIDE", "VARIABLE"));
        pairLexems.add(new PairLexem("MULT", "VARIABLE"));

        pairLexems.add(new PairLexem("VARIABLE", "ADD"));
        pairLexems.add(new PairLexem("VARIABLE", "MINUS"));
        pairLexems.add(new PairLexem("VARIABLE", "MULT"));
        pairLexems.add(new PairLexem("VARIABLE", "DIVIDE"));

        pairLexems.add(new PairLexem("ADD", "OPEN_BRACKET"));
        pairLexems.add(new PairLexem("MINUS", "OPEN_BRACKET"));
        pairLexems.add(new PairLexem("MULT", "OPEN_BRACKET"));
        pairLexems.add(new PairLexem("DIVIDE", "OPEN_BRACKET"));

        pairLexems.add(new PairLexem("OPEN_BRACKET", "VARIABLE"));
        pairLexems.add(new PairLexem("OPEN_BRACKET", "DIGIT"));

        pairLexems.add(new PairLexem("VARIABLE", "CLOSE_BRACKET"));
        pairLexems.add(new PairLexem("DIGIT", "CLOSE_BRACKET"));

        pairLexems.add(new PairLexem("CLOSE_BRACKET", "ADD"));
        pairLexems.add(new PairLexem("CLOSE_BRACKET", "MINUS"));
        pairLexems.add(new PairLexem("CLOSE_BRACKET", "MULT"));
        pairLexems.add(new PairLexem("CLOSE_BRACKET", "DIVIDE"));


        pairLexems.add(new PairLexem("CLOSE_BRACKET", "END"));
        pairLexems.add(new PairLexem("DIGIT", "END"));
        pairLexems.add(new PairLexem("VARIABLE", "END"));

        pairLexems.add(new PairLexem("END", "KEY_WORD_VAR"));
        pairLexems.add(new PairLexem("END", "VARIABLE"));
        pairLexems.add(new PairLexem("END", "PRINT"));

        pairLexems.add(new PairLexem("PRINT", "VARIABLE"));
        pairLexems.add(new PairLexem("PRINT", "DIGIT"));

        // IF
        pairLexems.add(new PairLexem("IF", "OPEN_BRACKET"));
        pairLexems.add(new PairLexem("OPEN_BRACKET", "DIGIT"));
        pairLexems.add(new PairLexem("OPEN_BRACKET", "VARIABLE"));
        pairLexems.add(new PairLexem("VARIABLE", "LESS"));
        pairLexems.add(new PairLexem("VARIABLE", "MORE"));
        pairLexems.add(new PairLexem("LESS", "VARIABLE"));
        pairLexems.add(new PairLexem("LESS", "DIGIT"));
        pairLexems.add(new PairLexem("DIGIT", "LESS"));
        pairLexems.add(new PairLexem("DIGIT", "MORE"));
        pairLexems.add(new PairLexem("MORE", "DIGIT"));
        pairLexems.add(new PairLexem("MORE", "VARIABLE"));
        pairLexems.add(new PairLexem("DIGIT", "CLOSE_BRACKET"));
        pairLexems.add(new PairLexem("CLOSE_BRACKET", "OPEN_BRACE"));
        pairLexems.add(new PairLexem("VARIABLE", "CLOSE_BRACKET"));
        pairLexems.add(new PairLexem("OPEN_BRACE", "PRINT"));
        pairLexems.add(new PairLexem("OPEN_BRACE", "CLOSE_BRACE"));

        pairLexems.add(new PairLexem("OPEN_BRACE", "KEY_WORD_VAR"));
        pairLexems.add(new PairLexem("OPEN_BRACE", "VARIABLE"));

        pairLexems.add(new PairLexem("DIGIT", "CLOSE_BRACE"));
        pairLexems.add(new PairLexem("VARIABLE", "CLOSE_BRACE"));
        pairLexems.add(new PairLexem("CLOSE_BRACE", "END"));
        pairLexems.add(new PairLexem("END", "IF"));

        // WHILE
        pairLexems.add(new PairLexem("WHILE", "OPEN_BRACKET"));
        pairLexems.add(new PairLexem("END", "WHILE"));

        // LINKED LIST
        pairLexems.add(new PairLexem("END", "LINKED_LIST"));
        pairLexems.add(new PairLexem("LINKED_LIST", "VARIABLE"));

        pairLexems.add(new PairLexem("VARIABLE", "LINKED_LIST_DELETE"));
        pairLexems.add(new PairLexem("VARIABLE", "LINKED_LIST_ADD"));
        pairLexems.add(new PairLexem("VARIABLE", "LINKED_LIST_CONTAINS"));

        pairLexems.add(new PairLexem("LINKED_LIST_PRINTLL", "VARIABLE"));

        pairLexems.add(new PairLexem("LINKED_LIST_ADD", "VARIABLE"));
        pairLexems.add(new PairLexem("LINKED_LIST_DELETE", "VARIABLE"));
        pairLexems.add(new PairLexem("LINKED_LIST_CONTAINS", "VARIABLE"));
        pairLexems.add(new PairLexem("LINKED_LIST_ADD", "DIGIT"));
        pairLexems.add(new PairLexem("LINKED_LIST_DELETE", "DIGIT"));
        pairLexems.add(new PairLexem("LINKED_LIST_CONTAINS", "DIGIT"));


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

        for (Token token : tokenList) {
            switch (token.getType()) {
                case "OPEN_BRACKET": {
                    openBrackets++;
                    break;
                }
                case "CLOSE_BRACKET": {
                    closeBrackets++;
                    break;
                }
                default:
                    break;
            }
        }
        if (openBrackets == closeBrackets) {
            return true;
        } else {
            return false;
        }
    }
}


class ConvertToPolishNotation {
    public int priority(Token token) {
        if (token.getType().equals("DIVIDE") || token.getType().equals("MULT")) {
            return 10;
        }
        if (token.getType().equals("ADD") || token.getType().equals("MINUS")) {
            return 9;
        }
        if (token.getType().equals("LESS") || token.getType().equals("MORE") ) {
            return 6;
        }
        if (token.getType().equals("ASSIGN") || token.getType().equals("PRINT") ) {
            return 2;
        } else {
            return 0;
        }

    }

    public List<Token> getPolish(List<Token> nePolish) {
        boolean toPrint = false;

//        List<Token> preparedNePolish = prepareTokensByReverse(nePolish);

        List<Token> alreadyPolish = new ArrayList<Token>();
        Stack<Token> stack = new Stack<Token>();

        for (Token token : nePolish) {
            if (token.getType().equals("PRINT")) {
                toPrint = true;
            } else if (token.getType().equals("END")) {
                while (stack.size() != 0) {
                    alreadyPolish.add(stack.pop());
                }
            } else if (token.getType().equals("DIGIT") || token.getType().equals("VARIABLE")) {
                alreadyPolish.add(token);
            } else if (token.getType().equals("OPEN_BRACKETS")) {
                stack.push(token);
            } else if (token.getType().equals("CLOSE_BRACKETS")) {
                while (!stack.peek().getType().equals("OPEN_BRACKET") || stack.size() == 0) {
                    alreadyPolish.add(stack.pop());
                    stack.pop();
                }
            }
            //else if (token.getType() == "")
            else if (token.getType().equals("ADD") || token.getType().equals("MINUS") || token.getType().equals("DIVIDE")
                    || token.getType().equals("MULT") || token.getType().equals("ASSIGN") ||
                    token.getType().equals("LESS") || token.getType().equals("MORE") ) {
                if (stack.size() == 0) {
                    stack.push(token);
                } else if (stack.peek().getType().equals("OPEN_BRACKETS")) {
                    stack.push(token);
                } else if (priority(token) <= priority(stack.peek())) {
                    while (stack.size() != 0 && priority(token) <= priority(stack.peek())) {
                        if (stack.peek().getType().equals("OPEN_BRACKET")) {
                            break;
                        }
                        alreadyPolish.add(stack.pop());
                    }
                    stack.push(token);
                } else if (priority(token) > priority(stack.peek())) {
                    stack.push(token);
                }
            }


        }
        while (stack.size() != 0) {
            alreadyPolish.add(stack.pop());
        }
        if (toPrint) {
            alreadyPolish.add(new Token("PRINT", "PRINT"));
        }
        return alreadyPolish;
    }

    public List<Token> prepareTokensByReverse(List<Token> nePolish) {
        List<Token> preparedNePolish = new ArrayList<>();

        List<Token> buffer = new ArrayList<>();
        for (int i = 0; i < nePolish.size(); i++) {
            if (i == 0) continue;
            if (nePolish.get(i).getType().equals("END")) {
                buffer.add(0, new Token("END", ";"));
                Collections.reverse(buffer);
                preparedNePolish.addAll(buffer);
                buffer = new ArrayList<>();
                continue;
            }
            if (i == nePolish.size() - 1) {
                buffer.add(0, new Token("END", ";"));
                buffer.add(nePolish.get(i));
                Collections.reverse(buffer);
                preparedNePolish.addAll(buffer);
                buffer = new ArrayList<>();
                continue;
            }

            buffer.add(nePolish.get(i));
        }
        return preparedNePolish;
    }

}

class Machine {
    private Stack<VarHolder> stack = new Stack<VarHolder>();
    private List<VarHolder> vars = new ArrayList<VarHolder>();
    private List<MyLinkedList> lists = new ArrayList<>();

    boolean isExist(String var) {
        for (VarHolder v : vars) {
            if (v.name.equals(var)) {
                return true;
            }
        }
        return false;
    }

    double getVarHolderByName(String type) {
        for (VarHolder var : vars) {
            if (var.name.equals(type)) {
                return var.value;
            }
        }
        return 0f;
    }

    int getVarHolderByNameIndex(String type) {
        for (int i = 0; i < vars.size(); i++) {
            if (vars.get(i).name.equals(type)) {
                return i;
            }
        }
        return -1;
    }

//    public boolean checkForStatementResult() {
//
//    }


    public String vmachine(List<Token> polishString) {
        String output = "";

        if (polishString.stream().filter(token -> token.getType().equals("IF")).count() > 0){
            output = produceIfLogic(polishString);
            return output;
        }
        if (polishString.stream().filter(token -> token.getType().equals("WHILE")).count() > 0){
            output = produceWhileLogic(polishString);
            return output;
        }

        if (polishString.stream().filter(token -> token.getType().equals("LINKED_LIST")).count() > 0){
            String nameOfList = polishString.stream().filter(token -> token.getType().equals("VARIABLE")).findFirst().get().getValue();
            lists.add(new MyLinkedList(nameOfList));
            return output;
        }

        if(polishString.stream().filter(token -> token.getType().equals("LINKED_LIST_ADD")).count() > 0){
            MyLinkedList list = getMyListByName(polishString.get(0).getValue());
            list.add(Double.parseDouble(polishString.get(2).getValue()));
            return output;
        }

        if(polishString.stream().filter(token -> token.getType().equals("LINKED_LIST_DELETE")).count() > 0){
            MyLinkedList list = getMyListByName(polishString.get(0).getValue());
            list.delete(Double.parseDouble(polishString.get(2).getValue()));
            return output;
        }

        if(polishString.stream().filter(token -> token.getType().equals("LINKED_LIST_CONTAINS")).count() > 0){
            MyLinkedList list = getMyListByName(polishString.get(0).getValue());
            output = "Element " + polishString.get(2).getValue() + " is in List: "+list.contains(Double.parseDouble(polishString.get(2).getValue()));
            return output;
        }

        if(polishString.stream().filter(token -> token.getType().equals("LINKED_LIST_PRINTLL")).count() > 0){
            MyLinkedList list = getMyListByName(polishString.get(1).getValue());
            list.print();
            return output;
        }

        for (Token token : polishString) {
            switch (token.getType()) {
                case "PRINT":
                    output += stack.pop().value;
                    break;
                case "VARIABLE": {
                    if (!isExist(token.getValue())) {
                        stack.push(new VarHolder(token.getValue(), 0f));
                        vars.add(new VarHolder(token.getValue(), 0f));
                    } else {
                        stack.push(new VarHolder(token.getValue(), getVarHolderByName(token.getValue())));
                    }
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
                case "MINUS": {
                    double minusOp1 = stack.pop().value;
                    double minusOp2 = stack.pop().value;
                    stack.push(new VarHolder("", (minusOp2 - minusOp1)));
                    break;
                }
                case "MULT": {
                    double multOp1 = stack.pop().value;
                    double multOp2 = stack.pop().value;
                    stack.push(new VarHolder("", (multOp1 * multOp2)));
                    break;
                }
                case "DIVIDE": {
                    double divideOp1 = stack.pop().value;
                    double divideOp2 = stack.pop().value;
                    stack.push(new VarHolder("", (divideOp2 / divideOp1)));
                    break;
                }
                case "ASSIGN": {
                    double opb = stack.pop().value;
                    String opa = stack.pop().name;

                    int a = getVarHolderByNameIndex(opa);
                    vars.set(a, new VarHolder(opa, opb));
                    break;
                }
                case "LESS":{
                    double opLess1 = stack.pop().value;
                    double opLess2 = stack.pop().value;
                    stack.push(new VarHolder("", opLess2<opLess1 ? 1 : -1 ));
                    break;
                }
                case "MORE":{
                    double opMore1 = stack.pop().value;
                    double opMore2 = stack.pop().value;
                    stack.push(new VarHolder("", opMore2>opMore1 ? 1 : -1 ));
                    break;
                }
                case "END": {
                    stack.clear();
                    break;
                }
                default: {
                    break;
                }
            }
        }
        return output;
    }

    private MyLinkedList getMyListByName(String listName) {
        return lists.stream().filter(list -> list.name.equals(listName)).findFirst().get();
    }


    private String produceWhileLogic(List<Token> polishString) {
        Token print = new Token("PRINT", "PRINT");
        Token end = new Token("END", ";");

        List<Token> betweenBrackets = getConditionTokensExpression(polishString);
        List<Token> betweenCurledBrackets = getBodyBetweenCurledBrackets(polishString);

        betweenBrackets = betweenBrackets.stream().filter(token -> !token.getType().equals("END")).collect(Collectors.toList());
        betweenBrackets.add(print);
        betweenBrackets.add(end);

        betweenBrackets.add(end);

        Boolean resOfIfConditionCheck = Double.parseDouble(this.vmachine(betweenBrackets)) > 0;

        String res = "";
        while (resOfIfConditionCheck){

            res += this.vmachine(betweenCurledBrackets) + " ";
            resOfIfConditionCheck = Double.parseDouble(this.vmachine(betweenBrackets)) > 0;
        }

        return res;
    }

    private String produceIfLogic(List<Token> polishString) {
        Token print = new Token("PRINT", "PRINT");
        Token end = new Token("END", ";");

        List<Token> betweenBrackets = getConditionTokensExpression(polishString);
        List<Token> betweenCurledBrackets = getBodyBetweenCurledBrackets(polishString);

        betweenBrackets = betweenBrackets.stream().filter(token -> !token.getType().equals("END")).collect(Collectors.toList());
        betweenBrackets.add(print);
        betweenBrackets.add(end);

        betweenBrackets.add(end);

        Boolean resOfIfConditionCheck = Double.parseDouble(this.vmachine(betweenBrackets)) > 0;

        if(resOfIfConditionCheck){
            return this.vmachine(betweenCurledBrackets);
        }

        return "";
    }

    private List<Token> getBodyBetweenCurledBrackets(List<Token> polishString) {
        //get body and convert it to polis
        int indexOfCOB = polishString.indexOf(polishString.stream().filter(t-> t.getType().equals("OPEN_BRACE")).findFirst().get());
        int indexOfCCB = polishString.indexOf(polishString.stream().filter(t-> t.getType().equals("CLOSE_BRACE")).findFirst().get());
        return polishString.subList(indexOfCOB+1,indexOfCCB);// get all that  inside {} in IF;
    }

    private List<Token> getConditionTokensExpression(List<Token> polishString) {
        //get condition and convert it to polis
        int indexOfOB = polishString.indexOf(polishString.stream().filter(t-> t.getType().equals("OPEN_BRACKET")).findFirst().get());
        int indexOfCB = polishString.indexOf(polishString.stream().filter(t-> t.getType().equals("CLOSE_BRACKET")).findFirst().get());
        return polishString.subList(indexOfOB+1,indexOfCB);// get all that  inside () in IF
    }

}

