

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 4. Sayfadaki Performans Matrisi'ni (Table 1) oluşturmak için
 * 8 farklı senaryoyu test eden Benchmark sınıfı.
 */
public class Benchmark {

    // --- Test Edilecek Dosyalar (4. Sayfa) ---
    private static final String CSV_FILE = "CNN_Articels.csv";
    private static final String SEARCH_WORDS_FILE = "search.txt";

    /**
     * Benchmark testlerini çalıştırmak için bu metodu çağırın.
     * (Bu metot, 'main' metodu içinden çağrılabilir).
     */
    public void runTests() {
        
        // --- 1. Konfigürasyonları Tanımla ---
        double[] loadFactors = {0.5, 0.8}; // α=50% ve α=80%
        
        // Bizim HashMap'imizdeki 'int' sabitleri
        int[] hashTypes = {HashMap.SSF, HashMap.PAF}; 
        int[] collisionTypes = {HashMap.LP, HashMap.DH};

        // Tabloyu güzel basmak için isim etiketleri
        String[] hashNames = {"SSF", "PAF"};
        String[] collisionNames = {"LP", "DH"};

        // --- 2. Arama Kelimelerini Yükle ---
        List<String> searchWords = loadSearchWords(SEARCH_WORDS_FILE);
        if (searchWords.isEmpty()) {
            System.err.println("Benchmark testi iptal edildi (search.txt okunamadı).");
            return;
        }
        System.out.println(searchWords.size() + " adet arama kelimesi yüklendi.");
        System.out.println("Benchmark testleri başlıyor. Bu işlem zaman alabilir...\n");

        // --- 3. Tablo Başlığını Bas ---
        System.out.println("--- Performans Matrisi (Table 1) ---");
        System.out.println("-----------------------------------------------------------------------------");
        // Sütunları formatlı basmak için printf kullanıyoruz
        System.out.printf("%-12s | %-12s | %-12s | %-15s | %-15s | %-15s\n", 
            "Load Factor", "Hash Func.", "Collision", "Collision Count", "Indexing Time(ms)", "Avg. Search(µs)");
        System.out.println("-----------------------------------------------------------------------------");

        // --- 4. 8 Senaryo için Test Döngülerini Başlat (2x2x2) ---
        
        for (double lf : loadFactors) {
            for (int ht_idx = 0; ht_idx < hashTypes.length; ht_idx++) {
                for (int ct_idx = 0; ct_idx < collisionTypes.length; ct_idx++) {
                    
                    int ht = hashTypes[ht_idx];
                    int ct = collisionTypes[ct_idx];
                    
                    // --- 5.a. İndeksleme Testi ---
                    
                    // Her test için sıfırdan bir SearchEngine kur
                    SearchEngine engine = new SearchEngine(lf, ht, ct);
                    
                    // İndekslemeyi çalıştır (Bu metot kendi süresini ölçer)
                    engine.loadArticles(CSV_FILE); 
                    
                    // Metrik 1: İndeksleme Süresi
                    long indexingTime = engine.getIndexingTime();
                    
                    // Metrik 2: Çarpışma Sayısı
                    long collisionCount = engine.getMapCollisionCount();
                    
                    
                    // --- 5.b. Arama Testi (Sayfa 4) ---
                    
                    // 'search.txt' dosyasındaki 1000 kelimeyi ara
                    MyMap<String, MyMap<String, Integer>> indexMap = engine.getIndexMap();
                    
                    // Arama süresini ölçmek için System.nanoTime() (daha hassas)
                    long searchStartTime = System.nanoTime();
                    
                    for (String word : searchWords) {
                        // Sayfa 4: "find a particular key in the hash table"
                        indexMap.get(word); 
                    }
                    
                    long searchEndTime = System.nanoTime();
                    
                    // Metrik 3: Ortalama Arama Süresi
                    long totalSearchTimeNanos = searchEndTime - searchStartTime;
                    // Ortalamayı nanosaniye cinsinden al
                    double avgSearchTimeNanos = (double) totalSearchTimeNanos / searchWords.size();
                    // Mikrosaniye (µs) cinsine çevir (1 µs = 1000 ns)
                    double avgSearchTimeMicros = avgSearchTimeNanos / 1000.0;

                    
                    // --- 6. Sonuçları Tabloya Bas ---
                    System.out.printf("%-12s | %-12s | %-12s | %-15d | %-15d | %-15.3f\n",
                        String.format("%.1f (%.0f%%)", lf, lf * 100), // "0.5 (50%)"
                        hashNames[ht_idx],      // "SSF" / "PAF"
                        collisionNames[ct_idx], // "LP" / "DH"
                        collisionCount,         // Çarpışma sayısı
                        indexingTime,           // İndeksleme (ms)
                        avgSearchTimeMicros     // Ortalama Arama (µs)
                    );
                }
            }
        }
        
        System.out.println("-----------------------------------------------------------------------------");
        System.out.println("Benchmark testleri tamamlandı.");
    }

    /**
     * 'search.txt' dosyasını okur ve kelimeleri bir List'e yükler.
     * @param filename Okunacak dosya adı
     * @return Kelimelerin listesi
     */
    private List<String> loadSearchWords(String filename) {
        List<String> words = new ArrayList<>();
        // Sayfa 5 - Exception Handling
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
