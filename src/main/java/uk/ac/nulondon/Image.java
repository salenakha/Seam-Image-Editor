package uk.ac.nulondon;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Image {
    private final List<Pixel> rows;

    private int width;
    private int height;

    public Image(BufferedImage img) {
        width = img.getWidth();
        height = img.getHeight();
        rows = new ArrayList<>();
        Pixel current = null;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Pixel pixel = new Pixel(img.getRGB(col, row));
                if (col == 0) {
                    rows.add(pixel);
                } else {
                    current.right = pixel;
                    pixel.left = current;
                }
                current = pixel;
            }
        }
    }

    public BufferedImage toBufferedImage() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < height; row++) {
            Pixel pixel = rows.get(row);
            int col = 0;
            while (pixel != null) {
                image.setRGB(col++, row, pixel.color.getRGB());
                pixel = pixel.right;
            }
        }
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Calculates the given energy for a pixel.
     *
     * @param above is the Pixel located above current
     * @param current is the desired Pixel whose energy needs to be calculated
     * @param below is the Pixel located below current
     * @return the energy of the current pixel
     */
    double energy(Pixel above, Pixel current, Pixel below) {
        // Early termination if we are computing the energy for a pixel along the edge
        if (above == null || current.left == null || current.right == null || below == null) return current.brightness();

        // Cache reused method calls
        double brA = above.left.brightness(),
                brC = above.right.brightness(),
                brG = below.left.brightness(),
                brI = below.right.brightness();

        // Compute the energy for the given pixel
        double horizEnergy = (brA + 2 * current.left.brightness() + brG) - (brC + 2 * current.right.brightness() + brI);
        double vertEnergy = (brA + 2 * above.brightness() + brC) - (brG + 2 * below.brightness() + brI);
        return Math.sqrt(horizEnergy * horizEnergy + vertEnergy * vertEnergy);
    }

    /**
     * Calculates the energy of all pixels located in the image.
     */
    public void calculateEnergy() {
        for (int row = 0; row < height; row++) {
            // Get the Pixel objects located above and below the current Pixel
            Pixel above = (row > 0) ? rows.get(row - 1) : null;
            Pixel current = rows.get(row);
            Pixel below = (row < height - 1) ? rows.get(row + 1) : null;

            // Calculate the energy for each Pixel
            for (int col = 0; col < width; col++) {
                current.energy = energy(above, current, below);

                // Shifts each Pixel object forward in the data structure
                if (above != null) above = above.right;
                current = current.right;
                if (below != null) below = below.right;
            }
        }
    }

    /**
     * Highlights the given seam with a predetermined color
     *
     * @param seam is the list containing all Pixels in the seam
     * @param color is the Color to fill all Pixels in the seam
     * @return the original version prior to seam highlighting
     */
    public List<Pixel> highlightSeam(List<Pixel> seam, Color color) {
        List<Pixel> original = seam.stream().map(pixel -> new Pixel(pixel.color.getRGB())).collect(Collectors.toList());

        for (Pixel pixel : seam) {
            if (pixel == null) continue;
            pixel.color = color;
        }

        return original;
    }

    /**
     * Removes a specified seam.
     *
     * @param seam is the list containing all Pixels within the seam
     */
    public void removeSeam(List<Pixel> seam) {
        // Edge case, early termination if we do not have a seam or the seam does not match image resolution
        if (seam == null || seam.size() != height) return;

        // Loops through every Pixel object within the seam
        for (int row = 0; row < height; row++) {
            Pixel p = seam.get(row);
            if (p == null) continue;

            if (p.left != null) p.left.right = p.right;
            else {
                int rowIndex = rows.indexOf(p);
                if (rowIndex != -1) {
                    rows.set(rowIndex, p.right);
                }
            }


            if (p.right != null) p.right.left = p.left;
        }

        // Decrements the image width
        width--;
    }

    /**
     * Adds a specified seam.
     *
     * @param seam is the list containing all Pixels in the seam
     */
    public void addSeam(List<Pixel> seam) {
        // Edge case, early termination if we do not have a seam or the seam does not match image resolution
        if (seam == null || seam.size() != height) return;

        // Uses streams for shorter code (albeit, I believe that streams might be less efficient in the current Java version? This varies.)
        IntStream.range(0, height).forEach(row -> Optional.ofNullable(seam.get(row))
                .ifPresent(p -> {
                    Pixel newPixel = new Pixel(p.color);
                    Pixel rowHead = rows.get(row);
                    if (rowHead == null) rows.set(row, newPixel);
                    else if (p.left == null) {
                        newPixel.right = rowHead;
                        rowHead.left = newPixel;
                        rowHead.right = null;
                        rows.set(row, newPixel);
                    } else {
                        Pixel leftPixel = p.left;
                        Pixel rightPixel = leftPixel.right;

                        newPixel.left = leftPixel;
                        newPixel.right = rightPixel;
                        leftPixel.right = newPixel;

                        if (rightPixel != null) rightPixel.left = newPixel;
                    }
                }));

        // Increments the image width
        width++;
    }

    /**
     * Uses a dynamic programming table to determine the maximum vertical seam value based on the argument provided
     *
     * @param valueGetter a function that takes a Pixel and computes a value representing Pixel influence
     * @return a list of Pixels representing the highest energy seam
     */
    private List<Pixel> getSeamMaximizing(Function<Pixel, Double> valueGetter) {
        // Initial table initialization
        record Pair<A, B>(Double value, List<Pixel> seam) {}
        Pair<Double, List<Pixel>>[] table = new Pair[width];

        // First row construction
        Pixel pixel = rows.getFirst();
        for (int col = 0; pixel != null; col++, pixel = pixel.right) {
            table[col] = new Pair<>(valueGetter.apply(pixel), new ArrayList<>(List.of(pixel)));
        }

        // Finds the best seams at each column within the image
        for (int row = 1; row < height; row++) {
            pixel = rows.get(row);
            Pair<Double, List<Pixel>>[] prevTable = table.clone();

            for (int col = 0; pixel != null; col++, pixel = pixel.right) {
                int bestCol = col;

                if (col > 0 && prevTable[col - 1].value() > prevTable[bestCol].value()) bestCol = col - 1;
                if (col < width - 1 && prevTable[col + 1].value() > prevTable[bestCol].value()) bestCol = col + 1;

                List<Pixel> newSeam = new ArrayList<>(prevTable[bestCol].seam());
                newSeam.add(pixel);
                table[col] = new Pair<>(prevTable[bestCol].value() + valueGetter.apply(pixel), newSeam);
            }
        }

        // Finds the maximum index
        int maxIndex = 0;
        for (int i = 1; i < width; i++) {
            if (table[i].value() > table[maxIndex].value()) maxIndex = i;
        }

        // Returns the highest energy seam
        return table[maxIndex].seam();
    }

    public List<Pixel> getGreenestSeam() {
        return getSeamMaximizing(Pixel::getGreen);
        /*Or, since we haven't lectured on lambda syntax in Java, this can be
        return getSeamMaximizing(new Function<Pixel, Double>() {
            @Override
            public Double apply(Pixel pixel) {
                return pixel.getGreen();
            }
        });*/

    }

    public List<Pixel> getLowestEnergySeam() {
        calculateEnergy();
        /*
        Maximizing negation of energy is the same as minimizing the energy.
         */
        return getSeamMaximizing(pixel -> -pixel.energy);

        /*Or, since we haven't lectured on lambda syntax in Java, this can be
        return getSeamMaximizing(new Function<Pixel, Double>() {
            @Override
            public Double apply(Pixel pixel) {
                return -pixel.energy;
            }
        });
        */
    }
}
