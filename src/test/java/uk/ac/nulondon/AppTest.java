package uk.ac.nulondon;

import org.approvaltests.awt.AwtApprovals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class AppTest {
    Image image;

    @BeforeEach
    void setup() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File("src/main/resources/beach.png"));
        image = new Image(bufferedImage);
    }

    @Test
    void toBufferedImage() {
        AwtApprovals.verify(image.toBufferedImage());
    }

    @Test
    void highlightGreenest() {
        image.highlightSeam(image.getGreenestSeam(), Color.BLUE);
        AwtApprovals.verify(image.toBufferedImage());
    }

    @Test
    void highlightLowestEnergy() {
        image.highlightSeam(image.getLowestEnergySeam(), Color.RED);
        AwtApprovals.verify(image.toBufferedImage());
    }

    @Test
    void removeSeam(){
        image.removeSeam(image.getGreenestSeam());
        AwtApprovals.verify(image.toBufferedImage());
    }

    @Test
    void insertSeam(){
        List<Pixel> seam = image.getGreenestSeam();
        image.removeSeam(seam);
        image.addSeam(seam);
        AwtApprovals.verify(image.toBufferedImage());
    }

}
