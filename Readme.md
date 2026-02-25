# 🚀 Article Search Engine

This project is a Java-based search engine developed to perform fast and efficient searches on large-scale news data (CNN Articles). Instead of standard Java collections, the project uses custom-implemented data structures and hash algorithms to optimize performance.

## 🌟 Features

* **Custom Data Structures:** A custom `HashMap` structure with specialized collision management and hash algorithms has been developed to increase performance.
* **Advanced Hash Algorithms:**
    * **SSF (Simple Summation Function):** Basic hash calculation based on the sum of the ASCII values of the characters.
    * **PAF (Polynomial Accumulation Function):** Advanced polynomial-based hash calculation that minimizes collisions.
* **Collision Resolution Strategies:**
    * **Linear Probing.**
    * **Double Hashing.**
* **Text Preprocessing:** Stop words from the `stop_words_en.txt` file are automatically filtered out when processing article texts.
* **Performance Analysis (Benchmarking):** Detailed speed and collision tests are performed using the data in the `search.txt` file for different load factors and algorithm combinations.

## 🛠️ Tech Stack

* **Language:** Java
* **Data Source:** An approximately 160MB `CNN_Articels.csv` file managed with Git LFS.
* **Data Management:** LFS filters configured via `.gitattributes`.
* **Text Processing:** CSV parsing and text cleaning operations using Regex (Regular Expressions).

## 📋 Installation

1. Clone the project to your local machine:
   ```bash
   git clone [https://github.com/Burakscheker/Article_Search_Engine.git](https://github.com/Burakscheker/Article_Search_Engine.git)
2.  Ensure the following files are present in the project's root directory:
    * `CNN_Articels.csv`
    * `stop_words_en.txt`
    * `search.txt`
3.  Open the project with a Java IDE (IntelliJ IDEA, Eclipse, etc.) or compile it via the terminal.

## 💻 Usage

When the application is launched, the following operations can be performed via an interactive menu:

**Search by Text:** Scores and retrieves the top 5 most relevant articles based on the words entered by the user.

**Search by ID:** Displays all details (Headline, Author, Date, Content) of a specific article by entering its ID.

**Benchmark Tests:** Presents the performance results (indexing time, number of collisions, etc.) of different hash and collision algorithms in a matrix format.

**Exit:** Terminates the application.

### Developer : Ömür Burak Şeker

---
