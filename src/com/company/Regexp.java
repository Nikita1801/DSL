package com.company;

import java.util.*;
import java.util.regex.*;

public class Regexp {


    private static Map<String, Pattern> lex = new HashMap<>();

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
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // String testString = "VAR size = 100 + 1";


        List<String> stringList = new LinkedList<>();
        List<Token> tokenList = new LinkedList<>();

        for (int word = 0; true; word++){
            stringList.add(word, scanner.nextLine());
            Boolean checkIsEmpty = stringList.get(word).isEmpty();
            if (checkIsEmpty){
                stringList.remove(word);
                break;
            }
        }
        for (int position = 0; position < stringList.size(); position++) {
            for (String key : lex.keySet()) {
                Matcher matcher;
                matcher= lex.get(key).matcher(stringList.get(position));


                while (matcher.find()) {
                    String currentValue = stringList.get(position).substring(matcher.start(), matcher.end());
                    tokenList.add(new Token(key, currentValue));


                }

            }
        }
        for (Token token: tokenList) {
            System.out.println(token);
        }

    }
}

class Token {

    private String type;
    private String value;

    public Token(String type, String value){
        this.type = type;
        this.value = value;

    }

    @Override
    public String toString() {
        return "Token{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                '}';

    }

}
