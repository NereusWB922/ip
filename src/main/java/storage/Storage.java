package storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import parsers.Parser;
import parsers.ParsingException;

/**
 * A class for storing and loading objects.
 *
 * @param <T> The type of object being stored and loaded.
 */
public class Storage<T extends Storable<T>> {
    private Parser<T> parser;
    private String filePath;

    /**
     * Constructs a Storage instance with the given parser and file path.
     *
     * @param parser   The parser for converting objects to storable strings.
     * @param filePath The file path where data will be stored or loaded.
     */
    public Storage(Parser<T> parser, String filePath) {
        this.parser = parser;
        this.filePath = filePath;
        
        // Check whether dir exists, create one if not
        File dir = new File(filePath).getParentFile();
        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
                System.err.println("An error occurred when creating directory: " + dir.getAbsolutePath());
            }
        }

        // Check whether file exists, create one if not
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                boolean success = file.createNewFile();
                if (!success) {
                    System.err.println("An error occurred when creating file: " + file.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            System.err.println("An error occurred when creating file: " + e.getMessage());
        }
    }

    public void save(List<T> items) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath))) {
            for (Storable<T> item : items) {
                writer.write(item.toStorableString() + "\n");
            }
        } catch (IOException e) {
            System.err.println("An error occurred when saving data: " + e.getMessage());
        }
    }

    public List<T> load() {
        List<T> items = new ArrayList<>();

        int skipped = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(this.filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    T item = parser.parse(line);
                    if (item != null) {
                        items.add(item);
                    }
                } catch (ParsingException e) {
                    skipped += 1;
                    System.err.println("A parsing error occurred when loading data: " + e.getMessage() + 
                            "\nCorrupted data: " + line + " (Skipped)");
                }
            }
        } catch (IOException e) {
            System.err.println("An error occurred when loading data: " + e.getMessage());
        }

        if (skipped > 0) {
            System.out.println("Skipped " + skipped + " lines with corrupted data.");
        }

        return items;
    }
}
