package uk.ac.nulondon;

import java.io.IOException;
import java.util.Scanner;

/*APPLICATION CONTROLLER LAYER*/
public class Main {
    private final ImageEditor editor = new ImageEditor();

    /**
     * Print the UI menu options to the user
     */
    private static void printMenu() {
        System.out.println("Please enter a command");
        System.out.println("g - Remove the greenest seam");
        System.out.println("e - Remove the seam with the lowest energy");
        System.out.println("u - Undo previous edit");
        System.out.println("q - Quit");
    }


    private void undo(Scanner scan) throws IOException {
        System.out.println("Undo. Continue? (Y/N)");
        if ("y".equalsIgnoreCase(scan.next())) {
            editor.undo();
        }
    }

    private void energy(Scanner scan) throws IOException {
        // highlight and export intermediate image
        editor.highlightLowestEnergySeam();
        // ask for confirmation and try to execute
        System.out.println("Remove a lowest energy seam. Continue? (Y/N)");
        if ("y".equalsIgnoreCase(scan.next())) {
            editor.removeHighlighted();
        } else {
            editor.undo();
        }
    }

    private void greenest(Scanner scan) throws IOException {
        // highlight and export intermediate image
        editor.highlightGreenest();
        // ask for confirmation and try to execute
        System.out.println("Remove the greenest seam. Continue? (Y/N)");
        if ("y".equalsIgnoreCase(scan.next())) {
            editor.removeHighlighted();
        } else {
            editor.undo();
        }
    }

    private void run() throws IOException {
        //Scanner is closeable, so we put it into try-with-resources
        try (Scanner scan = new Scanner(System.in)) {

            // src.main.resources.beach.png
            // copy pasta because i have to paste this in so much

            System.out.println("Welcome! Enter file path");
            String filePath = scan.next();
            // import the file
            editor.load(filePath);

            String choice = "";
            while (!"q".equalsIgnoreCase(choice)) {
                // display the menu after every edit
                printMenu();
                // get and handle user input
                choice = scan.next();
                switch (choice.toLowerCase()) {
                    //Extract all the actions into methods besides the trivial ones
                    case "g" -> greenest(scan);
                    case "e" -> energy(scan);
                    case "u" -> undo(scan);
                    case "q" -> System.out.println("Thanks for playing.");
                    default -> System.out.println("That is not a valid option.");
                }
            }
            // After the user exits, export the final image
            editor.save("target/newImg.png");
        }
    }

    public static void main(String[] args) {
        /*Keep main method short. Only create a main class and execute
        the entry point. Also, you may handle specific exceptions here*/
        try {
            new Main().run();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}