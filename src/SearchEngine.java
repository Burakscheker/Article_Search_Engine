import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList; // ArrayList kullanıyoruz
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SearchEngine {

	public MyMap<String, Article> articleMap;
	public MyMap<String, MyMap<String, Integer>> indexMap;
	
	private ArrayList<String> stopWords; 
	
	private long indexingTimeMillis;

	private static final Pattern CSV_PATTERN = Pattern.compile("\"([^\"]*)\"|[^,]+");  // Mailde sorduğumuzda kullanabiliceğimizi söylemiştiniz// CSV satırlarını parçalamak için Regex kuralı. 
    // Tırnak ("...") içindeki virgülleri ayraç olarak görmezden gelir.

	public static class ArticleScore {
		String articleId;
		int score;

		ArticleScore(String articleId, int score) {
			this.articleId = articleId;
			this.score = score;
		}

		public String getArticleId() {
			return articleId;
		}

		public int getScore() {
			return score;
		}
	}

	public SearchEngine(double loadFactor, int hashType, int collisionType) {
		this.articleMap = new HashMap<>(loadFactor, hashType, collisionType);
		this.indexMap = new HashMap<>(loadFactor, hashType, collisionType);

		this.indexingTimeMillis = 0;
		
		this.stopWords = new ArrayList<>();
		loadStopWords("stop_words_en.txt");
	}

	private void loadStopWords(String filename) { // stop words leri txt ten oku
		try {
			Scanner scanner = new Scanner(new File(filename));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim().toLowerCase();
				if (!line.isEmpty()) {
					stopWords.add(line);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("Uyari: Stop words dosyasi bulunamadi.");
		}
	}

	public void loadArticles(String csvFile) {
		long start = System.currentTimeMillis();

		try {
			Scanner scanner = new Scanner(new File(csvFile), "UTF-8");

			if (scanner.hasNextLine())
				scanner.nextLine(); 

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.trim().isEmpty())
					continue;

				String[] fields = csvtoList(line);

				if (fields.length != 11)
					continue;

				for (int i = 0; i < fields.length; i++) {    // CSV'den gelen tırnak işaretlerini temizle
					fields[i] = fields[i].trim();
					if (fields[i].startsWith("\"") && fields[i].endsWith("\"")) {
						fields[i] = fields[i].substring(1, fields[i].length() - 1);
					}
					fields[i] = fields[i].replace("\"", "");
				}

				Article article = new Article(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5],
						fields[6], fields[7], fields[8], fields[9], fields[10]);

				articleMap.put(article.getId(), article);

				String textToIndex = article.getHeadLine() + " " + article.getArticleText();
				String[] words = cleanText(textToIndex);

				for (String word : words) {
					MyMap<String, Integer> wordFrequencyMap = indexMap.get(word);

					if (wordFrequencyMap == null) {    // kelime daha önce hiç görülmediyse yeni map oluştur.
						wordFrequencyMap = new HashMap<>(0.8, HashMap.PAF, HashMap.DH);
						indexMap.put(word, wordFrequencyMap);
					}

					Integer count = wordFrequencyMap.get(article.getId());       
					if (count == null)
						count = 0;

					wordFrequencyMap.put(article.getId(), count + 1);
				}
			}
			scanner.close();

		} catch (FileNotFoundException e) {
			System.out.println("Hata: CSV dosyasi yok: " + csvFile);
		}

		this.indexingTimeMillis = System.currentTimeMillis() - start;
	}

	private String[] csvtoList(String line) {
		List<String> list = new ArrayList<>();
		Matcher matcher = CSV_PATTERN.matcher(line);

		while (matcher.find()) {   // Bulduğu sürece döngü devam eder.
			String match = matcher.group();
			
			if (matcher.group(1) != null) {
				list.add(matcher.group(1));   // Sadece tırnağın içindekini listeye ekle
			} else {
				list.add(match.replace(",", ""));  // Eğer group(1) null ise Virgülleri temizleyip ekle
			}
		}

		return list.toArray(new String[0]);
	}

	public String[] cleanText(String text) {
		String lower = text.toLowerCase();
		String cleaned = lower.replaceAll("[^a-z]", " ");  // a'dan z'ye olan küçük harfler dışındakiher şeyi boşluga cevir 
		String[] words = cleaned.trim().split("\\s+");    // bosluklardan bolup array haline getir 

		ArrayList<String> filteredWords = new ArrayList<>();
		for (String word : words) {
			if (!word.isEmpty() && !stopWords.contains(word)) {
				filteredWords.add(word);
			}
		}
		return filteredWords.toArray(new String[0]);
	}

	public Article searchByID(String id) {
		if (id == null)
			return null;
		return articleMap.get(id.trim());
	}

	public List<ArticleScore> searchByText(String query) {
		String[] queryWords = cleanText(query);
		MyMap<String, Integer> articleScores = new HashMap<>(0.8, HashMap.PAF, HashMap.DH);

		for (String word : queryWords) {
			MyMap<String, Integer> wordFrequencyMap = indexMap.get(word);

			if (wordFrequencyMap != null) {
				List<String> articleIdsInWord = wordFrequencyMap.getKeys();   // kelimenin gectigi tüm makale id lerini al 

				for (String articleId : articleIdsInWord) {  // kelimenin gectigi maaleleri gez 
					Integer count = wordFrequencyMap.get(articleId);
					if (count != null) {
						Integer currentScore = articleScores.get(articleId);
						if (currentScore == null)
							currentScore = 0;
						articleScores.put(articleId, currentScore + count);
					}
				}
			}
		}

		List<String> scoredArticleIds = articleScores.getKeys();
		List<ArticleScore> scoreList = new ArrayList<>();

		for (String articleId : scoredArticleIds) {
			scoreList.add(new ArticleScore(articleId, articleScores.get(articleId)));
		}

		Collections.sort(scoreList, new Comparator<ArticleScore>() {  // skorları karsılastır büyükten kucuge  	sırala
			public int compare(ArticleScore o1, ArticleScore o2) {
				return Integer.compare(o2.score, o1.score);
			}
		});

		if (scoreList.size() > 5) {
			return scoreList.subList(0, 5);
		} else {
			return scoreList;
		}
	}

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