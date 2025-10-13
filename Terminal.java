import java.util.Scanner;

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


    public Terminal() {
        parser = new Parser();
    }

    public String pwd(){
        return System.getProperty("user.dir");
    }

    public static void main(String[] args){
            Terminal terminal = new Terminal();
            Scanner sc = new Scanner(System.in);

            while(true){
                System.out.print("> ");

                String input = sc.nextLine().trim();
                if(input.equals("exit")){
                    System.exit(0);
                }

                terminal.parser.parse(input);
                String command = terminal.parser.getCommandName();
                String[] commandArgs = terminal.parser.getArgs();

                switch(command){
                    case "pwd":
                        String path = terminal.pwd();
                        System.out.println(path);
                        break;
                    default:
                        System.out.println(command + " is not a valid command.");
                }
            }
        }
    }
