package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.GlassCard
import com.example.Text
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SavingsAdvisorScreen(viewModel: MainViewModel, onNavigateBack: () -> Unit) {
    val savingTasks by viewModel.allSavingTasksFlow.collectAsStateWithLifecycle()
    val aiAdvice by viewModel.aiAdvice.collectAsStateWithLifecycle()
    val isLoadingAdvice by viewModel.isLoadingAdvice.collectAsStateWithLifecycle()
    val profiles by viewModel.profilesFlow.collectAsStateWithLifecycle()

    val myProfile = profiles.find { it.id == "user" }
    val gfProfile = profiles.find { it.id == "girlfriend" }

    val myTargetGoal = myProfile?.monthlySavingGoal ?: 500.0
    val gfTargetGoal = gfProfile?.monthlySavingGoal ?: 500.0
    val jointSavingsGoal = myTargetGoal + gfTargetGoal

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = sdf.format(Date())

    // Calculations for checked savings tasks
    val completedSavingsReward = savingTasks.filter { it.isCompleted }.sumOf { it.rewardAmount }

    var newSavingsChallengeTitle by remember { mutableStateOf("") }
    var newSavingsChallengeAmount by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Savings & AI Coach", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Screen Intro
        Column {
            Text("Goal Savings Advisor", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("AI micro-insights and savings tasks to hit mutual targets", fontSize = 13.sp, color = SecondaryTextLavender)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Savings Targets Progress Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderBrush = Brush.linearGradient(colors = listOf(SweetheartedPeach.copy(alpha = 0.5f), ElectricLavender.copy(alpha = 0.1f)))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Mutual Savings Targets", fontSize = 12.sp, color = SecondaryTextLavender)
                    Text("$${jointSavingsGoal.toInt()} / month", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }

                Box(
                    modifier = Modifier
                        .background(Color(0x11FFFFFF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("AI Monitored", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ElectricLavender)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0x11FFFFFF))
            Spacer(modifier = Modifier.height(12.dp))

            // Savings Task Accumulations
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Saved on micro-tasks today", fontSize = 11.sp, color = SecondaryTextLavender)
                    Text("+$${String.format(Locale.getDefault(), "%.1f", completedSavingsReward)} saved!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CosmicCyan)
                }
                Icon(Icons.Filled.AutoAwesome, contentDescription = "Spark", tint = CosmicCyan)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Advisor Consultation Card
        Text("AI Savings Analyst Advisor", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderBrush = Brush.linearGradient(colors = listOf(ElectricLavender.copy(alpha = 0.5f), CosmicCyan.copy(alpha = 0.3f)))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Color(0x338B6CFF), CircleShape)
                        .padding(10.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = "AI Icon", tint = ElectricLavender, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("UsSpace AI Savings Coach", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Deep-scan financial ledger to formulate saving tactics", fontSize = 12.sp, color = SecondaryTextLavender)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Text Output
            if (aiAdvice.isBlank() && !isLoadingAdvice) {
                Text(
                    text = "Request your smart financial checkup! Clicking 'Consult AI Coach' below reviews your budgets, limits, and cash flow to design personalized advice.",
                    fontSize = 13.sp,
                    color = SecondaryTextLavender,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else if (isLoadingAdvice) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = ElectricLavender)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Duo Ledger analytical scan active... 🧠", fontSize = 12.sp, color = SecondaryTextLavender)
                }
            } else {
                // Display the advice safely in scroll text
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x0CFFFFFF), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = aiAdvice,
                        fontSize = 13.sp,
                        color = Color.White,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.fetchAISavingsAdvice() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLavender),
                shape = RoundedCornerShape(10.dp),
                enabled = !isLoadingAdvice
            ) {
                Text("Refining/Consult AI Coach ✨", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Saving Tasks/Challenges Board
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Interactive Saving Tasks Tracker", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Add micro challenge field inline
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newSavingsChallengeTitle,
                    onValueChange = { newSavingsChallengeTitle = it },
                    label = { Text("What custom task saves cash?") },
                    placeholder = { Text("e.g. Carpooled today") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicCyan,
                        unfocusedBorderColor = Color(0x1EFFFFFF),
                        focusedLabelColor = CosmicCyan,
                        unfocusedLabelColor = SecondaryTextLavender,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = newSavingsChallengeAmount,
                    onValueChange = { newSavingsChallengeAmount = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Save amount ($)") },
                    modifier = Modifier.width(110.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicCyan,
                        unfocusedBorderColor = Color(0x1EFFFFFF),
                        focusedLabelColor = CosmicCyan,
                        unfocusedLabelColor = SecondaryTextLavender,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val rewardAmt = newSavingsChallengeAmount.toDoubleOrNull()
                    if (newSavingsChallengeTitle.isNotBlank() && rewardAmt != null && rewardAmt > 0) {
                        viewModel.addSavingTask("shared", newSavingsChallengeTitle, rewardAmt, todayStr)
                        newSavingsChallengeTitle = ""
                        newSavingsChallengeAmount = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add Saving Micro-task", color = Color(0xFF070511))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Log of active savings actions
        if (savingTasks.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No micro challenges written. Complete targets together to accumulate mutual balances!",
                    fontSize = 11.sp,
                    color = SecondaryTextLavender,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            savingTasks.forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color(0x0AFFFFFF), RoundedCornerShape(12.dp))
                        .border(0.5.dp, Color(0x13FFFFFF), RoundedCornerShape(12.dp))
                        .clickable { viewModel.toggleSavingTaskCompletion(task) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.AddCircleOutline,
                            contentDescription = "Selection check",
                            tint = if (task.isCompleted) CosmicCyan else SecondaryTextLavender,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = task.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (task.isCompleted) SecondaryTextLavender else Color.White
                            )
                            Text("Saves estimated $${task.rewardAmount}", fontSize = 11.sp, color = CosmicCyan)
                        }
                    }

                    IconButton(
                        onClick = { viewModel.deleteSavingTask(task.id) }
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove challenge", tint = Color(0x44FF3F3F), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
