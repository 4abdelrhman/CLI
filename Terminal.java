import java.io.*;
import java.util.*;
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

        //Handle Directories
        if (fileToZip.isDirectory()) {
            if (!fileName.endsWith("/")) fileName += "/";
            //Add the directory entry to the ZIP file.
            zos.putNextEntry(new ZipEntry(fileName));
            zos.closeEntry();
            System.out.println("  adding: " + fileName);

            // Recurs. files inside the directory
            File[] filesInside = fileToZip.listFiles();
            if (filesInside != null) {
                for (File childFile : filesInside) {
                    zipHelper(childFile, fileName + childFile.getName(), zos);
                }
            }
            return;
        }

        //Handle regular files
        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            //Create a new entry in the ZIP with the file name (user given)
            ZipEntry entry = new ZipEntry(fileName);
            zos.putNextEntry(entry);
            // Read and add the file's bytes in the ZIP output stream
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
        //Handle the
        if (args[0].equals("-r") && args.length >= 3) {
            String zipName = args[1];
            File fileToZip = new File(args[args.length - 1]);

            if (!fileToZip.exists()) {
                System.err.println("zip error: " + args[args.length - 1] + " does not exist!");
                return;
            }
            // create the zip file and add contents in it
            try (FileOutputStream fos = new FileOutputStream(zipName);
                 ZipOutputStream zipOut = new ZipOutputStream(fos)){
                    zipHelper(fileToZip, fileToZip.getName(), zipOut);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println("zip completed, created " + zipName);
            return;
        }

        //handle normal files ( not directory ) ZIP
        String zipName = args[0];
        try (FileOutputStream fos = new FileOutputStream(zipName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            //loop through the provided files
            for (int i = 1; i < args.length; i++) {
                File fileToZip = new File(args[i]);

                // skip missing files
                if (!fileToZip.exists() || fileToZip.isDirectory()) {
                    System.out.println("Skipping: " + args[i]);
                    continue;
                }

                // add each file (the existing ones) in the ZIP
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

    public void unzip(String[] args) throws IOException{
        if( args.length < 1) {
            System.err.println("unzip error: Nothing we can do!");
            return;
        }
        String zipName = args[0];
        File zipFile = new File(args[0]);

        //check if the ZIP file exists
        if ( !zipFile.exists() ) {
            System.err.println("unzip error:" + zipName + " does not exist!");
            return;
        }

        // determine the directory
        File dist = new File(pwd());
        if ( args.length >= 3  && args[1].equals("-d")){
            dist = new File(args[2]);
            if (!dist.exists()) dist.mkdirs();
        }
        System.out.println("Archive: " + zipFile.getName());

        Scanner sc = new Scanner(System.in);

        //open the ZIP file to read from it
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;

            // go through every file or directory in the ZIP file
            while( (entry = zis.getNextEntry()) != null ){
                File newFile = new File(dist, entry.getName());
                //Handle directory entries
                if(entry.isDirectory()){
                    newFile.mkdirs();
                    System.out.println("Inflating: " + newFile.getPath());
                    zis.closeEntry();
                    continue;
                }
                // handle if file already exists
                if(newFile.exists()){
                    System.out.print("replace " + newFile.getName() + "? [y]es/[n]o: ");
                    String resp = sc.nextLine().trim().toLowerCase();
                    if( !resp.equals("y") ){
                        zis.closeEntry();
                        continue;
                    }else{
                        new File(newFile.getParent()).mkdirs();
                    }
                }
                // write file contents from the ZIP to the disk
                try (FileOutputStream fos = new FileOutputStream(newFile)){
                    byte[] buffer = new byte[1024];
                    int length;
                    while ( (length = zis.read(buffer)) > 0 ) {
                        fos.write(buffer, 0, length);
                    }
                }
                System.out.println("    inflating: " + newFile.getPath());
                zis.closeEntry();
            }
        }
        System.out.println("Unzipped completed, extracted to " + dist.getAbsolutePath());
    }


    public void cat(String[] args) {
        if (args.length == 0) {
            System.err.println("cat: missing file operand");
            return;
        }

        for (String filename : args) {
            File file = new File(filename);
            if (!file.exists() || file.isDirectory()) {
                System.err.println("cat: " + filename + ": No such file");
                continue;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.err.println("cat: error reading " + filename);
            }
        }
    }

    public void wc(String[] args) {
        if (args.length == 0) {
            System.err.println("wc: missing file operand");
            return;
        }

        for (String filename : args) {
            File file = new File(filename);
            if (!file.exists() || file.isDirectory()) {
                System.err.println("wc: " + filename + ": No such file");
                continue;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                int lines = 0, words = 0, chars = 0;
                String line;
                while ((line = br.readLine()) != null) {
                    lines++;
                    words += line.trim().isEmpty() ? 0 : line.trim().split("\\s+").length;
                    chars += line.length() + 1; // +1 for newline
                }
                System.out.println(lines + " " + words + " " + chars + " " + filename);
            } catch (IOException e) {
                System.err.println("wc: error reading " + filename);
            }
        }
    }

    public void echo(String[] args) {
        if (args.length == 0) {
            System.out.println();
            return;
        }
        System.out.println(String.join(" ", args));
    }

    public void touch(String[] args) {
        if (args.length == 0) {
            System.out.println("touch: missing file name");
            return;
        }
        for (String file_name : args) {
            File file = new File(file_name);
            try {
                if (file.createNewFile()) {
                    System.out.println("file created: " + file_name);
                } else {
                    System.out.println("file already exists: " + file_name);
                }
            } catch (IOException e) {
                System.out.println("touch: cannot create file '" + file_name + "'");
            }
        }
    }

    public void rm(String[] args) {
        if (args.length == 0) {
            System.out.println("rm: missing file name");
            return;
        }

        for (String file_name : args) {
            File file = new File(file_name);
            if (file.exists() && file.isFile()) {
                if (file.delete()) {
                    System.out.println("file deleted: " + file_name);
                } else {
                    System.out.println("rm: failed to delete " + file_name);
                }
            } else {
                System.out.println("rm: cannot remove '" + file_name + "': file not exist");
            }
        }
    }

    public void cp(String[] args) {
        if (args.length != 2) {
            System.out.println("cp: requires 2 files (source and destination)");
            return;
        }

        File source = new File(args[0]);
        File dest = new File(args[1]);

        if (!source.exists() || source.isDirectory()) {
            System.out.println("cp: source file not found or is a directory");
            return;
        }

        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(dest)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            System.out.println("file copied from " + source.getName() + " to " + dest.getName());

        } catch (IOException e) {
            System.out.println("cp: error copying file");
        }
    }

    public String chooseCommandAction(String command, String[] args){
        ByteArrayOutputStream bb = new ByteArrayOutputStream();
        PrintStream pp = new PrintStream(bb);
        PrintStream old = System.out;
        System.setOut(pp);
        switch(command){
            case "pwd":
                System.out.println(pwd());
                break;
            case "zip":
                try{ zip(args); } catch (IOException e) { throw new RuntimeException(e); }
                break;
            case "unzip":
                try{ unzip(args); } catch (IOException e) { throw new RuntimeException(e); }
                break;
            case "cat":
                cat(args);
                break;
            case "wc":
                wc(args);
                break;
            case "echo":
                echo(args);
                break;
            case "touch":
                touch(args);
                break;
            case "rm":
                rm(args);
                break;
            case "cp":
                cp(args);
                break;
            default:
                System.out.println(command + " is not a valid command.");
        }
        System.out.flush();
        System.setOut(old);
        return bb.toString();
    }


    public static void main(String[] args){
            Terminal terminal = new Terminal();
            Scanner sc = new Scanner(System.in);

            while(true){
                System.out.print("> ");
                String input = sc.nextLine().trim();
                if(input.equals("exit")) System.exit(0);

                //flag to see the user want to append or not
                boolean append = false;
                // Name of the file to write/append in it
                String outputFile = null;

                //handle append
                if( input.contains(">>") ){
                    String[] parts = input.split(">>", 2);
                    input = parts[0].trim();
                    outputFile = parts[1].trim();
                    append = true;
                }
                //handle overwrite
                else if ( input.contains(">")){
                    String[] parts = input.split(">", 2);
                    input = parts[0].trim();
                    outputFile = parts[1].trim();
                }

                terminal.parser.parse(input);
                String command = terminal.parser.getCommandName();
                String[] commandArgs = terminal.parser.getArgs();

                String output = terminal.chooseCommandAction(command, commandArgs);
                if( outputFile != null ){
                    try (FileWriter fw = new FileWriter(outputFile, append)){
                        fw.write(output);
                    } catch ( IOException e) {
                        System.out.println("Error writing to file: " + outputFile);
                    }
                }else {
                    System.out.print(output);
                }


            }
        }
    }
