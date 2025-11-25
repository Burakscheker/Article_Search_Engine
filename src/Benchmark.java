import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Benchmark {

	private static final String CSV_FILE = "CNN_Articels.csv";
	private static final String SEARCH_WORDS_FILE = "search.txt";

	public void runTests() {

		double[] loadFactors = { 0.5, 0.8 };

		int[] hashTypes = { HashMap.SSF, HashMap.PAF };
		int[] collisionTypes = { HashMap.LP, HashMap.DH };

		String[] hashNames = { "SSF", "PAF" };
		String[] collisionNames = { "LP", "DH" };

		List<String> searchWords = loadSearchWords(SEARCH_WORDS_FILE);
		if (searchWords.isEmpty()) {
			System.out.println("Test iptal (search.txt okunamadi).");
			return;
		}
		System.out.println(searchWords.size() + " search words loaded.");
		System.out.println("Running benchmark tests...\n");

		System.out.println("--- Performance Matrix ---");
		System.out.println("LF\tHash\tCol.\tCol.Count\tIndexTime\tSearchTime");
		System.out.println("----------------------------------------------------------------");

		for (double lf : loadFactors) {     
			for (int htIndex = 0; htIndex < hashTypes.length; htIndex++) {
				for (int ctIndex = 0; ctIndex < collisionTypes.length; ctIndex++) {

					int ht = hashTypes[htIndex];
					int ct = collisionTypes[ctIndex];

					SearchEngine engine = new SearchEngine(lf, ht, ct);   // 8 ihtimal icin engine olustur 

					engine.loadArticles(CSV_FILE);    

					long indexingTime = engine.getIndexingTime();
					long collisionCount = engine.getMapCollisionCount();

					MyMap<String, MyMap<String, Integer>> indexMap = engine.getIndexMap();   

					long searchStartTime = System.nanoTime();

					for (String word : searchWords) {   
						indexMap.get(word);   // her bir searchword için get yap ve süreyi ölç 
					}

					long searchEndTime = System.nanoTime();
					long totalSearchTimeNanos = searchEndTime - searchStartTime;
					double avgSearchTimeNanos = (double) totalSearchTimeNanos / searchWords.size();
					double avgSearchTimeMicros = avgSearchTimeNanos / 1000.0;

					
					String lfStr = "";
					if (lf == 0.5) {
						lfStr = "0.5";
					} else {
						lfStr = "0.8";
					}

					System.out.println(lfStr + "\t" + hashNames[htIndex] + "\t" + collisionNames[ctIndex] + "\t"
							+ collisionCount + "\t\t" + indexingTime + "\t\t" + (float) avgSearchTimeMicros);
				}
			}
		}

		System.out.println("----------------------------------------------------------------");
		System.out.println("Benchmark finished.");
	}

	private List<String> loadSearchWords(String filename) {  // search.txt okudugumuz metot
		List<String> words = new ArrayList<>();
		try {
			Scanner scanner = new Scanner(new File(filename));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim().toLowerCase();
				if (!line.isEmpty()) {
					words.add(line);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("Hata: Dosya yok: " + filename);
		}
		return words;
	}
}