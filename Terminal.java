class Parser {
    private String commandName;
    private String[] args;

     // This method divides the input into commandName and args
    // Returns true if parsing succeeds, false otherwise
    public boolean parse (String input){
        if(input == null || input.trim().isEmpty()){
            return false;
        }
        String[] parts = input.trim().split("\\s+");
        commandName = parts[0];
        if(parts.length > 1){
            args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, parts.length - 1);
        } else {
            args = new String[0];
        }
        return true;
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }
}

public class Terminal {
    Parser parser;
}