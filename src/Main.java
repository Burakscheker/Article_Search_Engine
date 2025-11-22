import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String CSV_FILE = "CNN_Articels.csv";

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Benchmark benchmark = new Benchmark();

        System.out.println("Haber Arama Motoru Başlatılıyor...");

        SearchEngine engine = new SearchEngine(
                0.8,
                HashMap.PAF,
                HashMap.DH
        );

        System.out.println("'" + CSV_FILE + "' dosyasındaki makaleler yükleniyor...");
        System.out.println("Bu işlem birkaç dakika sürebilir, lütfen bekleyin...");

        engine.loadArticles(CSV_FILE);

        System.out.println("\nYükleme tamamlandı!");
        System.out.println("Toplam İndeksleme Süresi: " + engine.getIndexingTime() + " ms");
        System.out.println("Toplam Çarpışma (Collision): " + engine.getMapCollisionCount());
        System.out.println("Article Map Kapasitesi: " + engine.articleMap.getCapacity());
        System.out.println("Index Map Kapasitesi: " + engine.indexMap.getCapacity());

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
                    System.out.print("Aramak istediğiniz metni girin: ");
                    String query = scanner.nextLine();

                    List<SearchEngine.ArticleScore> scoreResults = engine.searchByText(query);
                    String[] queryWords = engine.cleanText(query);

                    if (scoreResults.isEmpty()) {
                        System.out.println("'" + query + "' için sonuç bulunamadı.");
                    } else {
                        System.out.println("\n'" + query + "' için en alakalı sonuçlar (Top 5):");
                        System.out.println("\n--- İLGİLİLİK PUANI HESAPLAMASI (Occurrences) ---");

                        final int SUTUN_GENISLIGI = 14;

                        System.out.printf("%-12s | ", "KELİME");
                        for (SearchEngine.ArticleScore score : scoreResults) {
                            System.out.printf("%-" + SUTUN_GENISLIGI + "s | ", "ID(" + score.getArticleId() + ")");
                        }

                        System.out.println("\n-------------------------------------------------------------------------------------------------");

                        for (String word : queryWords) {
                            System.out.printf("%-12s | ", word);

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
                                System.out.printf("%-" + SUTUN_GENISLIGI + "d | ", count);
                            }
                            System.out.println();
                        }

                        System.out.println("-------------------------------------------------------------------------------------------------");

                        System.out.printf("%-12s | ", "TOPLAM PUAN");
                        for (SearchEngine.ArticleScore score : scoreResults) {
                            System.out.printf("%-" + SUTUN_GENISLIGI + "d | ", score.getScore());
                        }

                        System.out.println("\n\nMakale detaylarını görmek için '2' (ID ile Arama) kullanabilirsiniz.");
                    }
                    break;

                case "2":
                    System.out.print("Aramak istediğiniz makale ID'sini girin: ");
                    String id = scanner.nextLine();

                    Article article = engine.searchByID(id);

                    if (article == null) {
                        System.out.println("'" + id + "' ID'li makale bulunamadı.");
                    } else {
                        System.out.println(article.toString());
                    }
                    break;

                case "3":
                    System.out.println("Performans Testi (8 senaryo) başlıyor...");
                    System.out.println("Bu işlem 'search.txt' dosyasını ve CSV'yi 8 kez işleyecektir.");
                    benchmark.runTests();
                    System.out.println("Performans Testi tamamlandı. Ana menüye dönülüyor.");
                    break;

                case "4":
                    System.out.println("Çıkış yapılıyor...");
                    scanner.close();
                    return;

                default:
                    System.out.println("Geçersiz seçim! Lütfen 1, 2, 3 veya 4 girin.");
                    break;
            }
        }
    }
}