import java.io.File;
import java.util.*;
import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Files;
import java.io.InputStream;
import java.util.Collections;

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
import org.tartarus.snowball.ext.PorterStemmer;

// Classe comprenant tous les termes et les documents dans lequel ont les retrouvent
class Termes implements Comparable<Termes> {
    private String mot;
    private int documents;
    private ArrayList<String> arr_documents;
    private ArrayList<String> combinaison;

    public Termes(String mot, int documents) {
        this.mot = mot;
        this.documents = documents;
        this.arr_documents = new ArrayList<String>();
        this.combinaison = new ArrayList<String>();
    }

    public String getMot() {
        return mot;
    }

    public void setMot(String mot) {
        this.mot = mot;
    }

    public int getDocuments() {
        return documents;
    }

    public void setDocuments(int documents) {
        this.documents = documents;
    }

    public ArrayList<String> getListe() {
        return this.arr_documents;
    }

    public void setListe(String fichier) {
        this.arr_documents.add(fichier);
    }

    public boolean hasDocuments() {
        return this.documents != 0;
    }

    /* combiner les different mots */
    public void setCombinaison(String mot) {
        this.combinaison.add(mot);
    }

    public boolean checkInCombinaison(String mot) {
        return false;
    }

    public String getCombinaison() {
        String combin = "";

        if (this.combinaison.size() == 0 || this.combinaison.size() == 1) {
            return combin;
        }

        combin += this.combinaison.get(0);
        for (int i = 1; i < this.combinaison.size(); i++) {
            combin += " AND ";
            combin += this.combinaison.get(i);
        }

        return combin;
    }

    @Override
    public int compareTo(Termes documents) {
        int docs = ((Termes) documents).getDocuments();

        /* Ascendant */
        // return this.documents - docs;

        /* Descendant */
        return docs - this.documents;
    }

    @Override
    public String toString() {
        return "Terme : " + this.getMot() + ", present dans nombre documents : " + this.getDocuments();
    }

}

class Combinaison implements Comparable<Combinaison> {
    private ArrayList<String> combinaison;
    private Integer nombre;

    public Combinaison() {
        this.combinaison = new ArrayList<String>();
        this.nombre = 0;
    }

    public void setNombre(Integer nb) {
        this.nombre = nb;
    }

    public Integer getNombre() {
        return this.nombre;
    }

    /* combiner les different mots */
    public void addMot(String mot) {
        this.combinaison.add(mot);
    }

    public boolean checkInCombinaison(String mot) {
        System.out.println(mot);
        return this.combinaison.contains(mot);
    }

    public ArrayList<String> getListeCombinaison() {
        return this.combinaison;
    }

    public String getCombinaison() {
        String combin = "";

        if (this.combinaison.size() == 0 || this.combinaison.size() == 1) {
            return combin;
        }

        combin += this.combinaison.get(0);
        for (int i = 1; i < this.combinaison.size(); i++) {
            combin += " AND ";
            combin += this.combinaison.get(i);
        }

        return combin;
    }

    @Override
    public int compareTo(Combinaison nombre) {
        int docs = ((Combinaison) nombre).getNombre();

        /* Ascendant */
        // return this.documents - docs;

        /* Descendant */
        return docs - this.nombre;
    }

    @Override
    public String toString() {
        return "La combinaison : " + this.getCombinaison() + " est presente dans nombre documents : "
                + this.getNombre();
    }
}

public class recherchefile {

    public static final String FILES_TO_INDEX_DIRECTORY = "C:\\lucene\\test";
    public static final String INDEX_DIRECTORY = ".\\index\\";
    public static final String FICHIER = ".\\termes.txt";

    public static final String contenu = "contenu";
    public static final String titre = "titre";
    public static ArrayList<Termes> termes_recurences = new ArrayList<>();
    public static ArrayList<String> termes = new ArrayList<String>();

    public static void main(String[] args) throws Exception {
        // lire le fichier
        lireFichier(FICHIER);
        chercherTousLesMots();

        // Trouver le mot plus populaire
        Termes populaire = motPlusPopulaire();
        System.out.println("Le mot le plus populaire : " + populaire.getMot() + " presents dans : "
                + populaire.getDocuments() + " documents.");

        // Trouver la combinaison la plus populaire
        trouverLaCombinaison();
    }

    // initialisation de la liste avec tous les termes provenant du fichier
    public static void lireFichier(String fichier) throws IOException {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(fichier));
            String line = reader.readLine();
            while (line != null) {
                System.out.println("Recherche du terme... " + line);

                // enregistrer le terme dans un array
                termes_recurences.add(new Termes(line, 0));

                // lire la prochaine ligne
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // chercher le mot dans l'index
    public static void chercherUnMot(Termes terme) throws IOException {
        try {
            StandardAnalyzer analyseur = new StandardAnalyzer();
            String[] champs_recherche = { titre, contenu };
            QueryParser parser = new MultiFieldQueryParser(champs_recherche, analyseur);

            // troncature
            String searchString = terme.getMot();
            PorterStemmer stem = new PorterStemmer();
            stem.setCurrent(searchString);
            stem.stem();
            String mot = stem.getCurrent();

            // lancement de la recherche
            Query query = parser.parse(mot);
            IndexSearcher searcher = new IndexSearcher(
                    DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIRECTORY))));
            TopDocs results = searcher.search(query, 4096);
            ScoreDoc[] hits = results.scoreDocs;

            terme.setDocuments(hits.length);

            // Trouver tous les documents pour chaque mot et les enregistrer
            for (int i = 0; i < hits.length; i++) {
                Document doc = searcher.doc(hits[i].doc);
                String titre_doc = doc.get(titre);
                terme.setListe(titre_doc);
            }

        } catch (Exception e) {
            System.err.println("Erreur rencontree: " + e.toString());
        }
    }

    // cherche les mots et enregistrer le nombre de documents trouves
    public static void chercherTousLesMots() throws IOException {
        termes_recurences.forEach(terme -> {
            try {
                chercherUnMot(terme);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // retourner le mot le plus populaire
    public static Termes motPlusPopulaire() {
        Collections.sort(termes_recurences);
        return termes_recurences.get(0);
    }

    // enlever le termes sans documents de la liste
    public static ArrayList<Termes> nettoyerListe(ArrayList<Termes> arr) {

        ArrayList<Termes> termes_arr = new ArrayList<>();

        arr.forEach(terme -> {
            if (terme.hasDocuments() == false) {
                return;
            }
            termes_arr.add(terme);
        });

        // sort
        Collections.sort(termes_arr);
        return termes_arr;
    }

    // chercher le mot dans l'index
    public static Termes chercherDansIndex(Termes terme) throws IOException {
        try {
            StandardAnalyzer analyseur = new StandardAnalyzer();
            String[] champs_recherche = { titre, contenu };
            QueryParser parser = new MultiFieldQueryParser(champs_recherche, analyseur);

            // troncature
            String searchString = terme.getMot();
            PorterStemmer stem = new PorterStemmer();
            stem.setCurrent(searchString);
            stem.stem();
            String mot = stem.getCurrent();

            // lancement de la recherche
            Query query = parser.parse(mot);
            IndexSearcher searcher = new IndexSearcher(
                    DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIRECTORY))));
            TopDocs results = searcher.search(query, 4096);
            ScoreDoc[] hits = results.scoreDocs;

            terme.setDocuments(hits.length);

            // Trouver tous les documents pour chaque mot et les enregistrer
            for (int i = 0; i < hits.length; i++) {
                Document doc = searcher.doc(hits[i].doc);
                String titre_doc = doc.get(titre);
                terme.setListe(titre_doc);
            }

        } catch (Exception e) {
            System.err.println("Erreur rencontree: " + e.toString());
        }

        return terme;
    }

    public static void combinaison() {
        ArrayList<Termes> nouvelle_arr = nettoyerListe(termes_recurences);

        nouvelle_arr.forEach(terme -> {
            try {
                Termes test = chercherDansIndex(terme);
                System.out.println(test);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void trouverLaCombinaison() {
        // nettoyer la liste des termes
        ArrayList<Termes> nouvelle_arr = nettoyerListe(termes_recurences);
        Integer longueur = nouvelle_arr.size();
        Boolean keep = true;

        while (keep == true) {

            ArrayList<Combinaison> current_arr = new ArrayList<>();

            // boucle pour combiner les termes avec un AND
            for (int i = 0; i < longueur - 1; i = i + 1) {
                System.out.println(i);
                Termes term1 = nouvelle_arr.get(i);

                for (int j = i + 1; j < longueur; j = j + 1) {
                    System.out.println(j);
                    Termes term2 = nouvelle_arr.get(j);
                    Combinaison combi = new Combinaison();
                    combi.addMot(term1.getMot());
                    combi.addMot(term2.getMot());

                    // creer un terme a partir de la combinaison
                    Termes text = new Termes(combi.getCombinaison(), 0);
                    try {
                        Termes trouverIndex = chercherDansIndex(text);
                        if (trouverIndex.hasDocuments()) {
                            combi.setNombre(trouverIndex.getDocuments());
                            current_arr.add(combi);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            Collections.sort(current_arr);
            System.out.println(current_arr.get(0));

            // pour lest triplets
            ArrayList<Combinaison> current_triplet = new ArrayList<>();

            // pour chaque combinaison ajouter les termes qui ne font pas partie de la
            // combinaison et calculer le resultat
            Integer nbCombi = current_arr.size();
            for (int z = 0; z < nbCombi; z = z + 1) {
                Combinaison combin = current_arr.get(z);
                System.out.println("Combinaison: " + combin.getCombinaison() + " present dans nombre de documents : "
                        + combin.getNombre());

                // boucle des termes
                for (int i = 0; i < longueur; i = i + 1) {
                    System.out.println(i);
                    Termes term1 = nouvelle_arr.get(i); // verification
                    if (combin.checkInCombinaison(term1.getMot()) == true) {
                        continue;
                    }

                    Combinaison combin_tempo = new Combinaison();

                    // ajouter les mots de l'ancienne combinaison a la nouvelle
                    combin.getListeCombinaison().forEach(mot -> {
                        combin_tempo.addMot(mot);
                    });

                    // ajouter le nouveau terme
                    combin_tempo.addMot(term1.getMot());

                    // faire la recherche
                    Termes text = new Termes(combin_tempo.getCombinaison(), 0);
                    System.out.println("Combinaison: " + combin_tempo.getCombinaison());
                    try {
                        Termes trouverIndex = chercherDansIndex(text);
                        System.out.println("Termes: " + trouverIndex.getMot());
                        if (trouverIndex.hasDocuments()) {
                            combin_tempo.setNombre(trouverIndex.getDocuments());
                            current_triplet.add(combin_tempo);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            current_triplet.forEach(c -> {
                System.out.println(c);
            });

            keep = false;
        }
    }

}