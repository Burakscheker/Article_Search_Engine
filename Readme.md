# Article Search Engine

Bu proje, büyük ölçekli haber verileri (CNN Makaleleri) üzerinde hızlı ve verimli aramalar yapabilmek için geliştirilmiş Java tabanlı bir arama motorudur. Proje, standart Java koleksiyonları yerine performansı optimize etmek amacıyla özel olarak implement edilmiş veri yapılarını ve hash algoritmalarını kullanır.

## 🚀 Özellikler

* **Özel Veri Yapıları:** Performansı artırmak için çakışma (collision) yönetimi ve hash algoritmaları özelleştirilmiş bir `HashMap` yapısı geliştirilmiştir.
* **Gelişmiş Hash Algoritmaları:**
    * **SSF (Simple Summation Function):** Karakterlerin ASCII değerlerinin toplamına dayalı temel hash hesaplama.
    * **PAF (Polynomial Accumulation Function):** Çakışmaları minimize eden polinom tabanlı gelişmiş hash hesaplama.
* **Çakışma Çözümleme Stratejileri:**
    * **Linear Probing (Doğrusal Yoklama).**
    * **Double Hashing (Çift Hashleme).**
* **Metin Ön İşleme:** Makale metinleri işlenirken `stop_words_en.txt` dosyasındaki durak kelimeler otomatik olarak filtrelenir.
* **Performans Analizi (Benchmarking):** Farklı yük faktörleri (Load Factor) ve algoritma kombinasyonları için `search.txt` dosyasındaki veriler kullanılarak detaylı hız ve çakışma testleri gerçekleştirilir.

## 🛠 Kullanılan Teknolojiler

* **Dil:** Java
* **Veri Kaynağı:** Yaklaşık 160MB boyutunda, Git LFS ile yönetilen `CNN_Articels.csv` dosyası.
* **Veri Yönetimi:** .gitattributes üzerinden yapılandırılmış LFS filtreleri.
* **Metin İşleme:** Regex (Düzenli İfadeler) ile CSV ayrıştırma ve metin temizleme işlemleri.

## 📋 Kurulum

1.  Projeyi yerel makinenize klonlayın:
    ```bash
    git clone [https://github.com/Burakscheker/TradeEngine.git](https://github.com/Burakscheker/TradeEngine.git)
    ```
2.  Proje kök dizininde şu dosyaların bulunduğundan emin olun:
    * `CNN_Articels.csv`
    * `stop_words_en.txt`
    * `search.txt`
3.  Projeyi bir Java IDE'si (IntelliJ IDEA, Eclipse vb.) ile açın veya terminal üzerinden derleyin.

## 💻 Kullanım

Uygulama başlatıldığında interaktif bir menü üzerinden şu işlemler gerçekleştirilebilir:

1.  **Metin ile Arama:** Kullanıcıdan alınan kelimelere göre en alakalı 5 makaleyi skorlayarak getirir.
2.  **ID ile Arama:** Belirli bir makale ID'si girilerek o makaleye ait tüm detayların (Başlık, Yazar, Tarih, İçerik) görüntülenmesini sağlar.
3.  **Benchmark Testleri:** Farklı hash ve çakışma algoritmalarının performans sonuçlarını (indeksleme süresi, çakışma sayısı vb.) bir matris halinde sunar.
4.  **Çıkış:** Uygulamayı sonlandırır.

### Geliştiren : Ömür Burak Şeker

---
