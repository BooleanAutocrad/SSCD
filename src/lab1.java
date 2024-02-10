import java.io.File;
import java.util.*;
import java.util.function.BiFunction;

enum OpcodeType {
    IS, DL, AD
}

class Instruction {
    private final String opcodeValue;
    private final OpcodeType opcodeType;

    public Instruction(String opcodeValue, OpcodeType opcodeType) {
        this.opcodeValue = opcodeValue;
        this.opcodeType = opcodeType;
    }

    public String getOpcodeValue() {
        return opcodeValue;
    }

    public OpcodeType getOpcodeType() {
        return opcodeType;
    }
}

class Register {
    private String mcCode;

    public Register(String mcCode) {
        this.mcCode = mcCode;
    }

    public String getMcCode() {
        return mcCode;
    }

    public void setMcCode(String mcCode) {
        this.mcCode = mcCode;
    }
}

class Condition {
    private String mcCode;

    public Condition(String mcCode) {
        this.mcCode = mcCode;
    }

    public String getMcCode() {
        return mcCode;
    }

    public void setMcCode(String mcCode) {
        this.mcCode = mcCode;
    }
}

public class lab1 {
    private static final String FILE_PATH = "E:\\SSCD\\LAB1\\assemblerCode.txt";
    private static final String START_OPCODE = "START";
    private static final String END_OPCODE = "END";
    private static final String ORIGIN_OPCODE = "ORIGIN";
    private static final String EQU_OPCODE = "EQU";
    private static final String DS_OPCODE = "DS";
    private static Integer locationCounter;
    private static String tokenAtLabelSide;


    public static void main(String[] args) {
        HashMap<String, Instruction> opcodeTable = new HashMap<>();
        HashMap<String, Register> regTable = new HashMap<>();
        HashMap<String, Condition> conditionTable = new HashMap<>();
        HashMap<String, String> symbolTable = new HashMap<>();
        Map<String, BiFunction<Integer, Integer, Integer>> operationMap = new HashMap<>();


        initializeTables(opcodeTable, regTable, conditionTable, operationMap);

        processFile(opcodeTable, regTable, symbolTable,operationMap);

        System.out.println("Complete Symbol Table:");
        printSymbolTable(symbolTable);
    }

    private static void initializeTables(HashMap<String, Instruction> opcodeTable,
                                         HashMap<String, Register> regTable,
                                         HashMap<String, Condition> conditionTable,
                                         Map<String, BiFunction<Integer, Integer, Integer>> operationMap) {
        opcodeTable.put("STOP", new Instruction("00", OpcodeType.IS));
        opcodeTable.put("ADD", new Instruction("01", OpcodeType.IS));
        opcodeTable.put("SUB", new Instruction("02", OpcodeType.IS));
        opcodeTable.put("MULT", new Instruction("03", OpcodeType.IS));
        opcodeTable.put("MOVER", new Instruction("04", OpcodeType.IS));
        opcodeTable.put("MOVEM", new Instruction("05", OpcodeType.IS));
        opcodeTable.put("COMP", new Instruction("06", OpcodeType.IS));
        opcodeTable.put("BC", new Instruction("07", OpcodeType.IS));
        opcodeTable.put("DIV", new Instruction("08", OpcodeType.IS));
        opcodeTable.put("READ", new Instruction("09", OpcodeType.IS));
        opcodeTable.put("PRINT", new Instruction("10", OpcodeType.IS));
        opcodeTable.put("DC", new Instruction("01", OpcodeType.DL));
        opcodeTable.put("DS", new Instruction("02", OpcodeType.DL));
        opcodeTable.put("START", new Instruction("01", OpcodeType.AD));
        opcodeTable.put("END", new Instruction("02", OpcodeType.AD));
        opcodeTable.put("ORIGIN", new Instruction("03", OpcodeType.AD));
        opcodeTable.put("EQU", new Instruction("04", OpcodeType.AD));
        opcodeTable.put("LTORG", new Instruction("05", OpcodeType.AD));

        regTable.put("AREG", new Register("1"));
        regTable.put("BREG", new Register("2"));
        regTable.put("CREG", new Register("3"));
        regTable.put("DREG", new Register("4"));

        conditionTable.put("LT", new Condition("1"));
        conditionTable.put("LE", new Condition("2"));
        conditionTable.put("EQ", new Condition("3"));
        conditionTable.put("GT", new Condition("4"));
        conditionTable.put("GE", new Condition("5"));
        conditionTable.put("ANY(NE)", new Condition("6"));

        operationMap.put("+", Integer::sum);
        operationMap.put("-", (a, b) -> a - b);
        operationMap.put("*", (a, b) -> a * b);
        operationMap.put("/", (a, b) -> {
            if (b != 0) {
                return a / b;
            } else {
                System.out.print(" Error: Division by zero.");
                return 0;
            }
        });
    }

    private static void processFile(HashMap<String, Instruction> opcodeTable,
                                    HashMap<String, Register> regTable,
                                    HashMap<String, String> symbolTable,
                                    Map<String, BiFunction<Integer, Integer, Integer>> operationMap) {
        try (Scanner scanner = new Scanner(new File(FILE_PATH))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                processLine(line, opcodeTable, regTable, symbolTable,operationMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processLine(String line, HashMap<String, Instruction> opcodeTable,
                                    HashMap<String, Register> regTable,
                                    HashMap<String, String> symbolTable,
                                    Map<String, BiFunction<Integer, Integer, Integer>> operationMap) {


        StringTokenizer tokenizer = new StringTokenizer(line, " ");
        boolean isLastToken = false;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            isLastToken = !tokenizer.hasMoreTokens();
            if (opcodeTable.containsKey(token)) {
                if (token.equals(START_OPCODE) && tokenizer.hasMoreTokens()) {
                    String nextToken = tokenizer.nextToken();
                    locationCounter = Integer.valueOf(nextToken);
                    Instruction instruction = opcodeTable.get(token);
                    System.out.print("    (" + instruction.getOpcodeType() + " , " + instruction.getOpcodeValue() + ")");
                    processConstant(nextToken);
                } else if (token.equals(EQU_OPCODE)) {
                    Instruction instruction = opcodeTable.get(token);
                    System.out.print("    (" + instruction.getOpcodeType() + " , " + instruction.getOpcodeValue() + ")");
                    String nextToken = tokenizer.nextToken();
                    processSymbol(nextToken,symbolTable);
                    String address = symbolTable.get(nextToken);
                    symbolTable.put(tokenAtLabelSide, address);
                } else if (token.equals(DS_OPCODE)) {
                    Instruction instruction = opcodeTable.get(token);
                    System.out.print(locationCounter + " (" + instruction.getOpcodeType() + " , " + instruction.getOpcodeValue() + ")");
                    String nextToken = tokenizer.nextToken();
                    locationCounter = locationCounter + Integer.parseInt(nextToken);
                    processConstant(nextToken);
                } else if (token.equals(ORIGIN_OPCODE)) {
                    Instruction instruction = opcodeTable.get(token);
                    System.out.print("    (" + instruction.getOpcodeType() + " , " + instruction.getOpcodeValue() + ")");
                    String nextToken = tokenizer.nextToken();
                    processSymbol(nextToken,symbolTable);
                    String address = symbolTable.get(nextToken);
                    if (address != null) {
                        try {
                            int addressInt =  Integer.parseInt(address);
                            String operator = tokenizer.nextToken();
                            int operandInt = Integer.parseInt(tokenizer.nextToken());
                            locationCounter = operationMap.get(operator).apply(addressInt, operandInt);
                        } catch (NumberFormatException e) {
                            System.out.print("Error: Invalid address format.");
                        }
                    } else {
                        System.out.print("Error: Symbol not found in symbol table.");
                    }
                } else if (token.equals(END_OPCODE)) {
                    Instruction instruction = opcodeTable.get(token);
                    System.out.print("    (" + instruction.getOpcodeType() + " , " + instruction.getOpcodeValue() + ")");
                } else {
                    if (locationCounter != null) {
                        Instruction instruction = opcodeTable.get(token);
                        System.out.print(locationCounter + " (" + instruction.getOpcodeType() + " , " + instruction.getOpcodeValue() + ")");
                        locationCounter++;
                    } else {
                        System.out.println("Error: Location counter is not initialized.");
                    }
                }
            } else if (regTable.containsKey(token)) {
                Register register = regTable.get(token);
                System.out.print(register.getMcCode());
            } else if (token.matches("\\d+")) {
                System.out.print("(C, " + token + ")");
            } else {
                if (isLastToken) {
                    if (!symbolTable.containsKey(token)) {
                        symbolTable.put(token, "-1");
                    }
                    processSymbol(token,symbolTable);
                } else {
                    symbolTable.put(token, String.valueOf(locationCounter));
                    tokenAtLabelSide = token;
                }
            }
        }
        System.out.println();
    }

    private static void processConstant(String token) {
        System.out.print("(C , " + token + ")");
    }
    private static void processSymbol(String token, HashMap<String, String> symbolTable) {
        int index = new ArrayList<>(symbolTable.keySet()).indexOf(token);
        index = index + 1;
        System.out.print("(S , " + index + ")");
    }

    private static void printSymbolTable(Map<String, String> symbolTable) {
        for (Map.Entry<String, String> entry : symbolTable.entrySet()) {
            System.out.println("label: " + entry.getKey() + ", Address: " + entry.getValue());
        }
    }
}
