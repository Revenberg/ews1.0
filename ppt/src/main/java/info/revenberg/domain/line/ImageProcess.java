package info.revenberg.domain.line;

import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageProcess{
    
    public static List<Line2D> findLines(BufferedImage image, int rgb) {
    List<Line2D> lines = new ArrayList<Line2D>();
    int w = image.getWidth();
    int h = image.getHeight();
    for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {

            boolean atC = pixelHasColor(image, x, y, rgb);
            boolean atN = pixelHasColor(image, x, y - 1, rgb);
            boolean atS = pixelHasColor(image, x, y + 1, rgb);
            boolean atE = pixelHasColor(image, x + 1, y, rgb);
            boolean atW = pixelHasColor(image, x - 1, y, rgb);

            if (atC) {
                if (atE && !atW) {
                    lines.add(computeLine(image, x, y, 1, 0, rgb));
                }
                if (atS && !atN) {
                    lines.add(computeLine(image, x, y, 0, 1, rgb));
                }
                if (!atS && !atN & !atW && !atE) {
                    lines.add(new Line2D.Double(x, y, x, y));
                }
            }
        }
    }
    return lines;
}

public static Line2D computeLine(BufferedImage image, int x, int y, int dx, int dy, int rgb) {
    int cx = x;
    int cy = y;
    while (pixelHasColor(image, cx, cy, rgb)) {
        cx += dx;
        cy += dy;
    }
    return new Line2D.Double(x, y, cx - dx, cy - dy);
}
private static boolean pixelHasColor(BufferedImage image, int x, int y, int rgb) {
    if (x < 0 || y < 0) {
        return false;
    }
    int w = image.getWidth();
    int h = image.getHeight();
    if (x >= w || y >= h) {
        return false;
    }
    return image.getRGB(x, y) != rgb;
}

public static boolean intersectsLine(Line2D block, Line2D line) {
    Double bh1;
    Double bh2;
    Double lh1;
    Double lh2;
    if (block.getY1() > block.getY2()) {
        bh1 = block.getY2();
        bh2 = block.getY1();
    } else {
        bh1 = block.getY1();
        bh2 = block.getY2();
    }
    if (line.getY1() > line.getY2()) {
        lh1 = line.getY2();
        lh2 = line.getY1();
    } else {
        lh1 = line.getY1();
        lh2 = line.getY2();
    }
    if ((lh1 < bh1) && (lh2 > bh1)) {
        return true;
    }
    if ((lh1 < bh2) && (lh2 > bh1)) {
        return true;
    }
    return false;
}

}
