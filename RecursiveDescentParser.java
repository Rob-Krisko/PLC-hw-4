import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class RecursiveDescentParser {
    private List<String> tokens;
    private int index;

    public RecursiveDescentParser(List<String> tokens) {
        this.tokens = tokens;
        this.index = 0;
    }

    public static List<String> tokenize(String input) {
        String[] delimiters = {"(", ")", "{", "}", ";", "+", "-", "*", "/", "%", ">", "<", ">=", "<=", "==", "!=", "&&", "||", "="};
        for (String delimiter : delimiters) {
            input = input.replace(delimiter, " " + delimiter + " ");
        }
        input = input.trim().replaceAll("\\s+", " ");
        List<String> tokens = new ArrayList<>(Arrays.asList(input.split(" ")));
    
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.matches("\\d+")) {
                tokens.set(i, "INT_LIT:" + token);
            } else if (token.matches("\\d+\\.\\d+")) {
                tokens.set(i, "FLOAT_LIT:" + token);
            } else if (token.matches("[a-zA-Z]+")) {
                tokens.set(i, "ID:" + token);
            } else {
                switch (token) {
                    case "(":
                        tokens.set(i, "LEFTPAREN");
                        break;
                    case ")":
                        tokens.set(i, "RIGHTPAREN");
                        break;
                    case "=":
                        tokens.set(i, "EQUALS");
                        break;
                    case "{":
                        tokens.set(i, "LEFTBRACKET");
                        break;
                    case "}":
                        tokens.set(i, "RIGHTBRACKET");
                        break;
                    case ";":
                        tokens.set(i, "SEMICOLON");
                        break;
                    case "+":
                        tokens.set(i, "PLUS");
                        break;
                    case "-":
                        tokens.set(i, "MINUS");
                        break;
                    case "*":
                        tokens.set(i, "MULTIPLY");
                        break;
                    case "/":
                        tokens.set(i, "DIVIDE");
                        break;
                    case "%":
                        tokens.set(i, "MODULO");
                        break;
                    case ">":
                        tokens.set(i, "GREATER");
                        break;
                    case "<":
                        tokens.set(i, "LESS");
                        break;
                    case ">=":
                        tokens.set(i, "GREATEREQUAL");
                        break;
                    case "<=":
                        tokens.set(i, "LESSEQUAL");
                        break;
                    case "==":
                        tokens.set(i, "EQUALTO");
                        break;
                    case "!=":
                        tokens.set(i, "NOTEQUAL");
                        break;
                    case "&&":
                        tokens.set(i, "AND");
                        break;
                    case "||":
                        tokens.set(i, "OR");
                        break;
                    default:
                        tokens.set(i, token);
                }
            }
        }
    
        return tokens;
    }
    
    

    private boolean declareStmt() {
        if (accept("int") || accept("float")) {
            if (!isIdentifier(peek())) {
                return false;
            }
            index++;
    
            while (accept(",")) {
                if (!isIdentifier(peek())) {
                    return false;
                }
                index++;
            }
    
            return true;
        }
    
        return false;
    }
    
    public boolean parse() {
        if (!stmt()) {
            return false;
        }
        return index >= tokens.size();
    }

    private boolean stmt() {
        int startIndex = index;
    
        if (ifStatement() || whileLoop() || block() || assignStmt() || declareStmt()) {
            if (!accept("SEMICOLON")) {
                index = startIndex;
                return false;
            }
            return true;
        }
    
        index = startIndex;
        return false;
    }
    
    

    private boolean ifStatement() {
        int startIndex = index;
        if (accept("if") && accept("(") && boolExpr() && accept(")") && stmt()) {
            if (accept("else")) {
                if (!stmt()) {
                    index = startIndex;
                    return false;
                }
            }
            return true;
        }
        index = startIndex;
        return false;
    }

    private boolean whileLoop() {
        int startIndex = index;
        if (accept("while") && accept("(") && boolExpr() && accept(")") && stmt()) {
            return true;
        }
        index = startIndex;
        return false;
    }

    private boolean assignStmt() {
        if (index >= tokens.size()) {
            return false;
        }

        if (!isIdentifier(tokens.get(index))) {
            return false;
        }
        index++;

        if (index >= tokens.size() || !tokens.get(index).equals("=")) {
            return false;
        }
        index++;

        if (index >= tokens.size()) {
            return false;
        }

        if (!expr()) {
            return false;
        }

        return true;
    }

    private boolean isIdentifier(String token) {
        return token.startsWith("ID:");
    }

    private boolean block() {
        int startIndex = index;
        if (accept("{")) {
            while (stmt()) {}
            if (accept("}")) {
                return true;
            }
        }
        index = startIndex;
        return false;
    }

    private boolean expr() {
        if (!term()) {
            return false;
        }
        while (accept("+") || accept("-")) {
            if (!term()) {
                return false;
            }
        }
        return true;
    }

    private boolean term() {
        if (!factor()) {
            return false;
        }
        while (accept("*") || accept("/") || accept("%")) {
            if (!factor()) {
                return false;
            }
        }
        return true;
    }

    private boolean factor() {
        if (accept("ID") || accept("INT_LIT") || accept("FLOAT_LIT")) {
            return true;
        }
        if (accept("LEFTPAREN")) {
            if (expr() && accept("RIGHTPAREN")) {
                return true;
            }
        }
        return false;
    }
    
    
    private boolean boolExpr() {
        return bterm();
    }

    private boolean bterm() {
        if (!band()) {
            return false;
        }
    
        while (accept("==") || accept("!=")) {
            if (!band()) {
                return false;
            }
        }
        return true;
    }

    private boolean band() {
        if (!bor()) {
            return false;
        }
    
        while (accept("&&")) {
            if (!bor()) {
                return false;
            }
        }
        return true;
    }
    
    private boolean bor() {
        if (!expr()) {
            return false;
        }
    
        while (accept("OR")) {
            if (!expr()) {
                return false;
            }
        }
        return true;
    }
    

    private String peek() {
        if (index < tokens.size()) {
            return tokens.get(index);
        }
        return null;
    }

    private boolean accept(String tokenName) {
        if (index < tokens.size()) {
            if (tokenName.equals("ID:") || tokenName.equals("INT_LIT:") || tokenName.equals("FLOAT_LIT:")) {
                if (tokens.get(index).startsWith(tokenName)) {
                    index++;
                    return true;
                }
            } else {
                if (tokens.get(index).equals(tokenName)) {
                    index++;
                    return true;
                }
            }
        }
        return false;
    }
    
    
    
    

    public static void main(String[] args) {
        // Read the file name from the user
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the file name:");
        String inputFilePath = scanner.nextLine();
    
        StringBuilder codeBuilder = new StringBuilder();
    
        // Read the file line by line
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                codeBuilder.append(line).append(" ");
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + inputFilePath);
            e.printStackTrace();
            System.exit(1);
        }
    
        String code = codeBuilder.toString();
        List<String> tokens = tokenize(code);
        System.out.println("Input: \n" + code);
        System.out.println("Tokens:");
        for (String token : tokens) {
            System.out.println(token);
        }
    
        RecursiveDescentParser parser = new RecursiveDescentParser(tokens);
        boolean success = parser.parse();
        System.out.println("Parsing result: " + (success ? "Success" : "Failure"));
    }
    
}
