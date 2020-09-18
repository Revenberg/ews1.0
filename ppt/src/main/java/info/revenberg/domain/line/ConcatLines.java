package info.revenberg.domain.line;

import java.util.LinkedHashMap;

import javax.imageio.ImageIO;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ConcatLines {

    private LinkedHashMap<Integer, BufferedImage> images = new LinkedHashMap<Integer, BufferedImage>();
    private String dest;

    public ConcatLines(String dest) {
        this.dest = dest;
    }

    public void addImage(BufferedImage image) throws IOException {
        images.put(images.size(), image);
    }       

    public void concat() throws IOException {
        int heightTotal = 0;
        int widthMax = 0;
        for(int j = 0; j < images.size(); j++) {
            heightTotal += images.get(j).getHeight();
            if (widthMax < images.get(j).getWidth()) {
                widthMax = images.get(j).getWidth();
            }
        }

        int heightCurr = 0;
        BufferedImage concatImage = new BufferedImage(widthMax, heightTotal, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = concatImage.createGraphics();
        for(int j = 0; j < images.size(); j++) {
            g2d.drawImage(images.get(j), 0, heightCurr, null);
            heightCurr += images.get(j).getHeight();
        }
        g2d.dispose();
        ImageIO.write(concatImage, "png", new File(this.dest));
    }
}
