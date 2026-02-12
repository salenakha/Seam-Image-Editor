package uk.ac.nulondon;

import java.awt.*;

public class Pixel {
    Pixel left;
    Pixel right;

    double energy;

    Color color;
    private double brightness = -1;

    public Pixel(int rgb) {
        this.color = new Color(rgb);
    }

    public Pixel(Color color) {
        this.color = color;
    }

    /**
     * Computes the brightness for a given pixel.
     * Additionally, caches the brightness value to reduce repeated computations (as pixel color does not necessarily change during runtime).
     *
     * @return a double, representing Pixel brightness
     */
    public double brightness() {
        if (brightness == -1) brightness = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
        return brightness;
    }

    public double getGreen() {
        return color.getGreen();
    }
}
