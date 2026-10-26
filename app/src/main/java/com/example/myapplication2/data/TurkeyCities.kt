package com.example.myapplication2.data

import com.example.myapplication2.model.City
import java.util.Locale

/**
 * Türkiye'nin 81 ili. Koordinatlar il merkezindedir; hava isteği bu noktalara gider.
 *
 * Sıra, Türkçe alfabenin harf düzenidir (I, ı'dan sonra İ gelir; Ç, C'den sonra).
 * Liste kaynakta karışık durursa bile [all] her zaman bu sırayı üretir.
 */
internal object TurkeyCities {

    private val CITIES: List<City> = listOf(
        city(1, "Adana", 37.0000, 35.3213),
        city(2, "Adıyaman", 37.7648, 38.2786),
        city(3, "Afyonkarahisar", 38.7507, 30.5567),
        city(4, "Ağrı", 39.7191, 43.0503),
        city(68, "Aksaray", 38.3687, 34.0370),
        city(5, "Amasya", 40.6499, 35.8353),
        city(6, "Ankara", 39.9334, 32.8597),
        city(7, "Antalya", 36.8969, 30.7133),
        city(75, "Ardahan", 41.1105, 42.7022),
        city(8, "Artvin", 41.1828, 41.8183),
        city(9, "Aydın", 37.8444, 27.8458),
        city(10, "Balıkesir", 39.6484, 27.8826),
        city(74, "Bartın", 41.6344, 32.3375),
        city(72, "Batman", 37.8812, 41.1351),
        city(69, "Bayburt", 40.2552, 40.2249),
        city(11, "Bilecik", 40.0567, 30.0665),
        city(12, "Bingöl", 38.8854, 40.4983),
        city(13, "Bitlis", 38.4006, 42.1095),
        city(14, "Bolu", 40.7360, 31.6061),
        city(15, "Burdur", 37.7203, 30.2908),
        city(16, "Bursa", 40.1885, 29.0610),
        city(17, "Çanakkale", 40.1553, 26.4142),
        city(18, "Çankırı", 40.6013, 33.6134),
        city(19, "Çorum", 40.5506, 34.9556),
        city(20, "Denizli", 37.7765, 29.0864),
        city(21, "Diyarbakır", 37.9144, 40.2306),
        city(81, "Düzce", 40.8438, 31.1565),
        city(22, "Edirne", 41.6769, 26.5557),
        city(23, "Elazığ", 38.6810, 39.2264),
        city(24, "Erzincan", 39.7500, 39.4928),
        city(25, "Erzurum", 39.9043, 41.2679),
        city(26, "Eskişehir", 39.7767, 30.5206),
        city(27, "Gaziantep", 37.0662, 37.3833),
        city(28, "Giresun", 40.9128, 38.3895),
        city(29, "Gümüşhane", 40.4386, 39.5086),
        city(30, "Hakkari", 37.5744, 43.7408),
        city(31, "Hatay", 36.2025, 36.1600),
        city(76, "Iğdır", 39.9237, 44.0450),
        city(32, "Isparta", 37.7648, 30.5566),
        city(34, "İstanbul", 41.0082, 28.9784),
        city(35, "İzmir", 38.4237, 27.1428),
        city(46, "Kahramanmaraş", 37.5858, 36.9371),
        city(78, "Karabük", 41.2061, 32.6204),
        city(70, "Karaman", 37.1759, 33.2287),
        city(36, "Kars", 40.6013, 43.0975),
        city(37, "Kastamonu", 41.3887, 33.7827),
        city(38, "Kayseri", 38.7312, 35.4787),
        city(71, "Kırıkkale", 39.8468, 33.5153),
        city(39, "Kırklareli", 41.7351, 27.2256),
        city(40, "Kırşehir", 39.1425, 34.1709),
        city(79, "Kilis", 36.7184, 37.1212),
        city(41, "Kocaeli", 40.7654, 29.9408),
        city(42, "Konya", 37.8746, 32.4932),
        city(43, "Kütahya", 39.4242, 29.9833),
        city(44, "Malatya", 38.3552, 38.3095),
        city(45, "Manisa", 38.6191, 27.4289),
        city(47, "Mardin", 37.3122, 40.7351),
        city(33, "Mersin", 36.8121, 34.6415),
        city(48, "Muğla", 37.2153, 28.3636),
        city(49, "Muş", 38.9462, 41.7539),
        city(50, "Nevşehir", 38.6244, 34.7239),
        city(51, "Niğde", 37.9667, 34.6833),
        city(52, "Ordu", 40.9839, 37.8764),
        city(80, "Osmaniye", 37.0742, 36.2478),
        city(53, "Rize", 41.0201, 40.5234),
        city(54, "Sakarya", 40.7731, 30.3948),
        city(55, "Samsun", 41.2867, 36.3300),
        city(56, "Siirt", 37.9333, 41.9500),
        city(57, "Sinop", 42.0231, 35.1531),
        city(58, "Sivas", 39.7477, 37.0179),
        city(63, "Şanlıurfa", 37.1591, 38.7969),
        city(73, "Şırnak", 37.5228, 42.4537),
        city(59, "Tekirdağ", 40.9780, 27.5110),
        city(60, "Tokat", 40.3167, 36.5544),
        city(61, "Trabzon", 41.0027, 39.7168),
        city(62, "Tunceli", 39.1079, 39.5401),
        city(64, "Uşak", 38.6823, 29.4082),
        city(65, "Van", 38.4891, 43.4089),
        city(77, "Yalova", 40.6500, 29.2667),
        city(66, "Yozgat", 39.8181, 34.8147),
        city(67, "Zonguldak", 41.4564, 31.7987)
    )

    // Ekrana bu sıra gider. Kaynak liste plaka sırasında durabilir; sıralama burada yapılır.
    val all: List<City> = CITIES.sortedWith(TurkishCityOrder)

    /**
     * Tek bir il.
     * [plate] listenin kimliğidir (1 = Adana). Enlem ve boylam il merkezidir;
     * hava isteği bu noktaya gider. admin1 boş: alt yazı yalnızca "Türkiye" olur.
     */
    private fun city(plate: Int, name: String, latitude: Double, longitude: Double) = City(
        id = plate.toLong(),
        name = name,
        latitude = latitude,
        longitude = longitude,
        countryCode = "TR",
        country = "Türkiye",
        admin1 = null
    )
}

/**
 * Türkçe alfabe: a b c ç d e f g ğ h ı i j k l m n o ö p r s ş t u ü v y z.
 * Büyük I, Türkçe yerelde ı'ya iner; İ ise i'ye iner. Bu yüzden Isparta, İstanbul'dan önce gelir.
 */
private object TurkishCityOrder : Comparator<City> {

    override fun compare(left: City, right: City): Int {
        // Türkçe'de "I".lowercase() "ı", "İ".lowercase() "i" olur. Böylece Isparta, İstanbul'dan önce gelir.
        val a = left.name.lowercase(TURKISH)
        val b = right.name.lowercase(TURKISH)
        val limit = minOf(a.length, b.length)
        for (index in 0 until limit) {
            val compared = rank(a[index]).compareTo(rank(b[index]))
            if (compared != 0) return compared
        }
        return a.length.compareTo(b.length)
    }

    private fun rank(letter: Char): Int = ALPHABET_INDEX[letter] ?: (ALPHABET.length + letter.code)

    private val TURKISH: Locale = Locale.forLanguageTag("tr-TR")
    private const val ALPHABET = "abc\u00e7defg\u011fh\u0131ijklmno\u00f6prs\u015ftu\u00fcvyz"
    private val ALPHABET_INDEX: Map<Char, Int> = ALPHABET.withIndex().associate { it.value to it.index }
}
