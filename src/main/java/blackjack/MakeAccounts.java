package blackjack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class MakeAccounts 
{
    public static void main(String[] args) throws Exception
    {
        // read the file src/main/resources/users.txt
        // for each line, create a random 8 digit hex value
        // put the results into src/main/resources/accounts.txt 
        // format: username:password
        // ok now write the java code to do this

        // Read the file src/main/resources/users.txt
        Path inputPath = Paths.get("src/main/resources/users.txt");
        Path outputPath = Paths.get("src/main/resources/accounts.txt");
        BufferedReader reader = Files.newBufferedReader(inputPath);
        BufferedWriter writer = Files.newBufferedWriter(outputPath);

        String line;
        Random random = new Random();

        while ((line = reader.readLine()) != null) {
            // Generate a random 8-digit hex value
            String password = String.format("%x", random.nextInt(0x10000000));
            // Write the username:password to accounts.txt
            writer.write(line + ":" + password);
            writer.newLine();
        }
        writer.write("jspacco:12347");
        writer.newLine();
        writer.flush();

        reader.close();
        writer.close();
    }    
}
