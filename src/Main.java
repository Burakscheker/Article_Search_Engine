import java.util.List;
import java.util.Scanner;

/**
 * Projenin ana giriş noktası (Entry Point).
 * Kullanıcı arayüzü menüsünü yönetir, SearchEngine'i başlatır
 * ve kullanıcı girdilerine göre ilgili fonksiyonları çağırır.
 */
public class Main {

    // Ana veri dosyalarının adları
    private static final String CSV_FILE = "CNN_Articels.csv";

    public static void main(String[] args) {
        
        // --- 1. Başlatma (Initialization) ---
        
        // Kullanıcı girdisi için Scanner
        Scanner scanner = new Scanner(System.in);
        
        // Benchmark sınıfından bir nesne oluştur
        Benchmark benchmark = new Benchmark();

        System.out.println("Haber Arama Motoru Başlatılıyor...");
        
        // Arama motorunu en iyi/stabil konfigürasyonla (PAF + DH) başlat
        // 5. Sayfa: "OOP principles must be applied"
        SearchEngine engine = new SearchEngine(
            0.8,       // maxLoadFactor (α=80%)
            HashMap.PAF, // hashType (Polynomial)
            HashMap.DH   // collisionType (Double Hashing)
        );

        // --- 2. Veri Yükleme (ETL) ---
        System.out.println("'" + CSV_FILE + "' dosyasındaki makaleler yükleniyor...");
        System.out.println("Bu işlem birkaç dakika sürebilir, lütfen bekleyin...");
        
        // Ana CSV dosyasını yükle
        engine.loadArticles(CSV_FILE);
        
        System.out.println("\nYükleme tamamlandı!");
        System.out.println("Toplam İndeksleme Süresi: " + engine.getIndexingTime() + " ms");
        System.out.println("Toplam Çarpışma (Collision): " + engine.getMapCollisionCount());
        System.out.println("Article Map Kapasitesi: " + engine.articleMap.getCapacity());
        System.out.println("Index Map Kapasitesi: " + engine.indexMap.getCapacity());

        // --- 3. Ana Menü Döngüsü ---
        while (true) {
            System.out.println("\n--- ANA MENÜ ---");
            System.out.println("1. Metin ile Arama (Search by text)");
            System.out.println("2. ID ile Arama (Search by ID)");
            System.out.println("3. Performans Testini Çalıştır (Run Benchmark)");
            System.out.println("4. Çıkış (Exit)");
            System.out.print("Seçiminiz (1-4): ");

            String choice = scanner.nextLine();

            switch (choice) {
            case "1":
                // --- Metin ile Arama (Sayfa 2) ---
                System.out.print("Aramak istediğiniz metni girin: ");
                String query = scanner.nextLine();
                
                // 1. Arama motorundan Top 5 skor listesini al
                List<SearchEngine.ArticleScore> scoreResults = engine.searchByText(query);
                
                // 2. Puanlamada kullanılan KELİMELERİ de al
                String[] queryWords = engine.cleanText(query);
                
                if (scoreResults.isEmpty()) {
                    System.out.println("'" + query + "' için sonuç bulunamadı.");
                } else {
                    System.out.println("\n'" + query + "' için en alakalı sonuçlar (Top 5):");
                    System.out.println("\n--- İLGİLİLİK PUANI HESAPLAMASI (Occurrences) ---");

                    // DÜZELTME: Sütun genişliğini sabitliyoruz.
                    // ID(10 harf) = ID(1234567890) -> 14 karakter.
                    // Bu yüzden tüm sütunlar için %-14s veya %-14d kullanacağız.
                    final int SUTUN_GENISLIGI = 14;

                    // 3. Tablo Başlığını Oluştur (Makale ID'leri ile)
                    System.out.printf("%-12s | ", "KELİME"); // İlk sütun sabit (12 karakter)
                    for (SearchEngine.ArticleScore score : scoreResults) {
                        // Başlığı (örn: "ID(C0209661RD)") 14 karaktere sığdır/genişlet
                        System.out.printf("%-" + SUTUN_GENISLIGI + "s | ", "ID(" + score.getArticleId() + ")");
                    }
                    
                    // Uzun ayraç çizgi
                    System.out.println("\n-------------------------------------------------------------------------------------------------");

                    // 4. Her kelime için frekans satırını bas
                    for (String word : queryWords) {
                        System.out.printf("%-12s | ", word); // Satır başlığı (12 karakter)
                        
                        for (SearchEngine.ArticleScore score : scoreResults) {
                            String articleId = score.getArticleId();
                            int count = 0;
                            
                            MyMap<String, Integer> wordMap = engine.indexMap.get(word); 
                            if (wordMap != null) {
                                Integer freq = wordMap.get(articleId); 
                                if (freq != null) {
                                    count = freq;
                                }
                            }
                            // Sayıyı (örn: 37) 14 karaktere sığdır/genişlet
                            System.out.printf("%-" + SUTUN_GENISLIGI + "d | ", count);
                        }
                        System.out.println(); // Satırı bitir
                    }

                    // 5. Ayraç çizgi bas
                    System.out.println("-------------------------------------------------------------------------------------------------");

                    // 6. Toplam Puan Satırını Bas
                    System.out.printf("%-12s | ", "TOPLAM PUAN");
                    for (SearchEngine.ArticleScore score : scoreResults) {
                        // Toplam puanı (örn: 38) 14 karaktere sığdır/genişlet
                        System.out.printf("%-" + SUTUN_GENISLIGI + "d | ", score.getScore());
                    }
                    
                    // 7. Son bilgilendirme
                    System.out.println("\n\nMakale detaylarını görmek için '2' (ID ile Arama) kullanabilirsiniz.");
                }
                break; // case "1" bitti

                case "2":
                    // --- ID ile Arama (Sayfa 2) ---
                    System.out.print("Aramak istediğiniz makale ID'sini girin: ");
                    String id = scanner.nextLine();
                    
                    Article article = engine.searchByID(id);
                    
                    if (article == null) {
                        System.out.println("'" + id + "' ID'li makale bulunamadı.");
                    } else {
                        System.out.println(article.toString()); // toString() metodumuzu kullanır
                    }
                    break;

                case "3":
                    // --- Performans Testi (Sayfa 4) ---
                    System.out.println("Performans Testi (8 senaryo) başlıyor...");
                    System.out.println("Bu işlem 'search.txt' dosyasını ve CSV'yi 8 kez işleyecektir.");
                    benchmark.runTests(); // Benchmark sınıfını çağır
                    System.out.println("Performans Testi tamamlandı. Ana menüye dönülüyor.");
                    break;

                case "4":
                    // --- Çıkış ---
                    System.out.println("Çıkış yapılıyor...");
                    scanner.close(); // Kaynakları serbest bırak
                    return; // Programı sonlandır

                default:
                    // --- Hatalı Girdi ---
                    // 5. Sayfa: "Exception handling must be used when it is needed"
                    // Bu, basit bir 'default' case ile hatalı kullanıcı girdisini yakalar.
                    System.out.println("Geçersiz seçim! Lütfen 1, 2, 3 veya 4 girin.");
                    break;
            }
        }
    }
}
