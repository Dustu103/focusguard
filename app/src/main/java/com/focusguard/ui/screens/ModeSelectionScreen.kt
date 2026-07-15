package com.focusguard.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModeSelectionScreen(onFocusModeSelected: () -> Unit, onParentalModeSelected: () -> Unit) {

    // Entrance animation
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 700, easing = EaseOut),
        label = "entrance"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A1A),
                        Color(0xFF0D1B2A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header
            Spacer(Modifier.height(48.dp))
            Text(
                "Choose Your Mode",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "How will you use FocusGuard?",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            // Self Focus Card
            ModeCard(
                icon = Icons.Default.SelfImprovement,
                iconTint = Color(0xFF6C63FF),
                iconBg = Color(0xFF6C63FF).copy(alpha = 0.15f),
                title = "Self Focus",
                subtitle = "Block distracting apps for yourself",
                bullets = listOf(
                    "Set timed app blocks",
                    "Commitment lock — can't quit early",
                    "Beat your own bad habits"
                ),
                badgeText = null,
                gradient = listOf(Color(0xFF1A1040), Color(0xFF0F0F2E)),
                borderColor = Color(0xFF6C63FF).copy(alpha = 0.5f),
                onClick = onFocusModeSelected
            )

            Spacer(Modifier.height(20.dp))

            // Parental Mode Card
            ModeCard(
                icon = Icons.Default.FamilyRestroom,
                iconTint = Color(0xFF00C896),
                iconBg = Color(0xFF00C896).copy(alpha = 0.15f),
                title = "Parental Control",
                subtitle = "Manage your child's screen time",
                bullets = listOf(
                    "PIN-protected — child can't bypass",
                    "Permanent app blocking",
                    "Device Admin protection"
                ),
                badgeText = "Coming Soon",
                gradient = listOf(Color(0xFF0A1F1A), Color(0xFF071510)),
                borderColor = Color(0xFF00C896).copy(alpha = 0.3f),
                onClick = onParentalModeSelected
            )

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun ModeCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    bullets: List<String>,
    badgeText: String?,
    gradient: List<Color>,
    borderColor: Color,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(gradient))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(listOf(borderColor, borderColor.copy(alpha = 0.1f))),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable {
                pressed = true
                onClick()
            }
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(iconBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        if (badgeText != null) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFFF6B35).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    badgeText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFF6B35),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Bullet points
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                bullets.forEach { bullet ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(iconTint, CircleShape)
                        )
                        Text(
                            bullet,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            // CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (badgeText == null) iconTint
                        else Color.White.copy(alpha = 0.08f)
                    )
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (badgeText == null) "Get Started →" else "Notify Me When Ready",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (badgeText == null) Color.White else Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}
