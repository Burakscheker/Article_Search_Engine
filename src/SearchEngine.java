

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.HashSet; // Stop-words tutmak için (izin verilen yardımcı araç)
import java.util.ArrayList; // Sonuç listesi döndürmek için
import java.util.List; // Sonuç listesi döndürmek için
import java.util.Comparator; // Puanlama sıralaması için
import java.util.Collections; // Puanlama sıralaması için

/**
 * Arama motorunun tüm ana mantığını yöneten servis sınıfı.
 * %100 'Bizim' HashMap sınıfımızı kullanır.
 */
public class SearchEngine {

    // --- 1. Veri Yapıları (Tamamen Kendi HashMap'imiz) ---
    public MyMap<String, Article> articleMap;
    // Dış map de, iç map de BİZİM sınıfımız:
    public MyMap<String, MyMap<String, Integer>> indexMap;

    // --- 2. Yardımcı Veri Kümeleri ---
    private HashSet<String> stopWords;
    private String delimiterRegex;

    // --- 3. Performans Ölçümü ---
    private long indexingTimeMillis;
    
    // --- 4. CSV Regex (Sayfa 2'de verildi) ---
    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    // --- 5. Puanlama için Geçici Sıralama Sınıfı ---
    /**
     * Puanlama map'indeki (MyMap) sonuçları sıralayabilmek için
     * kullanılan geçici bir yardımcı sınıf.
     */
    public static class ArticleScore {
        String articleId;
        int score;

        ArticleScore(String articleId, int score) {
            this.articleId = articleId;
            this.score = score;
        }

        int getScore() {
            return score;
        }

        String getArticleId() {
            return articleId;
        }
    }


    /**
     * SearchEngine'i belirtilen HashMap konfigürasyonu ile başlatır.
     * BU, Benchmark.java'nın aradığı CONSTRUCTOR'dır.
     */
    public SearchEngine(double loadFactor, int hashType, int collisionType) {
        
        // Ana 'Article Map'i oluştur (Bizim HashMap'imiz)
        this.articleMap = new HashMap<>(loadFactor, hashType, collisionType);

        // Ana 'Index Map'i oluştur (Bizim HashMap'imiz)
        this.indexMap = new HashMap<>(loadFactor, hashType, collisionType);
        
        // 'İç-map'ler için kullanılacak varsayılan, hızlı ayarlar
        this.defaultInnerMapLF = loadFactor;
        this.defaultInnerMapHash = hashType;
        this.defaultInnerMapCollision = collisionType;
        
        this.indexingTimeMillis = 0;
        this.stopWords = new HashSet<>();
        this.delimiterRegex = "[\\p{Punct}\\s]"; // Varsayılan (fallback)
        
        loadStopWords("stop_words_en.txt");
    }
    


	// İç-map'leri oluşturmak için varsayılan ayarlar
    private final double defaultInnerMapLF;
    private final int defaultInnerMapHash;
    private final int defaultInnerMapCollision;


    private void loadStopWords(String filename) {
        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim().toLowerCase();
                if (!line.isEmpty()) {
                    stopWords.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Uyarı: Stop words dosyası bulunamadı: " + filename);
        }
    }
    
    /**
     * BU, Benchmark.java'nın aradığı METOT'tur.
     */
    public void loadArticles(String csvFile) {
        long startTime = System.currentTimeMillis();
        
        try (Scanner scanner = new Scanner(new File(csvFile), "UTF-8")) {
            
            if (scanner.hasNextLine()) { scanner.nextLine(); } // Başlığı atla

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;

                String[] fields = line.split(CSV_SPLIT_REGEX, -1);
                
                if (fields.length != 11) continue;
                
                for (int i = 0; i < fields.length; i++) {
                    fields[i] = fields[i].trim().replaceAll("^\"|\"$", "");
                }

                Article article = new Article(
                    fields[0], fields[1], fields[2], fields[3], fields[4], 
                    fields[5], fields[6], fields[7], fields[8], fields[9], fields[10]
                );

                articleMap.put(article.getId(), article);
                
                String articleId = article.getId();
                String textToIndex = article.getHeadLine() + " " + article.getArticleText();
                String[] words = cleanText(textToIndex);
                
                for (String word : words) {
                    MyMap<String, Integer> wordFrequencyMap = indexMap.get(word);
                    
                    if (wordFrequencyMap == null) {
                        wordFrequencyMap = new HashMap<>(
                            defaultInnerMapLF, 
                            HashMap.PAF,        
                            HashMap.DH
                        );
                        indexMap.put(word, wordFrequencyMap);
                    }
                    
                    Integer count = wordFrequencyMap.get(articleId);
                    if (count == null) {
                        count = 0;
                    }
                    wordFrequencyMap.put(articleId, count + 1);
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println("Hata: Ana CSV dosyası bulunamadı: " + csvFile);
            e.printStackTrace();
        }

        this.indexingTimeMillis = System.currentTimeMillis() - startTime;
    }
    
    public String[] cleanText(String text) {
        String lower = text.toLowerCase();
        String cleaned = lower.replaceAll(this.delimiterRegex, " ");
        String[] words = cleaned.trim().split("\\s+");
        
        ArrayList<String> filteredWords = new ArrayList<>();
        for (String word : words) {
            if (!word.isEmpty() && !stopWords.contains(word)) {
                filteredWords.add(word);
            }
        }
        return filteredWords.toArray(new String[0]);
    }

    public Article searchByID(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return articleMap.get(id.trim());
    }

public List<ArticleScore> searchByText(String query) {
        
        String[] queryWords = cleanText(query);
        
        MyMap<String, Integer> articleScores = new HashMap<>(
            0.8, HashMap.PAF, HashMap.DH
        );
        
        for (String word : queryWords) {
            MyMap<String, Integer> wordFrequencyMap = indexMap.get(word);
            
            if (wordFrequencyMap != null) {
                List<String> articleIdsInWord = wordFrequencyMap.getKeys();
                
                for (String articleId : articleIdsInWord) {
                    
                    Integer count = wordFrequencyMap.get(articleId); 
                    
                    if (count != null) {
                        Integer currentScore = articleScores.get(articleId);
                        if (currentScore == null) {
                            currentScore = 0; 
                        }
                        articleScores.put(articleId, currentScore + count);
                    }
                }
            }
        }
        
        List<String> scoredArticleIds = articleScores.getKeys();
        
        List<ArticleScore> scoreList = new ArrayList<>();
        for (String articleId : scoredArticleIds) {
            
            Integer score = articleScores.get(articleId);
            
            if (score != null) {
                scoreList.add(new ArticleScore(articleId, score));
            }
        }
        
        scoreList.sort(Comparator.comparing(ArticleScore::getScore).reversed());
        
        // --- DEĞİŞİKLİK BURADA ---
        // Artık Article nesnesini aramıyoruz, sadece puan listesinin
        // ilk 5 elemanını (veya daha azı varsa hepsini) döndürüyoruz.
        
        int toIndex = Math.min(5, scoreList.size());
        return scoreList.subList(0, toIndex);
    }

    // --- Performans Matrisi için Yardımcı Metotlar ---

    public long getIndexingTime() {
        return this.indexingTimeMillis;
    }

    public long getMapCollisionCount() {
        return articleMap.getCollisionCount() + indexMap.getCollisionCount();
    }
    
    public MyMap<String, MyMap<String, Integer>> getIndexMap() {
        return indexMap;
    }
}