# Hava Durumu

Türkiye’nin 81 ili için anlık hava durumunu, dünya genelindeki şehir aramaları için ise saatlik ve 7 günlük tahmini sunan Android uygulamasıdır. Hava verisi [Open-Meteo](https://open-meteo.com/) servislerinden alınır. API anahtarı veya kullanıcı hesabı gerekmez.

Arayüz Jetpack Compose ve Material 3 ile geliştirilmiştir. Açık ve koyu tema, cihazın sistem ayarına göre uygulanır.

## Özellikler

- Uygulama açıldığında 81 il, Türkçe alfabetik sırada listelenir. 
- Arama metni temizlendiğinde Türkiye listesi, yeniden istenmeden geri yüklenir.
- Türkiye’deki kayıtlar hava durumuna göre etiketlenir. Kart üzerinde ilin konumunu gösteren bir Türkiye silüeti bulunur.
- Tahmin ekranında koşullara göre güncellenen bir hava sahnesi yer alır. Açık hava, bulut, yağış, kar ve fırtına ayrı sahnelerle çizilir.
- Anlık görünümde sıcaklık, hissedilen sıcaklık, nem ve rüzgâr hızı sunulur.
- Saatlik tahmin yatay bir listede, 7 günlük tahmin ise günün en düşük ve en yüksek sıcaklığıyla gösterilir.
- Yükleniyor, sonuç yok ve hata durumları birbirinden ayrılır. Hata durumunda istek yenilenebilir.

## Ekran görüntüleri

<img width="412" height="800" alt="image" src="https://github.com/user-attachments/assets/55f2a80e-1969-440a-acae-f8c87166394d" />
<img width="375" height="793" alt="image" src="https://github.com/user-attachments/assets/0815cd7f-c0d1-4466-9dff-44a005a3ff2e" />



