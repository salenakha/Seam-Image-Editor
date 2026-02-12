package uk.ac.nulondon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;

public class ImageEditor {
    private final Deque<Command> actionDeque = new ArrayDeque<>();
    private Image image;
    private int counter = 1;
    private SeamState lastSeamState;

    /**
     * Loads an image from the specified path
     *
     * @param filePath the target file path
     * @throws IOException if the file cannot be read from
     */
    public void load(String filePath) throws IOException {
        image = new Image(ImageIO.read(new File(filePath)));
    }

    /**
     * Saves the current image state to the specified file path.
     *
     * @param filePath the target file path
     * @throws IOException if the file cannot be written to
     */
    public void save(String filePath) throws IOException {
        ImageIO.write(image.toBufferedImage(), "png", new File(filePath));
    }

    /**
     * Highlights the vertical seem with the highest green concentration.
     *
     * @throws IOException if image processing throws an error
     */
    public void highlightGreenest() throws IOException {
        processSeam(image::getGreenestSeam, Color.GREEN);
    }

    /**
     * Highlights the vertical seam with the lowest energy.
     *
     * @throws IOException if image processing throws an error
     */
    public void highlightLowestEnergySeam() throws IOException {
        processSeam(image::getLowestEnergySeam, Color.RED);
    }

    private void processSeam(Supplier<List<Pixel>> seamSupplier, Color color) throws IOException {
        executeCommand(() -> {
            List<Pixel> seam = seamSupplier.get();
            image.highlightSeam(seam, color);
            lastSeamState = new SeamState(seam, color);
            return new SeamCommand(seam, color);
        });
    }

    /**
     * Removes the highlighted component in the image.
     *
     * @throws IOException if image processing fails
     */
    public void removeHighlighted() throws IOException {
        if (lastSeamState == null) throw new IllegalStateException();
        executeCommand(() -> new RemoveCommand(lastSeamState.seam(), lastSeamState.color()));
        lastSeamState = null;
    }

    /**
     * Undoes the most recent operation from the deque. Restores previous image state.
     *
     * @throws IOException if image processing fails
     */
    public void undo() throws IOException {
        if (actionDeque.isEmpty()) return;
        actionDeque.pop().undo();
        saveState();
    }

    private void executeCommand(Supplier<Command> commandSupplier) throws IOException {
        try {
            Command cmd = commandSupplier.get();
            cmd.execute();
            actionDeque.push(cmd);
            saveState();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void saveState() throws IOException {
        save("target/tmp" + counter++ + ".png");
    }

    /**
     * Command interface for image operations.
     */
    interface Command {
        /**
         * Execute the command
         */
        void execute();

        /**
         * Undoes a previous command.
         */
        void undo();
    }

    // Making use of records here for their efficiency and concise code
    private record SeamState(List<Pixel> seam, Color color) {}

    private class SeamCommand implements Command {
        protected final List<Pixel> seam;
        protected final Color color;

        SeamCommand(List<Pixel> seam, Color color) {
            this.seam = seam;
            this.color = color;
        }

        public void execute() {
            image.highlightSeam(seam, color);
        }

        public void undo() {
            image.removeSeam(seam);
            image.addSeam(seam);
        }
    }

    private class RemoveCommand extends SeamCommand {
        RemoveCommand(List<Pixel> seam, Color color) {
            super(seam, color);
        }

        @Override
        public void execute() {
            image.removeSeam(seam);
        }

        @Override
        public void undo() {
            image.addSeam(seam);
            image.highlightSeam(seam, color);
        }
    }
}
