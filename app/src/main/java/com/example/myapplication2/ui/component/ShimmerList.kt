package com.example.myapplication2.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication2.ui.theme.HavaTheme

/**
 * Yükleniyor durumunun görünümü.
 *
 * Shimmer, içerik gelmeden önce içeriğin **iskeletini** gösterir ve üzerinden
 * kayan bir ışıltı geçirir. Dönen bir çark yerine bunu tercih ederiz; çünkü
 * kullanıcı ne geleceğini önceden görür ve bekleme daha kısa hissedilir.
 *
 * Shimmer, Compose'a hazır gelmez; aşağıdaki animasyonu kendimiz yazıyoruz.
 */
@Composable
fun ShimmerList(modifier: Modifier = Modifier, itemCount: Int = 6) {
    val brush = shimmerBrush()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Gerçek kartlarla aynı ölçülerde boş kartlar çizilir.
        repeat(itemCount) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.height(76.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(44.dp)
                            .background(brush, CircleShape)
                    )
                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .height(16.dp)
                                .background(brush, RoundedCornerShape(8.dp))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.35f)
                                .height(12.dp)
                                .background(brush, RoundedCornerShape(8.dp))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 12.dp)
                            .width(102.dp)
                            .height(60.dp)
                            .border(width = 1.5.dp, brush = brush, shape = RoundedCornerShape(16.dp))
                    )
                }
            }
        }
    }
}

/**
 * Soldan sağa kayan ışıltı efekti.
 *
 * `rememberInfiniteTransition` + `infiniteRepeatable`: bitmeyen bir animasyon kurar.
 * Değişen değer, gradient'in yatay konumudur; her karede biraz sağa kayar.
 */
@Composable
private fun shimmerBrush(): Brush {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface
    val translate by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0f,
        targetValue = SHIMMER_DISTANCE,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(translate - SHIMMER_WIDTH, 0f),
        end = Offset(translate, 0f)
    )
}

private const val SHIMMER_DISTANCE = 1_500f
private const val SHIMMER_WIDTH = 400f

@Preview(showBackground = true)
@Composable
private fun ShimmerListPreview() {
    HavaTheme {
        ShimmerList()
    }
}
