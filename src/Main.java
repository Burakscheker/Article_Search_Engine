import java.util.List;
import java.util.Scanner;
public class Main {

	private static final String CSV_FILE = "CNN_Articels.csv";

	public static void main(String[] args) {

		Scanner scanner = new Scanner(System.in);
		Benchmark benchmark = new Benchmark();

		System.out.println("Starting News Search Engine...");

		SearchEngine engine = new SearchEngine(0.8, HashMap.PAF, HashMap.DH);

		System.out.println("Loading articles from '" + CSV_FILE + "'...");
		System.out.println("Please wait...");

		engine.loadArticles(CSV_FILE);

		System.out.println("\nLoading complete!");
		System.out.println("Total Indexing Time: " + engine.getIndexingTime() + " ms");
		System.out.println("Total Collisions: " + engine.getMapCollisionCount());
		System.out.println("Article Map Capacity: " + engine.articleMap.getCapacity());
		System.out.println("Index Map Capacity: " + engine.indexMap.getCapacity());

		while (true) {
			System.out.println("\n--- MAIN MENU ---");
			System.out.println("1. Search by text");
			System.out.println("2. Search by ID");
			System.out.println("3. Run Benchmark Tests");
			System.out.println("4. Exit");
			System.out.print("Choice (1-4): ");

			String choice = scanner.nextLine();

			switch (choice) {
			case "1":
				System.out.print("Enter search query: ");
				String input = scanner.nextLine();  // frekansı istenen kelime 

				List<SearchEngine.ArticleScore> scoreResults = engine.searchByText(input); // puanları list olarak tut 
				String[] inputWords = engine.cleanText(input);

				if (scoreResults.isEmpty()) {
					System.out.println("No results found for '" + input + "'.");
					
				} else {
					System.out.println("\nTop 5 results for '" + input + "':");
					System.out.println("\n--- RELEVANCE SCORE CALCULATION (Occurrences) ---");
					System.out.println();


					System.out.print("WORD\t\t");
					for (SearchEngine.ArticleScore score : scoreResults) {
						System.out.print("ID(" + score.getArticleId() + ")\t");
					}

					System.out.println(
							"\n---------------------------------------------------------------------------------------------");

					for (String word : inputWords) {
						System.out.print(word + "\t\t");

						for (SearchEngine.ArticleScore score : scoreResults) {           
							String articleId = score.getArticleId();
							int count = 0;

							MyMap<String, Integer> wordMap = engine.indexMap.get(word);  // bu kelime hangi makalelerde geçiyor 
							if (wordMap != null) {
								Integer freq = wordMap.get(articleId);    // kelime makalede geciyorsa sayısını al 
								if (freq != null) {
									count = freq;
								}
							}
							System.out.print(count + "\t\t");
						}
						System.out.println();
					}

					System.out.println(
							"---------------------------------------------------------------------------------------------");

					System.out.print("TOTAL\t\t");
					for (SearchEngine.ArticleScore score : scoreResults) {
						System.out.print(score.getScore() + "\t\t");
					}

					System.out.println("\n\nUse option '2' to see article details.");
				}
				break;

			case "2":
				System.out.print("Enter Article ID: ");
				String id = scanner.nextLine();

				Article article = engine.searchByID(id);

				if (article == null) {
					System.out.println("Article not found.");
				} else {
					System.out.println(article.toString());
				}
				break;

			case "3":
				System.out.println("Starting Performance Tests...");
				benchmark.runTests();
				System.out.println("Benchmark complete.");
				break;

			case "4":
				System.out.println("Exiting...");
				scanner.close();
				return;

			default:
				System.out.println("Invalid choice! Please enter 1, 2, 3, or 4.");
				break;
			}
		}
	}
}