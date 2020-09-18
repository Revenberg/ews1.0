package info.revenberg.domain.line;

import java.awt.image.BufferedImage;
import java.util.List;

import java.awt.Color;
import java.awt.geom.Line2D;

public class ImageDefinition {
    private Integer index;
    private String filename;
    private BufferedImage image;
    private String title;
    private double minY;
    private double maxY;
    private double minX;
    private double maxX;

    public ImageDefinition(Integer index) {
        this.index = index;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getIndex() {
        return this.index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public int getWidth() {
        return this.image.getWidth();
    }

    public int getHeight() {
        return this.image.getHeight();
    }

    public void setImage(BufferedImage image) {
        int rgb = Color.BLACK.getRGB();
        List<Line2D> lines = ImageProcess.findLines(image, rgb);

        Line2D line;
        minY = image.getHeight();
        maxY = 0;
        minX = image.getWidth();
        maxX = 0;
        for (int i = 0; i < lines.size(); i++) {
            line = lines.get(i);
            if (line.getP1().getY() < minY) {
                minY = line.getP1().getY();
            }
            if (line.getP2().getY() < minY) {
                minY = line.getP2().getY();
            }
            if (line.getP1().getY() > maxY) {
                maxY = line.getP1().getY();
            }
            if (line.getP2().getY() > maxY) {
                maxY = line.getP2().getY();
            }

            if (line.getP1().getX() < minX) {
                minX = line.getP1().getX();
            }
            if (line.getP2().getX() < minX) {
                minX = line.getP2().getX();
            }
            if ((line.getP1().getX() > maxX) && (line.getP1().getX() < image.getWidth() * .95)) {
                maxX = line.getP1().getX();
            }
            if ((line.getP2().getX() > maxX) && (line.getP2().getX() < image.getWidth() * .95)) {
                maxX = line.getP2().getX();
            }

        }

        this.image = image;
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public double getminY() {
        return this.minY;
    }

    public double getMaxY() {
        return this.maxY;
    }

    public double getminX() {
        return this.minX;
    }

    public double getMaxX() {
        return this.maxX;
    }        
}
