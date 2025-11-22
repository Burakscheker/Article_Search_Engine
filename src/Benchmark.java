import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Benchmark {

    private static final String CSV_FILE = "CNN_Articels.csv";
    private static final String SEARCH_WORDS_FILE = "search.txt";

    public void runTests() {

        double[] loadFactors = {0.5, 0.8};
        int[] hashTypes = {HashMap.SSF, HashMap.PAF};
        int[] collisionTypes = {HashMap.LP, HashMap.DH};

        String[] hashNames = {"SSF", "PAF"};
        String[] collisionNames = {"LP", "DH"};

        List<String> searchWords = loadSearchWords(SEARCH_WORDS_FILE);
        if (searchWords.isEmpty()) {
            System.err.println("Benchmark testi iptal edildi (search.txt okunamadı).");
            return;
        }
        System.out.println(searchWords.size() + " adet arama kelimesi yüklendi.");
        System.out.println("Benchmark testleri başlıyor. Bu işlem zaman alabilir...\n");

        System.out.println("--- Performans Matrisi (Table 1) ---");
        System.out.println("-----------------------------------------------------------------------------");
        System.out.printf("%-12s | %-12s | %-12s | %-15s | %-15s | %-15s\n",
                "Load Factor", "Hash Func.", "Collision", "Collision Count", "Indexing Time(ms)", "Avg. Search(µs)");
        System.out.println("-----------------------------------------------------------------------------");

        for (double lf : loadFactors) {
            for (int ht_idx = 0; ht_idx < hashTypes.length; ht_idx++) {
                for (int ct_idx = 0; ct_idx < collisionTypes.length; ct_idx++) {

                    int ht = hashTypes[ht_idx];
                    int ct = collisionTypes[ct_idx];

                    SearchEngine engine = new SearchEngine(lf, ht, ct);

                    engine.loadArticles(CSV_FILE);

                    long indexingTime = engine.getIndexingTime();
                    long collisionCount = engine.getMapCollisionCount();

                    MyMap<String, MyMap<String, Integer>> indexMap = engine.getIndexMap();

                    long searchStartTime = System.nanoTime();

                    for (String word : searchWords) {
                        indexMap.get(word);
                    }

                    long searchEndTime = System.nanoTime();

                    long totalSearchTimeNanos = searchEndTime - searchStartTime;
                    double avgSearchTimeNanos = (double) totalSearchTimeNanos / searchWords.size();
                    double avgSearchTimeMicros = avgSearchTimeNanos / 1000.0;

                    System.out.printf("%-12s | %-12s | %-12s | %-15d | %-15d | %-15.3f\n",
                            String.format("%.1f (%.0f%%)", lf, lf * 100),
                            hashNames[ht_idx],
                            collisionNames[ct_idx],
                            collisionCount,
                            indexingTime,
                            avgSearchTimeMicros
                    );
                }
            }
        }

        System.out.println("-----------------------------------------------------------------------------");
        System.out.println("Benchmark testleri tamamlandı.");
    }

    private List<String> loadSearchWords(String filename) {
        List<String> words = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim().toLowerCase();
                if (!line.isEmpty()) {
                    words.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Hata: Arama kelimeleri dosyası bulunamadı: " + filename);
            e.printStackTrace();
        }
        return words;
    }
}