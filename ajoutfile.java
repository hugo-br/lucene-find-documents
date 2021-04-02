import java.io.File;
import java.util.*;
import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Files;
import java.io.InputStream;

// Utilisation des differentes classes de Lucene 
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.analysis.fr.*;
import org.apache.lucene.analysis.en.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.util.*;
import org.apache.lucene.store.*;
import org.apache.lucene.search.*;
import org.apache.lucene.queryparser.classic.*;

public class ajoutfile {

    public static final String FILES_TO_INDEX_DIRECTORY = "C:\\lucene\\test";
    public static final String INDEX_DIRECTORY = ".\\index\\";

    public static final String contenu = "contenu";
    public static final String titre = "titre";
    public static final String chemin = "chemin";

    public static void main(String[] args) throws Exception {
        createIndex();
        search("");
    }

    public static void createIndex() throws CorruptIndexException, LockObtainFailedException, IOException {
        Analyzer analyzer = new StandardAnalyzer();
        boolean recreateIndexIfExists = true;
        IndexWriterConfig conf = new IndexWriterConfig(analyzer);
        Directory dirIndex = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexWriter indexWriter = new IndexWriter(dirIndex, conf);

        File dir = new File(FILES_TO_INDEX_DIRECTORY);
        File[] files = dir.listFiles();

        // debut
        long avant = System.currentTimeMillis();

        for (File file : files) {

            System.out.println("Indexing " + file.getCanonicalPath());
            Document document = new Document();
            System.out.println(file);

            Path chemin_file = Paths.get(file.getCanonicalPath());
            try (InputStream stream = Files.newInputStream(chemin_file)) {

                // indexer le contenu du fichier
                document.add(new TextField(contenu, new String(Files.readAllBytes(chemin_file)), Field.Store.YES));

                // indexer nom du fichier
                document.add(new StringField(titre, file.getName(), Field.Store.YES));

                // indexer nom du fichier
                document.add(new StringField(chemin, file.getCanonicalPath(), Field.Store.YES));

                indexWriter.addDocument(document);
            }

        }

        // apres
        long apres = System.currentTimeMillis();
        System.out.println("Temps d'execution :  " + (apres - avant) / 1000.0 + ".");
        System.out.println("Nombre de documents :  " + files.length + ".");
        System.out.println("Moyenne de " + (apres - avant) / files.length + " milisecondes.");

        indexWriter.close();

    }

    public static void search(String searchString) throws IOException {
        try {
            StandardAnalyzer analyseur = new StandardAnalyzer();
            String[] champs_recherche = { titre, contenu };
            QueryParser parser = new MultiFieldQueryParser(champs_recherche, analyseur);

            BufferedReader in = null;
            // Boucle permettant de faire plusieurs recherches
            while (1 == 1) {

                // entr�e des informations recherches au clavier
                System.out.print("Entrez le texte a chercher - ou tapez enter pour sortir: ");
                in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
                String lettres = in.readLine();

                // Si l'utilisateur a tape enter, on sort
                if (lettres.length() == 0)
                    break;

                // lancement de la recherche
                Query query = parser.parse(lettres);
                // Query query = MultiFieldQueryParser.parse(lettres, champs_recherche,
                // analyseur);

                IndexSearcher searcher = new IndexSearcher(
                        DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIRECTORY))));
                TopDocs results = searcher.search(query, 4096);
                ScoreDoc[] hits = results.scoreDocs;

                // Impression des r�sultats
                System.out.println("Resultat: " + hits.length + " documents contenaient " + lettres);
                for (int i = 0; i < hits.length; i++) {
                    Document doc = searcher.doc(hits[i].doc);
                    int lucene_id = hits[i].doc;
                    // String my_id = doc.get(s_id);
                    String titre_doc = doc.get(titre);
                    // String auteur = doc.get(s_auteur);
                    System.out.println(" - Lucene ID: " + lucene_id + " - Titre: " + titre_doc);
                    // System.out.println("ID: " + my_id + " - Lucene ID: " + lucene_id + " - Titre:
                    // " + titre + " - Auteur: " + auteur );
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur rencontree: " + e.toString());
        }
    }

}
