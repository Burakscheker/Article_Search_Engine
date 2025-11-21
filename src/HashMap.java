

import java.util.ArrayList;
import java.util.List;

public class HashMap<K, V> implements MyMap<K, V> {

    // --- Ödevde İstenen Sabitler (Sayfa 2-3) ---
    public static final int SSF = 0; // Simple Summation Function
    public static final int PAF = 1; // Polynomial Accumulation Function

    public static final int LP = 0; // Linear Probing
    public static final int DH = 1; // Double Hashing

    // --- Dahili Veri Yapıları ---
    private K[] keys;   // Anahtarları (String) tutan dizi
    private V[] values; // Değerleri (Article, HashMap) tutan dizi

    private int capacity;       // Tablo kapasitesi (N)
    private int size;           // Mevcut eleman sayısı
    private long collisionCount; // Çarpışma sayacı

    // --- Performans Ayarları ---
    private final double maxLoadFactor; // Yük faktörü (α)
    private final int hashType;      // SSF mi, PAF mı
    private final int collisionType;  // LP mi, DH mı

    // --- DH için Gerekli (Sayfa 3) ---
    private int primeQ; // Kapasiteden küçük bir asal sayı (q)
    private static final int DEFAULT_INITIAL_CAPACITY = 11;

    /**
     * HashMap'i belirtilen konfigürasyon ile oluşturan ana constructor.
     */
    public HashMap(double maxLoadFactor, int hashType, int collisionType) {
        this.capacity = findNextPrime(DEFAULT_INITIAL_CAPACITY);
        
        this.maxLoadFactor = maxLoadFactor;
        this.hashType = hashType;
        this.collisionType = collisionType;

        // Java'da 'new K[capacity]' yapılamadığı için Object dizisi yaratıp 'cast' ediyoruz.
        this.keys = (K[]) new Object[this.capacity];
        this.values = (V[]) new Object[this.capacity];

        this.size = 0;
        this.collisionCount = 0;

        // Eğer Double Hashing seçiliyse, 'q' değerini hesapla
        if (collisionType == DH) {
            updatePrimeQ();
        }
    }

    // --- 1. ANA METOTLAR (PUT, GET) ---

    /**
     * Anahtara karşılık gelen değeri ekler.
     * Eğer yük faktörü aşılırsa, önce 'resize' işlemini tetikler.
     */
    @Override
    public void put(K key, V value) {
        // 1. Doluluk oranını (load factor) kontrol et (Sayfa 2)
        if ((size + 1.0) / capacity > maxLoadFactor) {
            resize(); // Eğer doluluk aşıldıysa tabloyu büyüt
        }

        // 2. Veriyi yerleştirmesi için 'insert' yardımcısını çağır
        // Bu, 'resize' içindeki 'put' çağrısını engeller.
        insert(key, value);
    }

    /**
     * Anahtara karşılık gelen değeri bulur ve döndürür.
     * Eğer anahtar bulunamazsa 'null' döndürür.
     */
    @Override
    public V get(K key) {
        int hashCode = hash(key); // Anahtarın int hash kodunu al (SSF veya PAF)
        int step = 1; // Varsayılan adım (LP için)

        // Eğer Double Hashing (DH) seçiliyse, adımı yeniden hesapla (Sayfa 3)
        if (collisionType == DH) {
            step = secondaryHash_DH(hashCode);
        }

        // Açık adresleme (Open Addressing) arama döngüsü
        for (int i = 0; i < capacity; i++) {
            int index = compress(hashCode + i * step);

            // Durum 1: Aradığımız indeksi bulduk (Anahtar eşleşti)
            if (keys[index] != null && keys[index].equals(key)) {
                return values[index]; // Değeri döndür
            }

            // Durum 2: İndeks boş (null).
            // Açık adresleme kuralı: Eğer aradığımız anahtar olsaydı,
            // bu boşluğa gelene kadar bulunurdu. Demek ki anahtar map'te yok.
            if (keys[index] == null) {
                return null; // Bulunamadı
            }
            
            // Durum 3: İndeks dolu ama *farklı* bir anahtar var.
            // Döngüye devam et (i artacak).
        }

        // Tüm tablo arandı ve bulunamadı
        return null;
    }

    // --- 2. YARDIMCI METOTLAR (RESIZE, INSERT) ---

    /**
     * Doluluk oranı kontrolü YAPMADAN anahtar/değeri tabloya yerleştiren
     * özel yardımcı metot. Sadece 'put' ve 'resize' tarafından kullanılır.
     * Bu, 'resize' sırasında sonsuz döngüyü engeller.
     */
    private void insert(K key, V value) {
        int hashCode = hash(key); 
        int step = 1; 

        if (collisionType == DH) {
            step = secondaryHash_DH(hashCode);
        }

        // Açık adresleme (Open Addressing) döngüsü
        for (int i = 0; i < capacity; i++) {
            int index = compress(hashCode + i * step);

            // i > 0 ise, bu bir çarpışmadır. Sayacı artır (Sayfa 4)
            if (i > 0) {
                this.collisionCount++;
            }

            // Durum 1: İndeks boş (null). Yeni veriyi buraya ekle.
            if (keys[index] == null) {
                keys[index] = key;
                values[index] = value;
                size++;
                return; // Ekleme tamamlandı, metottan çık
            }

            // Durum 2: İndekste aynı anahtar zaten var. Değeri güncelle.
            if (keys[index].equals(key)) {
                values[index] = value;
                return; // Güncelleme tamamlandı, metottan çık
            }
            
            // Durum 3: İndeks dolu ama *farklı* bir anahtar var (Çarpışma).
            // Döngüye devam et...
        }
        
        // Bu satıra ulaşılıyorsa, 'capacity' denemeye rağmen yer bulunamamıştır.
        throw new RuntimeException("Hash table is completely full. Cannot insert key: " + key);
    }

    /**
     * Tablo kapasitesini iki katına (yaklaşık) çıkaran ve
     * tüm elemanları yeniden hash'leyen (rehash) metot.
     */
    private void resize() {
        // 1. Eski dizileri yedekle
        K[] oldKeys = this.keys;
        V[] oldValues = this.values;
        
        // 2. Yeni kapasiteyi hesapla (2 katının bir sonraki asalı)
        this.capacity = findNextPrime(this.capacity * 2);
        
        // 3. Yeni (boş) dizileri oluştur
        this.keys = (K[]) new Object[this.capacity];
        this.values = (V[]) new Object[this.capacity];
        this.size = 0; // Boyutu sıfırla, 'insert' yeniden sayacak
        
        // 4. DH kullanılıyorsa 'q' değerini de güncelle
        if (collisionType == DH) {
            updatePrimeQ();
        }

        // 5. Benchmark için 'rehash' sırasındaki çarpışmaları sayma
        long oldCollisionCount = this.collisionCount; 
        
        // 6. Eski dizilerdeki tüm elemanları gez
        for (int i = 0; i < oldKeys.length; i++) {
            if (oldKeys[i] != null) {
                // 7. DEĞİŞİKLİK: 'put' yerine 'insert' çağır (Sonsuz döngüyü engeller)
                insert(oldKeys[i], oldValues[i]); 
            }
        }
        
        // 8. Ana çarpışma sayacını, 'resize' öncesi haline döndür
        this.collisionCount = oldCollisionCount;
    }

    // --- 3. HASH METOTLARI (SSF, PAF, DH) ---

    /**
     * Ana hash fonksiyonu (Wrapper).
     * Seçilen 'hashType'a göre SSF veya PAF'ı çağırır.
     */
    private int hash(K key) {
        String s = (String) key; // Projemizde anahtarlar hep String.
        
        if (hashType == PAF) {
            return hash_PAF(s);
        } else {
            return hash_SSF(s);
        }
    }

    /**
     * 2.1.1. Simple Summation Function (Sayfa 3)
     */
    private int hash_SSF(String key) {
        int hash = 0;
        for (int i = 0; i < key.length(); i++) {
            hash += (int) key.charAt(i);
        }
        return hash; // Java 'overflow'u otomatik yönetir
    }

    /**
     * 2.1.2. Polynomial Accumulation Function (Sayfa 3)
     * Horner Kuralı (overflow'u yönetir) ve z=33 kullanır.
     */
    private int hash_PAF(String key) {
        int hash = 0;
        int z = 33; 
        
        String lowerKey = key.toLowerCase(); // "case insensitive"
        
        for (int i = 0; i < lowerKey.length(); i++) {
            char ch = lowerKey.charAt(i);
            
            if (ch >= 'a' && ch <= 'z') { // Karakterler 1-26
                int charValue = (int) ch - (int) 'a' + 1;
                hash = (hash * z) + charValue; // Horner's Rule
            }
        }
        return hash; // Java 'overflow'u otomatik yönetir
    }

    /**
     * 2.3.2. Double Hashing (Sayfa 3)
     * İkincil hash fonksiyonu d(k) = q - (k mod q)
     */
    private int secondaryHash_DH(int hashCode) {
        int h = Math.abs(hashCode);
        
        // Sayfa 3: "d(k) cannot have zero values"
        if (h % primeQ == 0) {
            return 1; // 0 olmasını engelle
        }
        return primeQ - (h % primeQ);
    }
    
    /**
     * Sıkıştırma Fonksiyonu (Compression Function)
     * Hash kodunu, tablo kapasitesi (N) aralığına indirger.
     */
    private int compress(int hashCode) {
        return Math.abs(hashCode) % capacity;
    }

    // --- 4. MYMAP ARAYÜZÜNÜN DİĞER METOTLARI ---

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public void clear() {
        this.capacity = findNextPrime(DEFAULT_INITIAL_CAPACITY);
        this.keys = (K[]) new Object[this.capacity];
        this.values = (V[]) new Object[this.capacity];
        this.size = 0;
        this.collisionCount = 0;
        if (collisionType == DH) {
            updatePrimeQ();
        }
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public long getCollisionCount() {
        return this.collisionCount;
    }

    /**
     * Map'teki tüm anahtarları (key) bir List olarak döndürür.
     * 'searchByText' metodunun puanlama yapabilmesi için gereklidir.
     */
    @Override
    public List<K> getKeys() {
        List<K> keyList = new ArrayList<>();
        
        // 'keys' dizimizin (hash tablomuzun) tamamını geziyoruz
        for (int i = 0; i < capacity; i++) {
            // Eğer o slot (göz) boş değilse (doluysa)
            if (keys[i] != null) {
                // Anahtarı listeye ekle
                keyList.add(keys[i]);
            }
        }
        return keyList;
    }
    
    // --- 5. ASAL SAYI YARDIMCI METOTLARI (DH ve Resize için) ---

    /**
     * 'primeQ' (q) değerini, mevcut kapasiteden (N) küçük
     * en yakın asal sayı olarak günceller. (Sayfa 3: q < N)
     */
    private void updatePrimeQ() {
        this.primeQ = findPreviousPrime(this.capacity);
    }

    /**
     * n'den küçük en büyük asal sayıyı bulur.
     */
    private int findPreviousPrime(int n) {
        for (int i = n - 1; i > 1; i--) {
            if (isPrime(i)) {
                return i;
            }
        }
        return 2; // En kötü durum
    }
    
    /**
     * n'den büyük veya eşit en küçük asal sayıyı bulur.
     * Tablo kapasitesinin (N) asal olmasını sağlar (Sayfa 3)
     */
    private int findNextPrime(int n) {
        int num = n;
        if (num <= 2) return 2;
        if (num % 2 == 0) num++; // Çiftse tek yap
        while (!isPrime(num)) {
            num += 2; // Sadece tek sayıları kontrol et
        }
        return num;
    }

    /**
     * Bir sayının asal olup olmadığını kontrol eder.
     */
    private boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        // 6k ± 1 optimizasyonu
        for (int i = 5; i * i <= n; i = i + 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }
}