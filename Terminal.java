import java.io.*;
import java.util.Scanner;
import java.util.zip.*;

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

    private void zipHelper(File fileToZip, String fileName, ZipOutputStream zos) throws IOException {
        if (fileToZip.isHidden()) return;

        if (fileToZip.isDirectory()) {
            if (!fileName.endsWith("/")) fileName += "/";
            zos.putNextEntry(new ZipEntry(fileName));
            zos.closeEntry();
            System.out.println("  adding: " + fileName);

            File[] filesInside = fileToZip.listFiles();
            if (filesInside != null) {
                for (File childFile : filesInside) {
                    zipHelper(childFile, fileName + childFile.getName(), zos);
                }
            }
            return;
        }

        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry entry = new ZipEntry(fileName);
            zos.putNextEntry(entry);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
            System.out.println("  adding: " + fileName);
        }
    }

    public void zip(String[] args) throws  IOException {
        if (args.length < 2) {
            System.err.println("zip error: Nothing we can do!");
            return;
        }
        if (args[0].equals("-r") && args.length < 3) {
            String zipName = args[1];
            File fileToZip = new File(args[args.length - 1]);

            if (!fileToZip.exists()) {
                System.err.println("zip error: " + args[args.length - 1] + " does not exist!");
                return;
            }
            try (FileOutputStream fos = new FileOutputStream(zipName);
                 ZipOutputStream zipOut = new ZipOutputStream(fos)){
                    zipHelper(fileToZip, fileToZip.getName(), zipOut);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println("zip completed, created " + zipName);
            return;
        }

        String zipName = args[0];
        try (FileOutputStream fos = new FileOutputStream(zipName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (int i = 1; i < args.length; i++) {
                File fileToZip = new File(args[i]);

                if (!fileToZip.exists() || fileToZip.isDirectory()) {
                    System.out.println("Skipping: " + args[i]);
                    continue;
                }

                try (FileInputStream fis = new FileInputStream(fileToZip)) {
                    ZipEntry entry = new ZipEntry(fileToZip.getName());
                    zos.putNextEntry(entry);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) >= 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                    System.out.println("Added " + fileToZip.getName());
                } catch (IOException e) {
                    System.out.println("Error adding file: " + args[i]);
                }
            }
            System.out.println("Zipped " + zipName + " successfully in " + System.getProperty("user.dir"));
        } catch (IOException e) {
            System.err.println(" zip error: Nothing to do! ");
        }
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
                        System.out.println(terminal.pwd());
                        break;
                    case "zip":
                        try{
                        terminal.zip(commandArgs);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    default:
                        System.out.println(command + " is not a valid command.");
                }
            }
        }
    }
