package info.revenberg.ppt;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
//import org.hibernate.service.ServiceRegistry;

import info.revenberg.domain.Song;

public class Reader {

    private File folder = null;
    private Session session = null;

    public static String location = "";

    private static SessionFactory sessionFactory = null;
    // private static ServiceRegistry serviceRegistry = null;

    private static SessionFactory configureSessionFactory() throws HibernateException {
        // Configuration configuration = new Configuration();
        // configuration.configure();

        // Properties properties = configuration.getProperties();
        // serviceRegistry = new
        // ServiceRegistryBuilder().applySettings(properties).buildServiceRegistry();
        // sessionFactory = configuration.buildSessionFactory(serviceRegistry);

        Configuration cfg = new Configuration().configure();

        cfg.addAnnotatedClass(info.revenberg.domain.Song.class);
        cfg.addAnnotatedClass(info.revenberg.domain.Vers.class);
        cfg.configure();

        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
                .applySettings(cfg.getProperties());
        sessionFactory = cfg.buildSessionFactory(builder.build());

        return sessionFactory;
    }

    public Reader(String location) throws IOException {
        // Configure the session factory
        configureSessionFactory();
        if (location.substring(location.length() - 1) == "/") {
            Reader.location = location;
        } else {
            Reader.location = location + "/";
        }
        if (folder == null) {
            folder = new File(location);
            search(".*", folder, "");
        }
    }

    public void search(final String pattern, final File folder, final String pre) throws IOException {
        this.session = sessionFactory.openSession();

        for (final File f : folder.listFiles()) {
            if (f.isDirectory()) {
                if (pre != "") {
                    search(pattern, f, pre + "/" + f.getName() + "/");
                } else {
                    search(pattern, f, f.getName() + "/");
                }
            }
            if (f.isFile()) {
                if (f.getName().matches(pattern)) {
                    List<Song> songList = this.session
                            .createQuery("from Song where filename='" + location + pre + f.getName() + "'").list();
                    if (songList != null) {
                        for (Song song1 : songList) {
                            System.out.println(song1);
                        }
                        if (songList.size() == 0) {
                            Song song = new Song(location + pre + f.getName());
                            if (song.getLocation() != null) {
                                song.save(session);
                                System.out.println(song);
                            }
                        }
                    }
                }
            }
        }
        this.session.close();
    }

    // cd D:\git\ews1.0\;mvn clean install package
    public static void main(String[] args) throws Exception {         
        Reader reader = new Reader("d:/pptx_test");
        List<Song> songList1 = reader.session.createQuery("from Song").list();
        if (songList1 != null) {
            for (Song song1 : songList1) {
                System.out.println(song1);
            }
        }
        reader.session.close();
        reader = null;
        System.out.println("EEEEEEEEEEEEEEEEEENNNNNNNNNNNNNNNNNNNNNNNNDDDDDDDDDDDDDDDDDDD");
    }
}