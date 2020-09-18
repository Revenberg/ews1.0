package info.revenberg.domain.line;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import info.revenberg.ppt.FileService;

public class FindLinesInImage {
    private BufferedImage image;
    private List<Line2D> noteLines = new ArrayList<Line2D>();
    private Map<Double, Line2D> blocks = new TreeMap<Double, Line2D>();
    private List<Line2D> lines;
    private Map<Integer, ImageDefinition> imageDefinitions = new TreeMap<Integer, ImageDefinition>();

    public static int minimumLineLength = 5;
    private double scaling;
    private int linesCounter = 0;

    public FindLinesInImage(String filename, String path, String newFilename) throws IOException {
        this( filename, path, newFilename, 1.0);
    }

    public FindLinesInImage(String filename, String path, String newFilename, double scaling) throws IOException {
        this.scaling = scaling;
        File f = new File(filename);
        image = ImageIO.read(new File(filename));

        int lineRgb = Color.BLACK.getRGB();
        Map<Double, Line2D> lines2a = new TreeMap<Double, Line2D>();
        lines = ImageProcess.findLines(image, lineRgb);

        Line2D line;
        Line2D block;

        int border = (int) (image.getWidth() * minimumLineLength / 100);

        for (int i = 0; i < lines.size(); i++) {
            line = lines.get(i);

            if (line.getP1().distance(line.getP2()) > (image.getWidth() - (border * 2))) {
                if (((line.getP1().getX() < border) || (line.getP2().getX() < border))
                        && (line.getP1().getX() > (image.getWidth() - border))
                        || (line.getP2().getX() > (image.getWidth() - border))) {
                    lines2a.put(line.getY2(), line);
                }
            }
        }

        double prev = -1;
        List<Double> temp = new ArrayList<Double>();
        for (Entry<Double, Line2D> entry : lines2a.entrySet()) {
            noteLines.add(entry.getValue());
            if (prev > -1) {
                Double div = Math.ceil((entry.getValue().getY1() - prev) / 5) * 5;
                if (temp.isEmpty()) {
                    temp.add(div);
                } else {
                    if (!(temp.contains(div))) {
                        temp.add(div);
                    }
                }
            }
            prev = entry.getValue().getY1();
        }

        double step = Collections.max(temp);
        temp.remove(step);
        double between = Collections.max(temp);

        Line2D prevLine = noteLines.get(0);
        Line2D line1 = noteLines.get(0);
        Line2D line2;

        for (int i = 1; i < noteLines.size() - 1; i = i + 2) {

            if (noteLines.get(i).getY1() - prevLine.getY1() > (step - between)) {
                // next blok
                line2 = noteLines.get(i);

                block = new Line2D.Double(line1.getP1().getX(), line1.getP1().getY() - 12, line2.getP2().getX() + 5,
                        line2.getP2().getY());
                // block = new Line2D.Double(line1.getP1().getX() + (i * 10),
                // line1.getP1().getY() - 12, 400, line2.getP2().getY());
                blocks.put(Double.valueOf(block.getY1()), block);

                line1 = noteLines.get(i);
            }
            prevLine = noteLines.get(i);
        }
        Line2D h = noteLines.get(noteLines.size() - 1);
        line2 = new Line2D.Double(h.getX1(), h.getY1(), h.getX2(), h.getY2() + step);

        block = new Line2D.Double(line1.getP1().getX(), line1.getP1().getY() - 12, line2.getP2().getX() + 5,
                line2.getP2().getY());
        blocks.put(Double.valueOf(block.getY1()), block);
        saveImages(path, newFilename);
    }

    public int getLinesCounter() {
        return this.linesCounter;
    }
    
    public void increaseLinesCounter() {
        this.linesCounter++;        
    }    

    public int getversLines() {
        return (int) (blocks.size());
    }

    public ImageDefinition getImageDefinition(int i) {
        return this.imageDefinitions.get(i);
    }

    public BufferedImage createimage(BufferedImage image) {
        BufferedImage myImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) myImage.getGraphics();
        AffineTransform oldAt = g.getTransform();
        AffineTransform lineTransform = AffineTransform.getScaleInstance(scaling, scaling);
        g.scale(scaling, scaling);
        g.drawImage(image, 0, 0, null);
        g.setTransform(oldAt);

        // draw blocks
        for (int i = 0; i < blocks.size(); i++) {
            Double key = blocks.keySet().toArray(new Double[blocks.size()])[i];
            Line2D block = blocks.get(key);
            g.setColor(Color.BLUE);
            g.fillRect((int) (block.getX1() * 1), (int) (block.getY1() * 1),
                    (int) ((block.getX2() - block.getX1()) * 1), (int) ((block.getY2() - block.getY1()) * 1));
        }
        // draw notlines
        g.setStroke(new BasicStroke((float) (2 * 1)));
        for (int i = 0; i < noteLines.size(); i++) {
            Line2D line = noteLines.get(i);
            g.setColor(Color.ORANGE);
            g.draw(lineTransform.createTransformedShape(line));
        }

        g.fillRect(0, (int) ((image.getHeight() - 40) * 1), (int) (image.getWidth() * 1), (int) (40 * 1));
        return myImage;
    }

    public BufferedImage line(int splitLine) {

        BufferedImage myImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) myImage.getGraphics();
        AffineTransform oldAt = g.getTransform();
        AffineTransform lineTransform = AffineTransform.getScaleInstance(scaling, scaling);
        g.scale(scaling, scaling);
        g.drawImage(image, 0, 0, null);
        g.setTransform(oldAt);

        Line2D block;

        Double key = blocks.keySet().toArray(new Double[blocks.size()])[splitLine - 1];
        Line2D showBlock = blocks.get(key);
        for (int i = 0; i < blocks.size(); i++) {
            key = blocks.keySet().toArray(new Double[blocks.size()])[i];
            block = blocks.get(key);
            if (i + 1 > splitLine) {
                g.setColor(Color.BLACK);
                g.drawRect((int) (block.getX1() * 1), (int) ((block.getY1()) * 1),
                        (int) ((block.getX2() - block.getX1()) * 1), (int) ((block.getY2() - block.getY2()) * 1));

                for (int j = 0; j < lines.size(); j++) {
                    Line2D line = lines.get(j);

                    if (ImageProcess.intersectsLine(block, line)) {
                        g.setColor(Color.BLACK);
                        g.draw(lineTransform.createTransformedShape(line));

                        if (line.getX1() < myImage.getWidth() / 15) {
                            g.setColor(Color.BLACK);
                            g.clearRect((int) (line.getX1() * 1), (int) ((line.getY1() - 20) * 1),
                                    (int) ((myImage.getWidth() / 15) * 1), (int) (line.getY2() * 1));
                        }
                        g.setColor(Color.BLACK);
                        g.draw(lineTransform.createTransformedShape(line));
                    }
                    if ((line.getX1() > myImage.getWidth() * 0.85) && (line.getY1() < showBlock.getY1())) {
                        g.setColor(Color.BLACK);
                        g.draw(lineTransform.createTransformedShape(line));
                    }
                }
            }
            if (i + 1 == splitLine - 1) {
                block = new Line2D.Double((int) (block.getX1()), (int) (block.getY1()), (int) (block.getX2()),
                        (int) ((block.getY2() - 20)));

                for (int j = 0; j < lines.size(); j++) {
                    Line2D line = lines.get(j);

                    if ((line.getX1() > myImage.getWidth() / 10) && (line.getY1() < showBlock.getY1())
                            && !ImageProcess.intersectsLine(showBlock, line)) {
                        g.setColor(Color.BLACK);
                        g.draw(lineTransform.createTransformedShape(line));
                    }
                }
            }
        }

        int w1 = (int) (showBlock.getX1() * 1);
        int h1 = (int) ((showBlock.getY1() - 25) * 1);

        int w2 = (int) ((showBlock.getX2() - showBlock.getX1()) * 1);
        int h2 = (int) ((showBlock.getY2() - showBlock.getY1() + 15) * 1);

        w2 = myImage.getWidth() - (int) (w1);

        if (h1 + h2 > myImage.getHeight()) {            
            h2 = myImage.getHeight() - h1;
        }
        if (w1 + w2 > myImage.getWidth()) {
            w2 = myImage.getWidth() - w1;            
        }
        return myImage.getSubimage(w1, h1, w2, h2);

        /*
         * BufferedImage temp = myImage.getSubimage((int) (showBlock.getX1() * 1), (int)
         * ((showBlock.getY1() - 25) * 1), (int) ((showBlock.getX2() -
         * showBlock.getX1()) * 1), (int) ((showBlock.getY2() - showBlock.getY1() + 15)
         * * 1)); return temp;
         */
    }

    public void saveImages(String path, String filename) throws IOException {
        FileService.createFolderIfNotExists(path);

        BufferedImage img;
        for (int i = 0; i < this.getversLines(); i++) {
            img = this.line(i + 1);
            if (img != null) {
                String filepath = path + "/" + filename + "_" + Integer.toString(i + 1) + ".jpeg";
                ImageDefinition imageDefinition = new ImageDefinition((Integer) i);

                filepath.replace("\"", "").replace("'", "").replace(" ", "_");

                imageDefinition.setFilename(filepath);
                imageDefinition.setTitle(filename);
                imageDefinition.setImage(img);
                imageDefinitions.put((Integer) i, imageDefinition);

                save(img, filepath);
                increaseLinesCounter();
            }
        }
    }

    public void save(BufferedImage img, String filepath) throws IOException {
        File file = new File(filepath);

        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = iter.next();
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionQuality(1); // an integer between 0 and 1
        FileImageOutputStream output = new FileImageOutputStream(file);
        writer.setOutput(output);

        IIOImage image = new IIOImage(img, null, null);
        writer.write(null, image, iwp);
        writer.dispose();        
    }

    public void createIMG(int from, int to, String path, String filename) throws IOException {
        from = from - 1;
        to = to - 1;
        if (to >= imageDefinitions.size()) {
            to = imageDefinitions.size() - 1;
        }

        int heightTotal = 0;
        for (int j = from; j < to; j++) {
            heightTotal += imageDefinitions.get((Integer) j).getHeight();
        }

        int heightCurr = 0;
        BufferedImage concatImage = new BufferedImage(image.getWidth(), heightTotal, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = concatImage.createGraphics();
        for (int j = from; j < to; j++) {
            ImageDefinition imageDefinition = imageDefinitions.get((Integer) j);
            g2d.drawImage(imageDefinition.getImage(), 0, heightCurr, null);
            heightCurr += imageDefinition.getHeight();
        }
        String filepath = path + "/" + filename + ".jpeg";
        save(concatImage, filepath);
        g2d.dispose();
    }

    private static void createAndShowGui(BufferedImage image) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        JLabel label = new JLabel(new ImageIcon(image));
        panel.add(label);
        f.getContentPane().add(panel);

        f.setSize(image.getWidth(), image.getHeight() + 50);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    public BufferedImage createimage1(BufferedImage image) {
        int lineRgb = Color.BLACK.getRGB();

        BufferedImage myImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) myImage.getGraphics();
        AffineTransform oldAt = g.getTransform();
        AffineTransform lineTransform = AffineTransform.getScaleInstance(scaling, scaling);
        g.scale(scaling, scaling);
        g.drawImage(image, 0, 0, null);
        g.setTransform(oldAt);

        lines = ImageProcess.findLines(image, lineRgb);

        g.setStroke(new BasicStroke((float) (2 * 1)));
        for (int i = 0; i < noteLines.size(); i++) {
            Line2D line = noteLines.get(i);
            g.setColor(Color.RED);
            g.draw(lineTransform.createTransformedShape(line));
        }
        return myImage;
    }

    public static void main(String[] args) throws IOException {
        FindLinesInImage result1 = new FindLinesInImage("D:/tmp/_test/ppt/media/image5.png",
                "D:/tmp/test/", "test4", 0.5);
        FindLinesInImage.createAndShowGui(result1.createimage1(result1.image));

    }
}