package com.company;

import java.util.*;
import java.util.regex.*;

public class Regexp {


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


        charsToRemove.add(" ");
    }


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
                        acc += commandLine.charAt(i-k);
                    }else {
                        i=i-k;
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
                acc="";

            }




        }
        for (Token token : tokenList) {
            System.out.println(token);
        }

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

    @Override
    public String toString() {
        return "Token{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                '}';

    }

}
