package info.revenberg.domain;

import java.awt.image.BufferedImage;
import java.io.IOException;

import info.revenberg.domain.line.ConcatLines;
import info.revenberg.domain.line.FindLinesInImage;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "vers")

public class Vers extends AuditModel {
    private static final long serialVersionUID = 3938614111689605974L;

    @Id
    // @GeneratedValue(strategy = GenerationType.AUTO)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vers_sequence")
    @GenericGenerator(name = "vers_sequence", strategy = "native")
    private Long id;

    private int number;

    private String FileName;
    private int lines;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "fk_song", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Song song;

    private String pictures = "";

    public Vers() {
    }

    public Vers(Long id, int number, String FileName, int lines, Song song) {
        this.id = id;
        this.number = number;
        this.FileName = FileName;
        this.lines = lines;
        this.song = song;
    }

    public Vers(Song song, String tempFile) throws IOException {
        setSong(song);
        String[] s1 = tempFile.split("/");
        String versName = s1[s1.length - 1];
        String ext = FileService.getExtension(tempFile);
        versName = versName.replace("." + ext, "").replace("image", "");

        this.setNumber(Integer.parseInt(versName) - 1);
        this.lines = 0;
        this.FileName = "vers " + Integer.toString(this.getNumber()) + "." + FileService.getExtension(tempFile);
        FileService.copyFile(tempFile, Song.unzipLocation + "/" + song.getLocation() + "/", this.getFileName());

        FindLinesInImage f = new FindLinesInImage(
                Song.unzipLocation + "/" + song.getLocation() + "/" + this.getFileName(),
                Song.unzipLocation + "/" + song.getLocation() + "/" + "/verzen", Integer.toString(this.getNumber()));

        this.setLines(f.getLinesCounter());

        // f = null;

        ConcatLines cl;
        String fn;
        switch (this.getLines()) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                // no split needed
                fn = song.getLocation() + "/" + "N_" + Integer.toString(this.getNumber()) + ".jpg";
                pictures += fn;
                cl = new ConcatLines(Song.unzipLocation + fn);

                for (int i = 0; i < this.getLines(); i++) {
                    BufferedImage image = f.getImageDefinition(i).getImage();
                    cl.addImage(image);
                }
                cl.concat();
                break;
            case 6:
            case 7:
                // split after 3
                fn = song.getLocation() + "/" + "N_" + Integer.toString(this.getNumber()) + "_1.jpg";
                pictures += fn + ";";
                cl = new ConcatLines(Song.unzipLocation + fn);
                for (int i = 0; i < 3; i++) {
                    BufferedImage image = f.getImageDefinition(i).getImage();
                    cl.addImage(image);
                }
                cl.concat();

                // split after 3
                fn = song.getLocation() + "/" + "N_" + Integer.toString(this.getNumber()) + "_2.jpg";
                pictures += fn;
                cl = new ConcatLines(Song.unzipLocation + fn);
                for (int i = 3; i < this.getLines(); i++) {
                    BufferedImage image = f.getImageDefinition(i).getImage();
                    cl.addImage(image);
                }
                cl.concat();
                break;
            case 8:
                // split after 4
                fn = song.getLocation() + "/" + "N_" + Integer.toString(this.getNumber()) + "_1.jpg";
                pictures += fn + ";";
                cl = new ConcatLines(Song.unzipLocation + fn);
                for (int i = 0; i < 4; i++) {
                    BufferedImage image = f.getImageDefinition(i).getImage();
                    cl.addImage(image);
                }
                cl.concat();

                // split after 4
                fn = song.getLocation() + "/" + "N_" + Integer.toString(this.getNumber()) + "_2.jpg";
                pictures += fn;
                cl = new ConcatLines(Song.unzipLocation + fn);
                for (int i = 4; i < this.getLines(); i++) {
                    BufferedImage image = f.getImageDefinition(i).getImage();
                    cl.addImage(image);
                }
                cl.concat();
                break;
            case 9:
            case 10:
                // split after 3 and after 6
                fn = song.getLocation() + "/" + "N_" + Integer.toString(this.getNumber()) + "_1.jpg";
                pictures += fn + ";";
                cl = new ConcatLines(Song.unzipLocation + fn);

                for (int i = 0; i < 3; i++) {
                    BufferedImage image = f.getImageDefinition(i).getImage();
                    cl.addImage(image);
                }
                cl.concat();

                // split after 3
                fn = song.getLocation() + "/" + "N_" + Integer.toString(this.getNumber()) + "_2.jpg";
                pictures += fn + ";";
                cl = new ConcatLines(Song.unzipLocation + fn);
                for (int i = 3; i < this.getLines(); i++) {
                    BufferedImage image = f.getImageDefinition(i).getImage();
                    cl.addImage(image);
                }
                cl.concat();

                // split after 6
                fn = song.getLocation() + "/" + "N_" + Integer.toString(this.getNumber()) + "_3.jpg";
                pictures += fn;
                cl = new ConcatLines(Song.unzipLocation + fn);
                for (int i = 6; i < this.getLines(); i++) {
                    BufferedImage image = f.getImageDefinition(i).getImage();
                    cl.addImage(image);
                }
                cl.concat();
                break;
            default:
                break;
        }

    }

    public Vers(long id, int number, String FileName, int lines, Song song) {
        this.id = id;
        this.number = number;
        this.FileName = FileName;
        this.lines = lines;
        setSong(song);
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public Song getSong() {
        return this.song;
    }

    public int getNumber() {
        return this.number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getLines() {
        return this.lines;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    @Override
    public String toString() {
        return "{" + "FileName='" + this.getFileName() + "', id=" + this.getId() + ", lines=" + this.getLines() + " }";
    }

    private String getFileName() {
        return this.FileName;
    }

    public void save(Session session) {
        session.save(this);
    }
}
