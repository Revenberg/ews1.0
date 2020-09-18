package info.revenberg.domain;

import java.io.File;

import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.GenericGenerator;

import info.revenberg.ppt.Reader;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "song")
public class Song extends AuditModel {
    private static final long serialVersionUID = -3265892245293180670L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "song_sequence")
    @GenericGenerator(name = "song_sequence", strategy = "native")
    private Long id;

    private String fileName;
    private String bundleName;
    private String songName;
    private String location;
    public static String unzipLocation = "d:/tmp";

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Vers> verzen = new HashSet<Vers>();

    public Song() {
    }

    public Song(Long id, String fileName, String bundleName, String songName, String location, Set<Vers> verzen) {
        this.id = id;
        this.fileName = fileName;
        this.bundleName = bundleName;
        this.songName = songName;
        this.location = location;
        this.verzen = verzen;
    }

    public Song(String fileName) throws IOException {
        if (fileName.toLowerCase().contains(".pptx")) {
            processSong(fileName);
        }
    }

    public void setBundleAndSongnameFromFilename(String fileName) throws UnsupportedEncodingException {
        this.setFileName(fileName);
        String[] s = fileName.replace(Reader.location, "").split("/");
        String bundleName = "";
        for (int i = 0; i < s.length - 1; i++)
            if (bundleName == "") {
                bundleName = s[i];
            } else {
                bundleName += " - " + s[i];
            }
        String songName = s[s.length - 1].replace(".pptx", "");

        this.setBundleName(bundleName);
        this.setSongName(songName);
    }

    public void processSong(String fileName) throws IOException {
        setBundleAndSongnameFromFilename(fileName);
        String fileNameDest = URLEncoder.encode(this.getBundleName() + "-" + this.getSongName(), "UTF-8");
        this.setLocation("/" + fileNameDest);

        List<String> t1 = FileService.unzip(fileName, unzipLocation + "/_" + fileNameDest);

        for (String temp : t1) {
            if (temp.contains(".png")) {
                Vers vers = new Vers(this, temp);
                verzen.add(vers);
            }
        }
        FileService.deleteFolderIfExists(new File(unzipLocation + "/_" + fileNameDest));
    }  

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private void setFileName(String fileName) {
        this.fileName = fileName.toLowerCase();
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public void setBundleName(String bundleName) throws UnsupportedEncodingException {
        this.bundleName = Normalizer.normalize(bundleName.trim(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
                .replace("  ", " ").replace("'", "").trim();
    }

    public String getSongName() {
        return this.songName;
    }

    public void setSongName(String songName) throws UnsupportedEncodingException {
        this.songName = Normalizer.normalize(songName.trim(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
                .replace("  ", " ").replace("'", "").trim();
    }

    @Override
    public String toString() {
        String str = "{" + " id=" + Long.toString(this.getId()) + ", location='" + this.getLocation() + "'"
                + ", bundleName='" + getBundleName() + "'" + ", songName='" + getSongName() + "'";
        str += " [ ";
        str += " ] ";
        str += " }";
        return str;
    }

    public void save(Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(this);

            if (verzen != null) {
                for (Vers vers: verzen) {
                    vers.save(session);
                    System.out.println(vers);
                }
            }

            // Committing the change in the database.
            session.flush();
            tx.commit();

        } catch (Exception ex) {
            ex.printStackTrace();

            // Rolling back the changes to make the data consistent in case of any failure
            // in between multiple database write operations.
            tx.rollback();

        }
    }

}
