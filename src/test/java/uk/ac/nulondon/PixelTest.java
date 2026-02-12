package uk.ac.nulondon;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;

class PixelTest {
    private Pixel pixel = new Pixel(new Color(10, 14, 9));

    @Test
    void testBrightness() {
        Assertions.assertThat(pixel.brightness()).isEqualTo(11.0);
    }
    
}