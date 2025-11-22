public class Article {

    String id;
    String author;
    String datePublished;
    String category;
    String section;
    String url;
    String headline;
    String description;
    String keywords;
    String secondHeadline;
    String articleText;


    public Article(String id, String author, String datePublished, String category, String section, String url,
                   String headline, String description, String keywords, String secondHeadline, String articleText) {
        this.id = id;
        this.author = author;
        this.datePublished = datePublished;
        this.category = category;
        this.section = section;
        this.url = url;
        this.headline = headline;
        this.description = description;
        this.keywords = keywords;
        this.secondHeadline = secondHeadline;
        this.articleText = articleText;
    }

    public String getId() {
        return id;
    }

    public String getHeadLine() {
        return headline;
    }

    public String getAuthor() {
        return author;
    }

    public String toString() {
        return "--- Makale ID: " + id + " ---\n" +
                "Başlık: " + headline + "\n" +
                "Yazar: " + author + "\n" +
                "Tarih: " + datePublished + "\n" +
                "Kategori: " + category + "\n" +
                "-----------------------------------\n" +
                "Metin : " + (articleText) + "\n";
    }

    public String getDatePublished() {
        return datePublished;
    }

    public String getCategory() {
        return category;
    }

    public String getArticleText() {
        return articleText;
    }
}