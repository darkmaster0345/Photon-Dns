package com.dnsspeedchecker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnsspeedchecker.data.preferences.Strategy
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategyQuizScreen(
    onStrategyRecommended: (Strategy) -> Unit,
    onNavigateBack: () -> Unit
) {
    var currentQuestion by remember { mutableStateOf(0) }
    var answers by remember { mutableStateOf(mutableMapOf<Int, Int>()) }
    var showResult by remember { mutableStateOf(false) }
    var recommendedStrategy by remember { mutableStateOf(Strategy.BALANCED) }
    
    val scrollState = rememberScrollState()
    
    // Quiz questions and options
    val questions = listOf(
        QuizQuestion(
            id = 1,
            question = "How do you primarily use your phone?",
            icon = Icons.Default.PhoneAndroid,
            options = listOf(
                QuizOption(1, "Mostly browsing/social media", Icons.Default.Public),
                QuizOption(2, "Video calls/streaming", Icons.Default.VideoCall),
                QuizOption(3, "Gaming/low latency apps", Icons.Default.Sports)
            )
        ),
        QuizQuestion(
            id = 2,
            question = "What matters more to you?",
            icon = Icons.Default.Tune,
            options = listOf(
                QuizOption(1, "Stability (fewer interruptions)", Icons.Default.Security),
                QuizOption(2, "Speed (always fastest)", Icons.Default.Speed),
                QuizOption(3, "Balance of both", Icons.Default.Balance)
            )
        ),
        QuizQuestion(
            id = 3,
            question = "How's your battery life?",
            icon = Icons.Default.BatteryChargingFull,
            options = listOf(
                QuizOption(1, "Always plugged in", Icons.Default.Power),
                QuizOption(2, "Need to conserve battery", Icons.Default.Eco),
                QuizOption(3, "Battery is fine", Icons.Default.BatteryFull)
            )
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Help Me Choose",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { /* TODO: Show help */ }) {
                Icon(Icons.Default.Help, contentDescription = "Help")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Quiz content
        if (!showResult) {
            questions.forEachIndexed { index, question ->
                if (index <= currentQuestion) {
                    QuestionCard(
                        question = question,
                        selectedAnswer = answers[question.id],
                        onAnswerSelected = { answerId ->
                            answers[question.id] = answerId
                        }
                        progress = (index + 1).toFloat() / questions.size
                    )
                } else {
                    // Future questions (dimmed)
                    QuestionCard(
                        question = question,
                        selectedAnswer = answers[question.id],
                        onAnswerSelected = { },
                        progress = (index + 1).toFloat() / questions.size,
                        isDimmed = true
                    )
                }
                
                if (index < questions.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentQuestion > 0) {
                    OutlinedButton(
                        onClick = {
                            currentQuestion = (currentQuestion - 1).coerceAtLeast(0)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                if (currentQuestion < questions.lastIndex) {
                    Button(
                        onClick = {
                            currentQuestion = (currentQuestion + 1).coerceAtMost(questions.lastIndex)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Next")
                    }
                } else {
                    Button(
                        onClick = {
                            recommendedStrategy = calculateRecommendedStrategy(answers)
                            showResult = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Get Recommendation")
                    }
                }
            }
        } else {
            // Results screen
            RecommendationResult(
                recommendedStrategy = recommendedStrategy,
                answers = answers,
                questions = questions,
                onRetakeQuiz = {
                    currentQuestion = 0
                    answers.clear()
                    showResult = false
                },
                onApplyStrategy = {
                    onStrategyRecommended(recommendedStrategy)
                },
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
private fun QuestionCard(
    question: QuizQuestion,
    selectedAnswer: Int?,
    onAnswerSelected: (Int) -> Unit,
    progress: Float,
    isDimmed: Boolean = false
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isDimmed) 0.6f else 1f,
        animationSpec = tween(300),
        label = "alpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(animatedAlpha),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDimmed) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Question with icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = question.icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (isDimmed) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isDimmed) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Options
            question.options.forEach { option ->
                OptionButton(
                    option = option,
                    isSelected = selectedAnswer == option.id,
                    onClick = { if (!isDimmed) onAnswerSelected(option.id) },
                    isDimmed = isDimmed
                )
                
                if (option != question.options.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun OptionButton(
    option: QuizOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDimmed: Boolean
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isDimmed) 0.6f else 1f,
        animationSpec = tween(200),
        label = "alpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!isDimmed) onClick() }
            .alpha(animatedAlpha),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                isDimmed -> MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = if (isDimmed) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDimmed) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                option.icon?.let { icon ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isDimmed) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationResult(
    recommendedStrategy: Strategy,
    answers: Map<Int, Int>,
    questions: List<QuizQuestion>,
    onRetakeQuiz: () -> Unit,
    onApplyStrategy: (Strategy) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Recommendation card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Recommended Strategy",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = recommendedStrategy.getDisplayName(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = recommendedStrategy.getDescription(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Reasoning
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Why this strategy is perfect for you:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                getReasoningText(answers, recommendedStrategy).forEach { reason ->
                    Text(
                        text = "• $reason",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRetakeQuiz,
                modifier = Modifier.weight(1f)
            ) {
                Text("Retake Quiz")
            }
            
            Button(
                onClick = onApplyStrategy,
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply Strategy")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Your answers summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Your Answers:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                questions.forEach { question ->
                    val selectedOption = question.options.find { it.id == answers[question.id] }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = question.question,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = selectedOption?.text ?: "Not answered",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Back button
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Settings")
        }
    }
}

private fun getReasoningText(answers: Map<Int, Int>, strategy: Strategy): List<String> {
    val usageType = answers[1] // Q1: Usage type
    val priority = answers[2] // Q2: Priority
    val battery = answers[3] // Q3: Battery
    
    return when (strategy) {
        Strategy.CONSERVATIVE -> listOf(
            "You prioritize stability - Conservative strategy minimizes interruptions",
            "Longer stability period (3 minutes) ensures consistent performance",
            "Higher threshold (30ms) means fewer unnecessary switches",
            "Perfect for business users or when network conditions are stable"
        )
        
        Strategy.BALANCED -> listOf(
            "You want a balance of speed and reliability - Balanced strategy delivers both",
            "Moderate check frequency (5 seconds) keeps performance optimal",
            "Standard threshold (20ms) catches meaningful improvements",
            "2-minute stability period prevents rapid switching while staying responsive"
        )
        
        Strategy.AGGRESSIVE -> listOf(
            "You prioritize speed above all else - Aggressive strategy maximizes performance",
            "Fast consecutive checks (2) ensure immediate switching to faster DNS",
            "Low threshold (15ms) captures even small performance gains",
            "Short stability period (1 minute) allows rapid optimization"
        )
        
        Strategy.CUSTOM -> listOf(
            "You want fine-tuned control - Custom strategy adapts to your needs",
            "Your specific preferences are applied for personalized experience",
            "All parameters are adjustable to match your exact requirements",
            "Perfect for advanced users who know exactly what they want"
        )
        
        else -> emptyList()
    }
}

data class QuizQuestion(
    val id: Int,
    val question: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val options: List<QuizOption>
)

data class QuizOption(
    val id: Int,
    val text: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null
)
