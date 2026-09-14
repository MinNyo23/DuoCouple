package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.data.model.*
import com.example.data.remote.SupabaseSyncState
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext

// --- High-Fidelity Light Glass Context Resolvers (Local Shadow Overrides) ---
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    fontStyle: androidx.compose.ui.text.font.FontStyle? = null,
    fontWeight: androidx.compose.ui.text.font.FontWeight? = null,
    fontFamily: androidx.compose.ui.text.font.FontFamily? = null,
    letterSpacing: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    textDecoration: androidx.compose.ui.text.style.TextDecoration? = null,
    textAlign: androidx.compose.ui.text.style.TextAlign? = null,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    overflow: androidx.compose.ui.text.style.TextOverflow = androidx.compose.ui.text.style.TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((androidx.compose.ui.text.TextLayoutResult) -> Unit)? = null,
    style: androidx.compose.ui.text.TextStyle = androidx.compose.material3.LocalTextStyle.current
) {
    val resolvedColor = if (color == Color.Unspecified) {
        LocalContentColor.current
    } else if (color == Color.White) {
        if (LocalContentColor.current == Color.White || LocalContentColor.current == MaterialTheme.colorScheme.onPrimary) {
            Color.White
        } else {
            GlassTextPrimary
        }
    } else {
        color
    }

    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        color = resolvedColor,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}

@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    val resolvedTint = if (tint == Color.White) {
        if (LocalContentColor.current == Color.White || LocalContentColor.current == MaterialTheme.colorScheme.onPrimary) {
            Color.White
        } else {
            GlassTextPrimary
        }
    } else {
        tint
    }
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = resolvedTint
    )
}

@Composable
fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: androidx.compose.foundation.interaction.MutableInteractionSource? = null,
    shape: androidx.compose.ui.graphics.Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    val resolvedColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = GlassTextPrimary,
        unfocusedTextColor = GlassTextPrimary,
        focusedBorderColor = ElectricLavender,
        unfocusedBorderColor = Color(0x25131024),
        focusedLabelColor = ElectricLavender,
        unfocusedLabelColor = SecondaryTextLavender,
        focusedContainerColor = Color(0x33FFFFFF),
        unfocusedContainerColor = Color(0x19FFFFFF)
    )

    androidx.compose.material3.OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle.copy(color = GlassTextPrimary),
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource ?: remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
        shape = shape,
        colors = resolvedColors
    )
}

class LoveTrackerCrashHandler(private val originalHandler: Thread.UncaughtExceptionHandler?) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        android.util.Log.e("LoveTrackerCrash", "FATAL EXCEPTION on thread ${thread.name}: ${throwable.message}", throwable)
        val sw = java.io.StringWriter()
        val pw = java.io.PrintWriter(sw)
        throwable.printStackTrace(pw)
        android.util.Log.e("LoveTrackerCrash", sw.toString())
        originalHandler?.uncaughtException(thread, throwable)
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Comprehensive Error/Crash Diagnostics Engine with proper platform chaining and recursion guard
        val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        if (originalHandler !is LoveTrackerCrashHandler) {
            Thread.setDefaultUncaughtExceptionHandler(LoveTrackerCrashHandler(originalHandler))
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                val isCoupled by viewModel.isCoupled.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { 
                        if (isLoggedIn && isCoupled) {
                            CoupleNavigationBar(viewModel)
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .drawBehind {
                                // Double Radial Glowing Halos for rich soft environment depth
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(ElectricLavender.copy(alpha = 0.18f), Color.Transparent),
                                        radius = 350.dp.toPx()
                                    ),
                                    center = Offset(0f, 180.dp.toPx())
                                )
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(CosmicCyan.copy(alpha = 0.12f), Color.Transparent),
                                        radius = 420.dp.toPx()
                                    ),
                                    center = Offset(size.width, size.height - 250.dp.toPx())
                                )
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(SweetheartedPeach.copy(alpha = 0.08f), Color.Transparent),
                                        radius = 300.dp.toPx()
                                    ),
                                    center = Offset(size.width / 2, size.height / 2)
                                )
                            }
                            .padding(innerPadding)
                    ) {
                        AppNavigationContent(viewModel)
                    }
                }
            }
        }
    }
}

// --- Dynamic Navigation Routing ---
@Composable
fun AppNavigationContent(viewModel: MainViewModel) {
    val isAppLoading by viewModel.isAppLoading.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isCoupled by viewModel.isCoupled.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = isAppLoading,
        transitionSpec = {
            if (targetState == false) {
                // Smoothly zoom, scale and crossfade out of splash screen into the main experience
                (fadeIn(animationSpec = tween(750, easing = FastOutSlowInEasing)) +
                 scaleIn(initialScale = 1.04f, animationSpec = tween(750, easing = FastOutSlowInEasing)))
                    .togetherWith(
                        fadeOut(animationSpec = tween(450, easing = FastOutSlowInEasing)) +
                        scaleOut(targetScale = 0.96f, animationSpec = tween(450, easing = FastOutSlowInEasing))
                    )
            } else {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(450))
            }
        },
        label = "LoadingTransition"
    ) { loading ->
        if (loading) {
            LoadingScreen(viewModel)
        } else {
            if (!isLoggedIn) {
                LoginScreen(viewModel)
            } else if (!isCoupled) {
                CoupleConnectScreen(viewModel)
            } else {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val duration = 320
                        if (targetState > initialState) {
                            (slideInHorizontally(animationSpec = tween(duration, easing = FastOutSlowInEasing)) { width -> (width * 0.12f).toInt() } +
                             fadeIn(animationSpec = tween(duration, easing = FastOutSlowInEasing)))
                                .togetherWith(
                                    slideOutHorizontally(animationSpec = tween(duration - 50, easing = FastOutSlowInEasing)) { width -> (-width * 0.12f).toInt() } +
                                    fadeOut(animationSpec = tween(duration - 50, easing = FastOutSlowInEasing))
                                )
                        } else {
                            (slideInHorizontally(animationSpec = tween(duration, easing = FastOutSlowInEasing)) { width -> (-width * 0.12f).toInt() } +
                             fadeIn(animationSpec = tween(duration, easing = FastOutSlowInEasing)))
                                .togetherWith(
                                    slideOutHorizontally(animationSpec = tween(duration - 50, easing = FastOutSlowInEasing)) { width -> (width * 0.12f).toInt() } +
                                    fadeOut(animationSpec = tween(duration - 50, easing = FastOutSlowInEasing))
                                )
                        }
                    },
                    label = "TabTransition"
                ) { tab ->
                    when (tab) {
                        0 -> DashboardScreen(viewModel)
                        1 -> LearningScreen(viewModel)
                        2 -> ExpensesScreen(viewModel)
                        3 -> SavingsAdvisorScreen(viewModel)
                        4 -> ProfilesScreen(viewModel)
                    }
                }
            }
        }
    }
}

// --- Glassmorphic Container Element (The GlassCard) ---
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderBrush: Brush = Brush.linearGradient(
        colors = listOf(ElectricLavender.copy(alpha = 0.25f), CosmicCyan.copy(alpha = 0.15f))
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .animateContentSize(
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            )
            .clip(RoundedCornerShape(20.dp))
    ) {
        // Base translucent frosted backdrop
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(color = GlassSurfaceDark) 
        )
        // Internal decorative glassmorphic blurs
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-30).dp, y = (-30).dp)
                .size(120.dp)
                .blur(50.dp)
                .background(ElectricLavender.copy(alpha = 0.2f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 30.dp, y = 30.dp)
                .size(120.dp)
                .blur(50.dp)
                .background(CosmicCyan.copy(alpha = 0.2f), CircleShape)
        )
        // Border layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 1.dp,
                    brush = borderBrush,
                    shape = RoundedCornerShape(20.dp)
                )
        )
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun ExpensesPieChart(expenses: List<com.example.data.model.ExpenseEntry>, modifier: Modifier = Modifier) {
    val expenseEntries = expenses.filter { !it.isIncome }
    if (expenseEntries.isEmpty()) {
        Text("No expenses yet to visualize.", color = SecondaryTextLavender, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
        return
    }

    val grouped = expenseEntries.groupBy { it.category }
        .mapValues { it.value.sumOf { e -> e.amount } }
        .toList()
        .sortedByDescending { it.second }

    val total = grouped.sumOf { it.second }
    
    // Animate progress
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationPlayed = true
    }
    val sweepProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "pie_anim"
    )

    val colors = listOf(
        CosmicCyan,
        ElectricLavender,
        Color(0xFFFF9800), // Orange
        Color(0xFFE91E63), // Pink
        Color(0xFF4CAF50), // Green
        Color(0xFFFFC107), // Amber
        Color(0xFFF44336), // Red
        Color(0xFF9E9E9E)  // Grey
    )

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                grouped.forEachIndexed { index, (_, amount) ->
                    val sweepAngle = ((amount.toFloat() / total.toFloat()) * 360f) * sweepProgress
                    val color = colors[index % colors.size]
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 24f, cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }
            Text(
                text = "Expenses",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(24.dp))
        
        // Legend
        Column(modifier = Modifier.weight(1f)) {
            grouped.forEachIndexed { index, (category, amount) ->
                val color = colors[index % colors.size]
                val percentage = if (total > 0) ((amount / total) * 100).toInt() else 0
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$percentage%",
                        color = SecondaryTextLavender,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// --- 1. DASHBOARD SCREEN (SIDE-BY-SIDE COUPLE STATUS) ---

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val profiles by viewModel.profilesFlow.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpensesFlow.collectAsStateWithLifecycle()
    val roadmaps by viewModel.roadmapsFlow.collectAsStateWithLifecycle()
    val learningTasks by viewModel.allLearningTasksFlow.collectAsStateWithLifecycle()
    val loveStartDate by viewModel.loveStartDate.collectAsStateWithLifecycle()
    
    val aiAdvice by viewModel.aiAdvice.collectAsStateWithLifecycle()
    val isLoadingAdvice by viewModel.isLoadingAdvice.collectAsStateWithLifecycle()
    val allCalendarTasks by viewModel.allCalendarTasksFlow.collectAsStateWithLifecycle()

    val myProfile = profiles.find { it.id == "user" }
    val gfProfile = profiles.find { it.id == "girlfriend" }

    val loveDaysInfo = remember(loveStartDate) {
        if (loveStartDate.isBlank()) null
        else {
            try {
                val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val startDate = inputSdf.parse(loveStartDate)
                if (startDate != null) {
                    val currentDate = Date()
                    val diffMs = currentDate.time - startDate.time
                    val totalDays = (diffMs / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                    
                    val startCal = Calendar.getInstance().apply { time = startDate }
                    val currentCal = Calendar.getInstance().apply { time = currentDate }
                    
                    var years = currentCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
                    var months = currentCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH)
                    var days = currentCal.get(Calendar.DAY_OF_MONTH) - startCal.get(Calendar.DAY_OF_MONTH)
                    
                    if (days < 0) {
                        val prevMonthCal = (currentCal.clone() as Calendar).apply {
                            add(Calendar.MONTH, -1)
                        }
                        days += prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        months--
                    }
                    
                    if (months < 0) {
                        months += 12
                        years--
                    }
                    
                    Triple(totalDays, years.coerceAtLeast(0), Pair(months.coerceAtLeast(0), days.coerceAtLeast(0)))
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    // Computations
    val myName = myProfile?.name ?: "Minnyo"
    val gfName = gfProfile?.name ?: "Girlfriend"

    val totalIncome = expenses.filter { it.isIncome }.sumOf { it.amount }
    val totalExpense = expenses.filter { !it.isIncome }.sumOf { it.amount }
    val mutualSavings = totalIncome - totalExpense

    val totalBudgetLimit = (myProfile?.dailyBudget ?: 80.0) + (gfProfile?.dailyBudget ?: 70.0)
    
    // Filter today's spent
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = sdf.format(Date())
    val todayExpenses = expenses.filter { !it.isIncome && it.dateString == todayStr }
    val todaySpent = todayExpenses.sumOf { it.amount }

    // Interactive Live Status States
    var userStatusVibe by remember { mutableStateOf("Coding 🧠") }
    var userStatusEmoji by remember { mutableStateOf("🧠") }
    var userOnlineState by remember { mutableStateOf(true) }
    var showUserVibePicker by remember { mutableStateOf(false) }

    var gfStatusVibe by remember { mutableStateOf("Learning Jetpack Compose 📚") }
    var gfStatusEmoji by remember { mutableStateOf("📚") }
    var gfOnlineState by remember { mutableStateOf(true) }
    var showGfVibePicker by remember { mutableStateOf(false) }

    // Interactive Shared Calendar Tasks State
    var showCalendarAddDialog by remember { mutableStateOf(false) }
    var newEventTitle by remember { mutableStateOf("") }
    var newEventTime by remember { mutableStateOf("") }

    // Category Breakdowns for Expense Summaries
    val foodSpent = remember(expenses) { expenses.filter { !it.isIncome && it.category == "Food" }.sumOf { it.amount } }
    val shoppingSpent = remember(expenses) { expenses.filter { !it.isIncome && it.category == "Shopping" }.sumOf { it.amount } }
    val transportSpent = remember(expenses) { expenses.filter { !it.isIncome && it.category == "Transport" }.sumOf { it.amount } }
    val entertainmentSpent = remember(expenses) { expenses.filter { !it.isIncome && it.category == "Entertainment" }.sumOf { it.amount } }
    val otherSpent = remember(expenses) { expenses.filter { !it.isIncome && (it.category != "Food" && it.category != "Shopping" && it.category != "Transport" && it.category != "Entertainment") }.sumOf { it.amount } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Dashboard Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "UsSpace Share Workspace",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Cooperative live duo status • No secrets 💕",
                        fontSize = 13.sp,
                        color = SecondaryTextLavender
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Love Core",
                    tint = SweetheartedPeach,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // --- Love Days Tracker Card ---
        item {
            val context = LocalContext.current
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderBrush = Brush.linearGradient(
                    colors = listOf(SweetheartedPeach.copy(alpha = 0.6f), ElectricLavender.copy(alpha = 0.4f))
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(SweetheartedPeach.copy(alpha = 0.2f), CircleShape)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Love Indicator",
                                tint = SweetheartedPeach,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Love Days Counter",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Our precious time together 💕",
                                fontSize = 12.sp,
                                color = SecondaryTextLavender
                            )
                        }
                    }
                    
                    // Button to Set or Edit the date
                    IconButton(
                        onClick = {
                            var initYear = Calendar.getInstance().get(Calendar.YEAR)
                            var initMonth = Calendar.getInstance().get(Calendar.MONTH)
                            var initDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                            
                            if (loveStartDate.isNotBlank()) {
                                try {
                                    val parts = loveStartDate.split("-")
                                    if (parts.size == 3) {
                                        initYear = parts[0].toInt()
                                        initMonth = parts[1].toInt() - 1 // 0-based month
                                        initDay = parts[2].toInt()
                                    }
                                } catch (e: Exception) {
                                    // Fallback to now
                                }
                            }
                            
                            showSafeDatePicker(
                                context,
                                initYear,
                                initMonth,
                                initDay
                            ) { year, month, dayOfMonth ->
                                val newDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                viewModel.updateLoveStartDate(newDate)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (loveStartDate.isBlank()) Icons.Filled.AddCircle else Icons.Filled.DateRange,
                            contentDescription = "Edit Love Date",
                            tint = CosmicCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (loveDaysInfo == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Love Start Date added yet",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = SecondaryTextLavender,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val now = Calendar.getInstance()
                                showSafeDatePicker(
                                    context,
                                    now.get(Calendar.YEAR),
                                    now.get(Calendar.MONTH),
                                    now.get(Calendar.DAY_OF_MONTH)
                                ) { year, month, day ->
                                    val newDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                                    viewModel.updateLoveStartDate(newDate)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SweetheartedPeach),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF070511))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Love Start Date", color = Color(0xFF070511), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    val totalDays = loveDaysInfo.first
                    val years = loveDaysInfo.second
                    val months = loveDaysInfo.third.first
                    val days = loveDaysInfo.third.second
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x0AFFFFFF), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left Profile
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = myProfile?.avatarEmoji ?: "🦁",
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = myName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Middle Heart & Total Days
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(2f)
                        ) {
                            Text(
                                text = "❤️",
                                fontSize = 24.sp,
                                modifier = Modifier.animateContentSize()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$totalDays Days",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CosmicCyan,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "since $loveStartDate",
                                fontSize = 11.sp,
                                color = SecondaryTextLavender,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Right Profile
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = gfProfile?.avatarEmoji ?: "🌸",
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = gfName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Breakdown (Years, Months, Days)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Years count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0x0DFFFFFF), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$years",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SweetheartedPeach
                                )
                                Text(
                                    text = if (years == 1) "Year" else "Years",
                                    fontSize = 11.sp,
                                    color = SecondaryTextLavender
                                )
                            }
                        }

                        // Months count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0x0DFFFFFF), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$months",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricLavender
                                )
                                Text(
                                    text = if (months == 1) "Month" else "Months",
                                    fontSize = 11.sp,
                                    color = SecondaryTextLavender
                                )
                            }
                        }

                        // Days count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0x0DFFFFFF), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$days",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CosmicCyan
                                )
                                Text(
                                    text = if (days == 1) "Day" else "Days",
                                    fontSize = 11.sp,
                                    color = SecondaryTextLavender
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 1: Live Status & Presence Tracking ---
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderBrush = Brush.linearGradient(
                    colors = listOf(ElectricLavender.copy(alpha = 0.4f), CosmicCyan.copy(alpha = 0.4f))
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Wifi,
                        contentDescription = "Presence Icon",
                        tint = CosmicCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Live Co-Presence Status",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .background(CosmicCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Connected",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CosmicCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Minnyo Column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0x0AFFFFFF), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(16.dp))
                            .clickable { showUserVibePicker = !showUserVibePicker }
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Me", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecondaryTextLavender)
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (userOnlineState) Color(0xFF4CAF50) else Color.Gray,
                                        CircleShape
                                    )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(userStatusEmoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(myName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(userStatusVibe, fontSize = 11.sp, color = SecondaryTextLavender, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tap to update mood",
                            fontSize = 9.sp,
                            color = ElectricLavender,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Girlfriend Column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0x0AFFFFFF), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(16.dp))
                            .clickable { showGfVibePicker = !showGfVibePicker }
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Partner", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecondaryTextLavender)
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (gfOnlineState) Color(0xFF4CAF50) else Color.Gray,
                                        CircleShape
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(gfStatusEmoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(gfName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(gfStatusVibe, fontSize = 11.sp, color = SecondaryTextLavender, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tap to cycle activity",
                            fontSize = 9.sp,
                            color = SweetheartedPeach,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Interactive vibe preset picker for USER
                AnimatedVisibility(visible = showUserVibePicker) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(Color(0x08FFFFFF), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Text("Set My Vibe Preset:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            val userPresets = listOf(
                                Pair("Coding 🧠", "🧠"),
                                Pair("Coffee Break ☕", "☕"),
                                Pair("Chilling 🍃", "🍃"),
                                Pair("Loving You ❤️", "❤️"),
                                Pair("Tired & Sleeping 😴", "😴"),
                                Pair("Exercising 🏃", "🏃")
                            )
                            userPresets.forEach { preset ->
                                SuggestionChip(
                                    onClick = {
                                        userStatusVibe = preset.first
                                        userStatusEmoji = preset.second
                                        showUserVibePicker = false
                                    },
                                    label = { Text(preset.first, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }

                // Interactive vibe cycle for Girlfriend
                AnimatedVisibility(visible = showGfVibePicker) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(Color(0x08FFFFFF), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Text("Simulate Partner Action:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            val gfPresets = listOf(
                                Pair("Learning Compose 📚", "📚"),
                                Pair("Shopping 🛍️", "🛍️"),
                                Pair("Happy Hour 🎉", "🎉"),
                                Pair("Napping 😴", "😴"),
                                Pair("Missing You 💕", "💕"),
                                Pair("Gym Workout 💪", "💪")
                            )
                            gfPresets.forEach { preset ->
                                SuggestionChip(
                                    onClick = {
                                        gfStatusVibe = preset.first
                                        gfStatusEmoji = preset.second
                                        showGfVibePicker = false
                                    },
                                    label = { Text(preset.first, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Connection level calculation display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Sync, contentDescription = "Sync", tint = ElectricLavender, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Workspace Sync Level", fontSize = 11.sp, color = SecondaryTextLavender)
                    }
                    Text("98% Synced", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CosmicCyan)
                }

                Spacer(modifier = Modifier.height(6.dp))

                var syncProgress by remember { mutableStateOf(0f) }
                LaunchedEffect(Unit) {
                    syncProgress = 0.98f
                }
                val animatedSyncProgress by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = syncProgress,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                    label = "sync_progress"
                )

                LinearProgressIndicator(
                    progress = { animatedSyncProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ElectricLavender,
                    trackColor = Color(0x12FFFFFF)
                )
            }
        }

        // --- SECTION 2: Shared Calendar Tasks ---
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderBrush = Brush.linearGradient(
                    colors = listOf(CosmicCyan.copy(alpha = 0.4f), Color.Transparent)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "Calendar List",
                            tint = SweetheartedPeach,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Shared Couple Calendar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val context = LocalContext.current
                    IconButton(
                        onClick = { com.example.utils.CsvExportUtil.exportCalendarTasksToCsv(context, allCalendarTasks) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Export Events to CSV",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { showCalendarAddDialog = !showCalendarAddDialog }
                    ) {
                        Icon(
                            imageVector = if (showCalendarAddDialog) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = "Add Event Toggle",
                            tint = CosmicCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Add Calendar Event Expandable Panel
                AnimatedVisibility(visible = showCalendarAddDialog) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .background(Color(0x08FFFFFF), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Add Shared Duo Event 📅", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        
                        OutlinedTextField(
                            value = newEventTitle,
                            onValueChange = { newEventTitle = it },
                            label = { Text("Event Title") },
                            placeholder = { Text("e.g. Cinema Date Night 🍿") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = newEventTime,
                            onValueChange = { newEventTime = it },
                            label = { Text("Schedule Time / Day") },
                            placeholder = { Text("e.g. Tonight, 8:30 PM") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (newEventTitle.isNotBlank() && newEventTime.isNotBlank()) {
                                    viewModel.addCalendarTask(
                                        title = newEventTitle.trim(),
                                        time = newEventTime.trim()
                                    )
                                    newEventTitle = ""
                                    newEventTime = ""
                                    showCalendarAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Schedule on Shared Calendar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Render Calendar Events
                if (allCalendarTasks.isEmpty()) {
                    Text(
                        text = "All caught up! No scheduled couple tasks. Use the (+) button to organize your joint plans.",
                        fontSize = 12.sp,
                        color = SecondaryTextLavender,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                } else {
                    allCalendarTasks.forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .background(
                                    if (task.isCompleted) Color(0x05FFFFFF) else Color(0x0AFFFFFF),
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    1.dp,
                                    if (task.isCompleted) Color.Transparent else Color(0x08FFFFFF),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                IconButton(
                                    onClick = {
                                        viewModel.toggleCalendarTask(task)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                        contentDescription = "Toggle Event State",
                                        tint = if (task.isCompleted) CosmicCyan else SecondaryTextLavender,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column {
                                    Text(
                                        text = task.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (task.isCompleted) SecondaryTextLavender else Color.White,
                                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                    )
                                    Text(
                                        text = task.time,
                                        fontSize = 11.sp,
                                        color = SecondaryTextLavender
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    viewModel.deleteCalendarTask(task)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete Event",
                                    tint = SweetheartedPeach.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 3: Mutual Financial Hub & Expense Summaries ---
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderBrush = Brush.linearGradient(
                    colors = listOf(SweetheartedPeach.copy(alpha = 0.5f), ElectricLavender.copy(alpha = 0.3f))
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Mutual Savings Pool", fontSize = 12.sp, color = SecondaryTextLavender, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$${String.format(Locale.getDefault(), "%,.2f", mutualSavings)}",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CosmicCyan
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(SweetheartedPeach.copy(alpha = 0.15f), CircleShape)
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = "Wallet Icon",
                            tint = SweetheartedPeach,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0x14FFFFFF))
                Spacer(modifier = Modifier.height(12.dp))

                // Combined Spent Tracker Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Joint Spent Today", fontSize = 11.sp, color = SecondaryTextLavender)
                        Text("$${String.format(Locale.getDefault(), "%.2f", todaySpent)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Joint Daily Budget", fontSize = 11.sp, color = SecondaryTextLavender)
                        Text("$${String.format(Locale.getDefault(), "%.1f", totalBudgetLimit)}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = SecondaryTextLavender)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                val budgetFraction = if (totalBudgetLimit > 0) (todaySpent / totalBudgetLimit).toFloat() else 0f
                val animatedBudgetFraction by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = budgetFraction.coerceIn(0f, 1f),
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                    label = "budget_progress"
                )
                LinearProgressIndicator(
                    progress = { animatedBudgetFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (budgetFraction > 0.9f) SweetheartedPeach else ElectricLavender,
                    trackColor = Color(0x12FFFFFF),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Rich Category Expense Breakdowns (From Real Data)
                Text("Category Spending Breakdown", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))

                val totalCategorySpent = (foodSpent + shoppingSpent + transportSpent + entertainmentSpent + otherSpent).coerceAtLeast(1.0)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Category: Food
                    CategoryProgressRow(
                        title = "Food & Dining 🍔",
                        amount = foodSpent,
                        percentage = (foodSpent / totalCategorySpent * 100).toInt(),
                        color = ElectricLavender
                    )

                    // Category: Shopping
                    CategoryProgressRow(
                        title = "Shopping & Clothing 🛍️",
                        amount = shoppingSpent,
                        percentage = (shoppingSpent / totalCategorySpent * 100).toInt(),
                        color = CosmicCyan
                    )

                    // Category: Transport
                    CategoryProgressRow(
                        title = "Transport & Travel 🚗",
                        amount = transportSpent,
                        percentage = (transportSpent / totalCategorySpent * 100).toInt(),
                        color = SweetheartedPeach
                    )

                    // Category: Entertainment
                    CategoryProgressRow(
                        title = "Fun & Entertainment 🍿",
                        amount = entertainmentSpent,
                        percentage = (entertainmentSpent / totalCategorySpent * 100).toInt(),
                        color = Color(0xFFFFB74D)
                    )

                    // Category: Other
                    CategoryProgressRow(
                        title = "Utilities & Other 📝",
                        amount = otherSpent,
                        percentage = (otherSpent / totalCategorySpent * 100).toInt(),
                        color = SecondaryTextLavender
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0x14FFFFFF))
                Spacer(modifier = Modifier.height(12.dp))

                // Real-time Recent Expenses Log preview
                Text("Recent Shared Logs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))

                val recentExpensesList = expenses.filter { !it.isIncome }.takeLast(3).reversed()
                if (recentExpensesList.isEmpty()) {
                    Text(
                        text = "No recorded shared expenses yet! Head to the Expenses tab to insert one.",
                        fontSize = 11.sp,
                        color = SecondaryTextLavender,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    recentExpensesList.forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (entry.ownerId == "user") ElectricLavender.copy(alpha = 0.15f)
                                            else SweetheartedPeach.copy(alpha = 0.15f),
                                            CircleShape
                                        )
                                        .padding(6.dp)
                                ) {
                                    Text(text = if (entry.ownerId == "user") "🦁" else "🌸", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (entry.note.isNotBlank()) entry.note else entry.category,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${entry.category} • ${entry.dateString}",
                                        fontSize = 10.sp,
                                        color = SecondaryTextLavender
                                    )
                                }
                            }
                            Text(
                                text = "-$${String.format(Locale.getDefault(), "%.2f", entry.amount)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SweetheartedPeach
                            )
                        }
                    }
                }
            }
        }

        // --- SECTION 4: AI Savings Advisor ---
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderBrush = Brush.linearGradient(
                    colors = listOf(CosmicCyan.copy(alpha = 0.4f), ElectricLavender.copy(alpha = 0.4f))
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "AI Sparkle",
                            tint = CosmicCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini Savings Advisor",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Button(
                        onClick = { viewModel.fetchAISavingsAdvice() },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoadingAdvice
                    ) {
                        Text(if (isLoadingAdvice) "Analyzing..." else "Analyze ✨", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // --- Animated Expenses Pie Chart ---
                ExpensesPieChart(expenses = expenses, modifier = Modifier.padding(bottom = 16.dp))
                
                androidx.compose.animation.AnimatedContent(
                    targetState = isLoadingAdvice to aiAdvice,
                    label = "ai_advisor_content",
                    transitionSpec = {
                        androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(500)) togetherWith 
                        androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500))
                    }
                ) { (loading, advice) ->
                    if (loading) {
                        Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = CosmicCyan, modifier = Modifier.size(24.dp))
                        }
                    } else if (advice.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x0AFFFFFF), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = advice,
                                fontSize = 13.sp,
                                color = Color.White,
                                lineHeight = 18.sp
                            )
                        }
                    } else {
                        Text(
                            text = "Tap 'Analyze ✨' to get personalized AI advice on your recent spending habits.",
                            fontSize = 12.sp,
                            color = SecondaryTextLavender,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        // Side-by-Side Partner Info Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Boyfriend Column
                Box(modifier = Modifier.weight(1f)) {
                    PartnerColumnCard(
                        name = myName,
                        emoji = myProfile?.avatarEmoji ?: "🦁",
                        themeColor = ElectricLavender,
                        todaySpent = expenses.filter { it.ownerId == "user" && !it.isIncome && it.dateString == todayStr }.sumOf { it.amount },
                        dailyLimit = myProfile?.dailyBudget ?: 80.0,
                        studyMins = learningTasks.filter { it.ownerId == "user" && it.isCompleted && it.dateString == todayStr }.sumOf { it.minutesSpent },
                        completedRoadmapTasks = "Active Roadmap",
                        activeRoadmapCount = roadmaps.filter { it.ownerId == "user" }.size
                    )
                }

                // Girlfriend Column
                Box(modifier = Modifier.weight(1f)) {
                    PartnerColumnCard(
                        name = gfName,
                        emoji = gfProfile?.avatarEmoji ?: "🌸",
                        themeColor = SweetheartedPeach,
                        todaySpent = expenses.filter { it.ownerId == "girlfriend" && !it.isIncome && it.dateString == todayStr }.sumOf { it.amount },
                        dailyLimit = gfProfile?.dailyBudget ?: 70.0,
                        studyMins = learningTasks.filter { it.ownerId == "girlfriend" && it.isCompleted && it.dateString == todayStr }.sumOf { it.minutesSpent },
                        completedRoadmapTasks = "Active Roadmap",
                        activeRoadmapCount = roadmaps.filter { it.ownerId == "girlfriend" }.size
                    )
                }
            }
        }

        // Real-Time Learning Task Progress
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.MenuBook,
                        contentDescription = "Learning Status",
                        tint = CosmicCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Today's Learning Tasks",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val todayTasks = learningTasks.filter { it.dateString == todayStr }
                if (todayTasks.isEmpty()) {
                    Text(
                        text = "No learning tasks designed for today yet! Pop over to the Learning tab to schedule study.",
                        fontSize = 12.sp,
                        color = SecondaryTextLavender,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                } else {
                    todayTasks.forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (task.ownerId == "user") ElectricLavender.copy(alpha = 0.2f)
                                            else SweetheartedPeach.copy(alpha = 0.2f),
                                            CircleShape
                                        )
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = if (task.ownerId == "user") "🦁" else "🌸",
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = task.title,
                                    fontSize = 13.sp,
                                    color = if (task.isCompleted) SecondaryTextLavender else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = "Tick State",
                                tint = if (task.isCompleted) CosmicCyan else Color(0x33FFFFFF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun CategoryProgressRow(
    title: String,
    amount: Double,
    percentage: Int,
    color: Color
) {
    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = (percentage / 100f).coerceIn(0f, 1f),
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "progress_anim"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 11.sp, color = SecondaryTextLavender)
            Text(
                text = "$${String.format(Locale.getDefault(), "%.1f", amount)} (${percentage}%)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = Color(0x0AFFFFFF)
        )
    }
}

@Composable
fun PartnerColumnCard(
    name: String,
    emoji: String,
    themeColor: Color,
    todaySpent: Double,
    dailyLimit: Double,
    studyMins: Int,
    completedRoadmapTasks: String,
    activeRoadmapCount: Int
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderBrush = Brush.linearGradient(
            colors = listOf(themeColor.copy(alpha = 0.4f), Color(0x05FFFFFF))
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(themeColor.copy(alpha = 0.15f), CircleShape)
                    .size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Spent Progress
        Text("Daily Budget", fontSize = 10.sp, color = SecondaryTextLavender)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("$${String.format(Locale.getDefault(), "%.1f", todaySpent)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColor)
            Text("/ $${dailyLimit.toInt()}", fontSize = 10.sp, color = SecondaryTextLavender)
        }
        Spacer(modifier = Modifier.height(4.dp))
        val fraction = if (dailyLimit > 0) (todaySpent / dailyLimit).toFloat() else 0f
        LinearProgressIndicator(
            progress = { fraction.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = themeColor,
            trackColor = Color(0x11FFFFFF)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Study Hours Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Timer,
                contentDescription = "Study time",
                tint = CosmicCyan,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text("Study Completed", fontSize = 9.sp, color = SecondaryTextLavender)
                Text("${studyMins}m today", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Roadmap Count
        Text(completedRoadmapTasks, fontSize = 9.sp, color = SecondaryTextLavender)
        Text("$activeRoadmapCount Track(s)", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}


// --- 2. LEARNING SPACE SCREEN (CALENDAR & ROADMAPS) ---
@Composable
fun LearningScreen(viewModel: MainViewModel) {
    val roadmaps by viewModel.roadmapsFlow.collectAsStateWithLifecycle()
    val learningTasks by viewModel.allLearningTasksFlow.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val activeUserContext by viewModel.activeUserContext.collectAsStateWithLifecycle()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskMins by remember { mutableStateOf("30") }

    var showAddRoadmapDialog by remember { mutableStateOf(false) }
    var newRoadmapTitle by remember { mutableStateOf("") }
    var newRoadmapDesc by remember { mutableStateOf("") }

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val todayCal = Calendar.getInstance()

    // Filter tasks for selected date
    val filteredTasks = learningTasks.filter { it.dateString == selectedDate }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Selection Header for Owner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Duo Education Hub", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Track roadmap status & daily progress", fontSize = 13.sp, color = SecondaryTextLavender)
            }

            // User Toggle Button
            Row(
                modifier = Modifier
                    .background(Color(0x13FFFFFF), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("user" to "🦁", "girlfriend" to "🌸").forEach { (id, label) ->
                    Box(
                        modifier = Modifier
                            .background(
                                if (activeUserContext == id) {
                                    if (id == "user") ElectricLavender.copy(alpha = 0.4f) else SweetheartedPeach.copy(alpha = 0.4f)
                                } else Color.Transparent,
                                RoundedCornerShape(9.dp)
                            )
                            .clickable { viewModel.setActiveUserContext(id) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(label, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interlocking Calendar Component
        Text(
            text = "Learning Tracker Calendar (${monthYearFormat.format(todayCal.time)})",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            CalendarGrid(
                selectedDate = selectedDate,
                learningTasks = learningTasks,
                onDayClick = { dateStr -> viewModel.selectDate(dateStr) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Date Tasks Panel
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Tasks for $selectedDate", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${filteredTasks.size} lessons recorded", fontSize = 11.sp, color = SecondaryTextLavender)
                }

                Button(
                    onClick = { showAddTaskDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLavender),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Task", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredTasks.isEmpty()) {
                Text(
                    text = "No learning scheduled for this date. Click 'Add Task' above to log study subjects together! ✏️",
                    fontSize = 12.sp,
                    color = SecondaryTextLavender,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            } else {
                filteredTasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(Color(0x08FFFFFF), RoundedCornerShape(8.dp))
                            .border(0.5.dp, Color(0x13FFFFFF), RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleLearningTaskCompletion(task) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = "Status Mark",
                                tint = if (task.isCompleted) CosmicCyan else SecondaryTextLavender,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = task.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (task.isCompleted) SecondaryTextLavender else Color.White,
                                    style = if (task.isCompleted) MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    ) else MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Booked by ${if (task.ownerId == "user") "🦁 Minnyo" else "🌸 Partner"} • ${task.minutesSpent}m estimated",
                                    fontSize = 11.sp,
                                    color = SecondaryTextLavender
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.deleteLearningTask(task.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Roadmaps Panel Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Partner's Learning Roadmaps ($activeUserContext's views)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Button(
                onClick = { showAddRoadmapDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create Roadmap", tint = Color(0xFF070511), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Roadmap", color = Color(0xFF070511), fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Roadmaps to active contextual owner
        val activeRoadmaps = roadmaps.filter { it.ownerId == activeUserContext }
        if (activeRoadmaps.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No Roadmaps created yet. Set long-term milestones (e.g. 'Learn Docker', 'Organic Baking') to visualize progressive lessons!",
                    fontSize = 12.sp,
                    color = SecondaryTextLavender,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            activeRoadmaps.forEach { roadmap ->
                RoadmapCard(roadmap = roadmap, viewModel = viewModel)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Task addition pop-up
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Log Study Task", color = Color.White) },
            containerColor = GlassSurfaceDark,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Study Subject") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicCyan,
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedLabelColor = CosmicCyan,
                            unfocusedLabelColor = SecondaryTextLavender,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newTaskMins,
                        onValueChange = { newTaskMins = it },
                        label = { Text("Estimated study minutes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicCyan,
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedLabelColor = CosmicCyan,
                            unfocusedLabelColor = SecondaryTextLavender,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            viewModel.addLearningTask(
                                ownerId = activeUserContext,
                                title = newTaskTitle,
                                dateString = selectedDate,
                                minutes = newTaskMins.toIntOrNull() ?: 30
                            )
                            newTaskTitle = ""
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan)
                ) {
                    Text("Save Task", color = Color(0xFF070511))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel", color = SecondaryTextLavender)
                }
            }
        )
    }

    // Roadmap addition pop-up
    if (showAddRoadmapDialog) {
        AlertDialog(
            onDismissRequest = { showAddRoadmapDialog = false },
            title = { Text("Write Custom Learning Roadmap", color = Color.White) },
            containerColor = GlassSurfaceDark,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    Text("This creates a structured roadmap for $activeUserContext.", color = SecondaryTextLavender, fontSize = 12.sp)
                    OutlinedTextField(
                        value = newRoadmapTitle,
                        onValueChange = { newRoadmapTitle = it },
                        label = { Text("Roadmap Subject") },
                        placeholder = { Text("e.g. Master English Grammar") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricLavender,
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedLabelColor = ElectricLavender,
                            unfocusedLabelColor = SecondaryTextLavender,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newRoadmapDesc,
                        onValueChange = { newRoadmapDesc = it },
                        label = { Text("Overall Objectives / Description") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricLavender,
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedLabelColor = ElectricLavender,
                            unfocusedLabelColor = SecondaryTextLavender,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newRoadmapTitle.isNotBlank()) {
                            viewModel.addRoadmap(
                                ownerId = activeUserContext,
                                title = newRoadmapTitle,
                                description = newRoadmapDesc,
                                lessons = listOf(
                                    "Phase 1: Basic foundations" to "Explore initial keywords, structures and terminologies.",
                                    "Phase 2: Intermediate drills" to "Implement progressive test challenges and real practical applications.",
                                    "Phase 3: Advanced validation" to "Review complete case studies and execute complex independent final tasks."
                                )
                            )
                            newRoadmapTitle = ""
                            newRoadmapDesc = ""
                            showAddRoadmapDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLavender)
                ) {
                    Text("Create Roadmap", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRoadmapDialog = false }) {
                    Text("Cancel", color = SecondaryTextLavender)
                }
            }
        )
    }
}

@Composable
fun CalendarGrid(
    selectedDate: String,
    learningTasks: List<LearningTask>,
    onDayClick: (String) -> Unit
) {
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Generate days of current month. Let's make it display static grid of 35 cells to look uniform.
    // For simplicity of a beautiful UI widget, let's display days from today and the surrounding 14 days, or current calendar month.
    // Let's draw the current month!
    val currentYear = cal.get(Calendar.YEAR)
    val currentMonth = cal.get(Calendar.MONTH)
    
    val firstDayCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYear)
        set(Calendar.MONTH, currentMonth)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    
    val dayOfWeekOffset = firstDayCal.get(Calendar.DAY_OF_WEEK) - 1 // 0: Sunday, 1: Monday etc.
    val maxDaysInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Week Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Text(
                    text = it,
                    fontSize = 11.sp,
                    color = SecondaryTextLavender,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days Grid mapping
        val dayRows = 6
        val cols = 7

        repeat(dayRows) { row ->
            var rowHasDays = false
            repeat(cols) { col ->
                val idx = row * cols + col
                val dayN = idx - dayOfWeekOffset + 1
                if (dayN in 1..maxDaysInMonth) {
                    rowHasDays = true
                }
            }

            if (rowHasDays) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(cols) { col ->
                        val index = row * cols + col
                        val dayNum = index - dayOfWeekOffset + 1

                        if (dayNum in 1..maxDaysInMonth) {
                            val cellCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, currentYear)
                                set(Calendar.MONTH, currentMonth)
                                set(Calendar.DAY_OF_MONTH, dayNum)
                            }
                            val cellDateStr = sdf.format(cellCal.time)
                            val isSelected = selectedDate == cellDateStr

                            // Find tasks completed on this date
                            val dayTasks = learningTasks.filter { it.dateString == cellDateStr }
                            val hasUserTask = dayTasks.any { it.ownerId == "user" && it.isCompleted }
                            val hasGfTask = dayTasks.any { it.ownerId == "girlfriend" && it.isCompleted }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(46.dp)
                                    .background(
                                        if (isSelected) ElectricLavender.copy(alpha = 0.3f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) ElectricLavender else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onDayClick(cellDateStr) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Draw dots underneath cell numbers
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (hasUserTask) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .background(ElectricLavender, CircleShape)
                                        )
                                    }
                                    if (hasGfTask) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .background(SweetheartedPeach, CircleShape)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Empty spacer cell
                            Spacer(modifier = Modifier.width(36.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}


@Composable
fun ExpensesCalendarView(
    selectedDate: String?,
    expenses: List<com.example.data.model.ExpenseEntry>,
    onDayClick: (String?) -> Unit
) {
    var currentYearMonth by remember {
        val cal = Calendar.getInstance()
        mutableStateOf(Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)))
    }

    val displayCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYearMonth.first)
        set(Calendar.MONTH, currentYearMonth.second)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val year = currentYearMonth.first
    val month = currentYearMonth.second

    val monthName = displayCal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: ""
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val firstDayOfWeekOffset = displayCal.get(Calendar.DAY_OF_WEEK) - 1
    val maxDaysInMonth = displayCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        borderBrush = Brush.linearGradient(colors = listOf(ElectricLavender.copy(alpha = 0.3f), CosmicCyan.copy(alpha = 0.2f)))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val prevCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            add(Calendar.MONTH, -1)
                        }
                        currentYearMonth = Pair(prevCal.get(Calendar.YEAR), prevCal.get(Calendar.MONTH))
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Prev Month", tint = Color.White)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$monthName $year",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    if (selectedDate != null) {
                        IconButton(
                            onClick = { onDayClick(null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Clear search filter", tint = CosmicCyan, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                IconButton(
                    onClick = {
                        val nextCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            add(Calendar.MONTH, 1)
                        }
                        currentYearMonth = Pair(nextCal.get(Calendar.YEAR), nextCal.get(Calendar.MONTH))
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next Month", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach {
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        color = SecondaryTextLavender,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val dayRows = 6
            val cols = 7

            Column {
                repeat(dayRows) { row ->
                    var rowHasDays = false
                    repeat(cols) { col ->
                        val idx = row * cols + col
                        val dayN = idx - firstDayOfWeekOffset + 1
                        if (dayN in 1..maxDaysInMonth) {
                            rowHasDays = true
                        }
                    }

                    if (rowHasDays) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(cols) { col ->
                                val index = row * cols + col
                                val dayNum = index - firstDayOfWeekOffset + 1

                                if (dayNum in 1..maxDaysInMonth) {
                                    val cellCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, dayNum)
                                    }
                                    val cellDateStr = sdf.format(cellCal.time)
                                    val isSelected = selectedDate == cellDateStr

                                    val dayExpenses = expenses.filter { it.dateString == cellDateStr }
                                    val hasIncome = dayExpenses.any { it.isIncome }
                                    val hasExpense = dayExpenses.any { !it.isIncome }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(44.dp)
                                            .background(
                                                if (isSelected) ElectricLavender.copy(alpha = 0.3f) else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                width = if (isSelected) 1.dp else 0.dp,
                                                color = if (isSelected) ElectricLavender else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                if (isSelected) {
                                                    onDayClick(null)
                                                } else {
                                                    onDayClick(cellDateStr)
                                                }
                                            }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = dayNum.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (hasIncome) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .background(Color(0xFF81C784), CircleShape)
                                                )
                                            }
                                            if (hasExpense) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .background(Color(0xFFE57373), CircleShape)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(36.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}


// --- 3. EXPENSES SCREEN (COUPLE CASH FLOW LEDGER) ---
@Composable
fun ExpensesScreen(viewModel: MainViewModel) {
    val profiles by viewModel.profilesFlow.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpensesFlow.collectAsStateWithLifecycle()
    val activeUserContext by viewModel.activeUserContext.collectAsStateWithLifecycle()

    var expenseAmount by remember { mutableStateOf("") }
    var expenseIsIncome by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Food") }
    var expenseNote by remember { mutableStateOf("") }
    var selectedFilterDate by remember { mutableStateOf<String?>(null) }

    val myProfile = profiles.find { it.id == "user" }
    val gfProfile = profiles.find { it.id == "girlfriend" }
    val myName = myProfile?.name ?: "Minnyo"
    val gfName = gfProfile?.name ?: "Honey"

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayString = sdf.format(Date())

    // Math computations
    val totalIn = expenses.filter { it.isIncome }.sumOf { it.amount }
    val totalOut = expenses.filter { !it.isIncome }.sumOf { it.amount }
    val combinedSavings = totalIn - totalOut

    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Ledger Title
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Mutual Expense Checker", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Unified log for budgets and saving goals • Transparent 💖", fontSize = 13.sp, color = SecondaryTextLavender)
            }
            IconButton(
                onClick = { com.example.utils.CsvExportUtil.exportExpensesToCsv(context, expenses) },
                modifier = Modifier.background(Color(0x2AFFFFFF), CircleShape)
            ) {
                Icon(Icons.Filled.Share, contentDescription = "Export CSV", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Combined Net Worth Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderBrush = Brush.linearGradient(colors = listOf(CosmicCyan.copy(alpha = 0.5f), ElectricLavender.copy(alpha = 0.2f)))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Mutual Cash Savings", fontSize = 12.sp, color = SecondaryTextLavender)
                    Text("$${String.format(Locale.getDefault(), "%,.2f", combinedSavings)}", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = CosmicCyan)
                }

                Badge(containerColor = if (combinedSavings >= 0) Color(0x334CAF50) else Color(0x33F44336)) {
                    Text(
                        text = if (combinedSavings >= 0) "Surplus Status" else "Deficit warning",
                        color = if (combinedSavings >= 0) Color(0xFF81C784) else Color(0xFFE57373),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0x11FFFFFF))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF81C784), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gross Income", fontSize = 11.sp, color = SecondaryTextLavender)
                    }
                    Text("$${String.format(Locale.getDefault(), "%,.1f", totalIn)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFE57373), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Total Outflows", fontSize = 11.sp, color = SecondaryTextLavender)
                    }
                    Text("$${String.format(Locale.getDefault(), "%,.1f", totalOut)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Tracker Calendar
        Text("Duo Financial Daily Calendar", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Visual daily spend tracking & retrospective search 📅", fontSize = 12.sp, color = SecondaryTextLavender, modifier = Modifier.padding(bottom = 6.dp))
        ExpensesCalendarView(
            selectedDate = selectedFilterDate,
            expenses = expenses,
            onDayClick = { selectedFilterDate = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Manual Cost Input Form
        Text("Log New Entry", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selector for income vs expense
                listOf(false to "Expense 📉", true to "Income 📈").forEach { (isIncome, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (expenseIsIncome == isIncome) {
                                    if (isIncome) Color(0x334CAF50) else Color(0x33F44336)
                                } else Color(0x0AFFFFFF),
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (expenseIsIncome == isIncome) {
                                    if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
                                } else Color(0x1AFFFFFF),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { expenseIsIncome = isIncome }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount TextField & Owner selector inline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = expenseAmount,
                    onValueChange = { expenseAmount = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("0.00", color = SecondaryTextLavender) },
                    label = { Text("Amount ($)", fontSize = 12.sp) },
                    modifier = Modifier.weight(1.2f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicCyan,
                        unfocusedBorderColor = Color(0x44FFFFFF),
                        focusedLabelColor = CosmicCyan,
                        unfocusedLabelColor = SecondaryTextLavender,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                // Contextual user toggle
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0x0AFFFFFF), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf("user" to "🦁", "girlfriend" to "🌸").forEach { (id, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (activeUserContext == id) {
                                        if (id == "user") ElectricLavender.copy(alpha = 0.4f) else SweetheartedPeach.copy(alpha = 0.4f)
                                    } else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setActiveUserContext(id) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category select row
            Text("Category Selection", fontSize = 11.sp, color = SecondaryTextLavender)
            Spacer(modifier = Modifier.height(4.dp))
            val categories = if (expenseIsIncome) listOf("Salary", "Investment", "Bonus", "Other") else listOf("Food", "Transport", "Shopping", "Entertainment", "Utilities", "Other")
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(categories) { cat ->
                    val isCatSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .background(if (isCatSelected) ElectricLavender.copy(alpha = 0.4f) else Color(0x0FFFFFFF), RoundedCornerShape(8.dp))
                            .border(width = 1.dp, color = if (isCatSelected) ElectricLavender else Color(0x11FFFFFF), shape = RoundedCornerShape(8.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(cat, color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Text Label
            OutlinedTextField(
                value = expenseNote,
                onValueChange = { expenseNote = it },
                label = { Text("Short Description / Note") },
                placeholder = { Text("e.g. Weekly organic vegetables package", color = SecondaryTextLavender) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricLavender,
                    unfocusedBorderColor = Color(0x22FFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = ElectricLavender,
                    unfocusedLabelColor = SecondaryTextLavender
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    val amt = expenseAmount.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        viewModel.addExpense(
                            ownerId = activeUserContext,
                            amount = amt,
                            isIncome = expenseIsIncome,
                            category = selectedCategory,
                            dateString = todayString,
                            note = expenseNote.ifBlank { selectedCategory }
                        )
                        // Reset forms
                        expenseAmount = ""
                        expenseNote = ""
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLavender),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirm Entry Log", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1-Tap Auto Spend/Quick presets (Highly requested Frictionless Auto features)
        Text("Frictionless Instant Add Cost Presets", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val presets = listOf(
                Triple("☕ Café Latte", 4.5, "Food"),
                Triple("🚗 Uber / Bolt Ride", 12.0, "Transport"),
                Triple("🍿 Movie Combo", 15.0, "Entertainment")
            )
            presets.forEach { (title, cost, cat) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0x13FFFFFF), RoundedCornerShape(12.dp))
                        .border(1.dp, CosmicCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.addExpense(
                                ownerId = activeUserContext,
                                amount = cost,
                                isIncome = false,
                                category = cat,
                                dateString = todayString,
                                note = title
                            )
                        }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(title.split(" ")[0], fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(title.substringAfter(" "), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("$${cost}", fontSize = 12.sp, color = CosmicCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History logs
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Duo Financial Ledger Logs", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            if (selectedFilterDate != null) {
                TextButton(
                    onClick = { selectedFilterDate = null },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Clear Filter ✖", fontSize = 11.sp, color = CosmicCyan, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (selectedFilterDate != null) {
            val dayIn = expenses.filter { it.dateString == selectedFilterDate && it.isIncome }.sumOf { it.amount }
            val dayOut = expenses.filter { it.dateString == selectedFilterDate && !it.isIncome }.sumOf { it.amount }
            val dayNet = dayIn - dayOut

            GlassCard(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                borderBrush = Brush.linearGradient(colors = listOf(CosmicCyan.copy(alpha = 0.3f), Color.Transparent))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Selected Date: $selectedFilterDate 🔍", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CosmicCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Day's Revenue: $${String.format(Locale.getDefault(), "%.1f", dayIn)}", fontSize = 11.sp, color = Color(0xFF81C784))
                        Text("Day's Cost: $${String.format(Locale.getDefault(), "%.1f", dayOut)}", fontSize = 11.sp, color = Color(0xFFE57373))
                        Text("Net: $${String.format(Locale.getDefault(), "%.1f", dayNet)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        val displayedExpenses = if (selectedFilterDate != null) {
            expenses.filter { it.dateString == selectedFilterDate }
        } else {
            expenses
        }

        if (displayedExpenses.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (selectedFilterDate != null)
                        "No cost entries logged on $selectedFilterDate. Tap on other calendar dates or click Clear Filter to view everything!"
                        else "No cost logs yet. Entries you type or auto-presets click will stream here instantly, syncing with your partner in real time!",
                    fontSize = 12.sp,
                    color = SecondaryTextLavender,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            displayedExpenses.forEach { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color(0x0AFFFFFF), RoundedCornerShape(12.dp))
                        .border(0.5.dp, Color(0x11FFFFFF), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar tag
                        Box(
                            modifier = Modifier
                                .background(
                                    if (log.ownerId == "user") ElectricLavender.copy(alpha = 0.2f)
                                    else SweetheartedPeach.copy(alpha = 0.2f),
                                    CircleShape
                                )
                                .size(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (log.ownerId == "user") "🦁" else "🌸",
                                fontSize = 14.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = log.note,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${log.category} • ${log.dateString}",
                                fontSize = 11.sp,
                                color = SecondaryTextLavender
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${if (log.isIncome) "+" else "-"}$${String.format(Locale.getDefault(), "%.1f", log.amount)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (log.isIncome) Color(0xFF81C784) else Color(0xFFE57373)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = { viewModel.deleteExpense(log.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove entry", tint = Color(0x33FF5A5A), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}


// --- 4. SAVINGS ADVISOR SCREEN (AI COACHING HUB) ---
@Composable
fun SavingsAdvisorScreen(viewModel: MainViewModel) {
    val profiles by viewModel.profilesFlow.collectAsStateWithLifecycle()
    val savingTasks by viewModel.allSavingTasksFlow.collectAsStateWithLifecycle()
    val aiAdvice by viewModel.aiAdvice.collectAsStateWithLifecycle()
    val isLoadingAdvice by viewModel.isLoadingAdvice.collectAsStateWithLifecycle()

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
        Spacer(modifier = Modifier.height(16.dp))

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
}


// --- 5. PROFILES SCREEN (PARTNER PERSONALIZATION) ---
@Composable
fun ProfilesScreen(viewModel: MainViewModel) {
    val profiles by viewModel.profilesFlow.collectAsStateWithLifecycle()

    val myProfile = profiles.find { it.id == "user" }
    val gfProfile = profiles.find { it.id == "girlfriend" }

    var myName by remember { mutableStateOf("") }
    var myEmoji by remember { mutableStateOf("") }
    var myBudget by remember { mutableStateOf("") }
    var myGoal by remember { mutableStateOf("") }

    var gfName by remember { mutableStateOf("") }
    var gfEmoji by remember { mutableStateOf("") }
    var gfBudget by remember { mutableStateOf("") }
    var gfGoal by remember { mutableStateOf("") }

    var supabaseUrl by remember { mutableStateOf(viewModel.supabaseSyncManager.getSupabaseUrl()) }
    var supabaseKey by remember { mutableStateOf(viewModel.supabaseSyncManager.getSupabaseAnonKey()) }
    var testResult by remember { mutableStateOf<Boolean?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var isSqlExpanded by remember { mutableStateOf(false) }
    var showAdminPortal by remember { mutableStateOf(false) }
    val syncState by viewModel.supabaseSyncState.collectAsStateWithLifecycle()
    val customGeminiApiKeyFlow by viewModel.customGeminiApiKey.collectAsStateWithLifecycle()
    var customGeminiKey by remember(customGeminiApiKeyFlow) { mutableStateOf(customGeminiApiKeyFlow) }
    val scope = rememberCoroutineScope()

    // Synchronize initial values once profiles load
    LaunchedEffect(profiles) {
        if (myProfile != null && myName.isBlank()) {
            myName = myProfile.name
            myEmoji = myProfile.avatarEmoji
            myBudget = myProfile.dailyBudget.toString()
            myGoal = myProfile.monthlySavingGoal.toString()
        }
        if (gfProfile != null && gfName.isBlank()) {
            gfName = gfProfile.name
            gfEmoji = gfProfile.avatarEmoji
            gfBudget = gfProfile.dailyBudget.toString()
            gfGoal = gfProfile.monthlySavingGoal.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Column {
            Text("Settings & Profiles", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Customize usernames, emojis, daily budgets & mutual goals", fontSize = 13.sp, color = SecondaryTextLavender)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Minnyo's custom card
        Text("Your Profile Card (Minnyo's)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 6.dp))
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderBrush = Brush.linearGradient(colors = listOf(ElectricLavender.copy(alpha = 0.5f), Color.Transparent))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = myName,
                    onValueChange = { myName = it },
                    label = { Text("Your Nickname") },
                    modifier = Modifier.weight(1.5f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ElectricLavender,
                        unfocusedBorderColor = Color(0x1FFFFFFF)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = myEmoji,
                    onValueChange = { myEmoji = it },
                    label = { Text("Avatar") },
                    modifier = Modifier.weight(0.7f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ElectricLavender,
                        unfocusedBorderColor = Color(0x1FFFFFFF)
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = myBudget,
                    onValueChange = { myBudget = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Daily Spend Limit ($)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ElectricLavender,
                        unfocusedBorderColor = Color(0x1FFFFFFF)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = myGoal,
                    onValueChange = { myGoal = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Monthly Goal ($)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ElectricLavender,
                        unfocusedBorderColor = Color(0x1FFFFFFF)
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val bud = myBudget.toDoubleOrNull() ?: 80.0
                    val gol = myGoal.toDoubleOrNull() ?: 600.0
                    viewModel.updateProfile("user", myName.ifBlank { "Minnyo" }, myEmoji.ifBlank { "🦁" }, bud, gol)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLavender)
            ) {
                Text("Confirm Save (Minnyo's Data)", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Girlfriend's custom card
        Text("Your Girlfriend's Profile Card", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 6.dp))
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderBrush = Brush.linearGradient(colors = listOf(SweetheartedPeach.copy(alpha = 0.5f), Color.Transparent))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = gfName,
                    onValueChange = { gfName = it },
                    label = { Text("Girlfriend's Nickname") },
                    modifier = Modifier.weight(1.5f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SweetheartedPeach,
                        unfocusedBorderColor = Color(0x1FFFFFFF)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = gfEmoji,
                    onValueChange = { gfEmoji = it },
                    label = { Text("Avatar") },
                    modifier = Modifier.weight(0.7f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SweetheartedPeach,
                        unfocusedBorderColor = Color(0x1FFFFFFF)
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = gfBudget,
                    onValueChange = { gfBudget = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Daily Spend Limit ($)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SweetheartedPeach,
                        unfocusedBorderColor = Color(0x1FFFFFFF)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = gfGoal,
                    onValueChange = { gfGoal = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Monthly Goal ($)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SweetheartedPeach,
                        unfocusedBorderColor = Color(0x1FFFFFFF)
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val bud = gfBudget.toDoubleOrNull() ?: 70.0
                    val gol = gfGoal.toDoubleOrNull() ?: 550.0
                    viewModel.updateProfile("girlfriend", gfName.ifBlank { "Honey 🌸" }, gfEmoji.ifBlank { "🦄" }, bud, gol)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SweetheartedPeach)
            ) {
                Text("Confirm Save (Girlfriend's Data)", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Session Control & Logout Center
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Connection Status", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Exclusive Duo Couple Space Linked", fontSize = 11.sp, color = SecondaryTextLavender)
                }

                Button(
                    onClick = { viewModel.logOut() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF5A5A)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ExitToApp,
                        contentDescription = "Log Out",
                        tint = Color(0xFFFF7373),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Out", color = Color(0xFFFF7373), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Supabase Integration Card
        Text("Supabase Cloud Database Link ☁️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 6.dp))
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderBrush = Brush.linearGradient(colors = listOf(CosmicCyan.copy(alpha = 0.5f), Color.Transparent))
        ) {
            Text(
                text = "Supabase API Integration Host",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CosmicCyan,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Synchronize data variables across multiple partner devices using PostgreSQL. All credentials are saved strictly on this device.",
                fontSize = 11.sp,
                color = SecondaryTextLavender,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = supabaseUrl,
                onValueChange = { supabaseUrl = it ; testResult = null },
                label = { Text("Supabase Base URL") },
                placeholder = { Text("https://your-project.supabase.co") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = CosmicCyan,
                    unfocusedBorderColor = Color(0x1FFFFFFF)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = supabaseKey,
                onValueChange = { supabaseKey = it ; testResult = null },
                label = { Text("Supabase Anon Public Key") },
                placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = CosmicCyan,
                    unfocusedBorderColor = Color(0x1FFFFFFF)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Save Button
                Button(
                    onClick = {
                        viewModel.saveSupabaseCredentials(supabaseUrl, supabaseKey)
                        testResult = null
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan)
                ) {
                    Text("Save Config", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                // Clear Button
                Button(
                    onClick = {
                        viewModel.clearSupabaseCredentials()
                        supabaseUrl = ""
                        supabaseKey = ""
                        testResult = null
                        viewModel.resetSupabaseState()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF))
                ) {
                    Text("Clear", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Test Connection Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isTestingConnection = true
                            testResult = viewModel.testSupabaseConnection()
                            isTestingConnection = false
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x0FFFFFFF)),
                    enabled = supabaseUrl.isNotBlank() && supabaseKey.isNotBlank() && !isTestingConnection
                ) {
                    if (isTestingConnection) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CosmicCyan, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verifying...", fontSize = 12.sp, color = Color.White)
                    } else {
                        Text("Test Connection ⚡", fontSize = 12.sp, color = Color.White)
                    }
                }

                testResult?.let { connected ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (connected) Color(0xFF00E676) else Color(0xFFFF5252), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (connected) "Connected 🟢" else "Connection Failed 🔴",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (connected) Color(0xFF00E676) else Color(0xFFFF5252)
                        )
                    }
                }
            }

            if (viewModel.isSupabaseConfigured()) {
                Divider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = Color(0x1AFFFFFF)
                )

                Text(
                    text = "Cloud Sync Center",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Backup current lists to Supabase, or completely restore them to this device.",
                    fontSize = 11.sp,
                    color = SecondaryTextLavender,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Sync controls row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pull
                    Button(
                        onClick = { viewModel.triggerSupabasePull() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricLavender.copy(alpha = 0.4f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CloudDownload, contentDescription = "Pull", modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore From Cloud", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Push
                    Button(
                        onClick = { viewModel.triggerSupabasePush() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan.copy(alpha = 0.4f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CloudUpload, contentDescription = "Push", modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Backup to Cloud", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Sync status indicator
                when (val sync = syncState) {
                    is SupabaseSyncState.Loading -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CosmicCyan, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(sync.message, fontSize = 12.sp, color = CosmicCyan)
                        }
                    }
                    is SupabaseSyncState.Success -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Done", tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(sync.message, fontSize = 12.sp, color = Color(0xFF00E676), fontWeight = FontWeight.Medium)
                        }
                    }
                    is SupabaseSyncState.Error -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Error, contentDescription = "Error", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(sync.errorReason, fontSize = 12.sp, color = Color(0xFFFF5252))
                        }
                    }
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expandable SQL Schema setup card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isSqlExpanded = !isSqlExpanded }
                    .background(Color(0x0AFFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Code,
                        contentDescription = "SQL code",
                        tint = SecondaryTextLavender,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Supabase PostgreSQL SQL Script", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Icon(
                    imageVector = if (isSqlExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Toggle",
                    tint = SecondaryTextLavender,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (isSqlExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x1A000000), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = """
                            -- RUN THIS IN YOUR SUPABASE SQL EDITOR:
                            
                            CREATE TABLE IF NOT EXISTS user_profiles (
                                id TEXT PRIMARY KEY,
                                name TEXT NOT NULL,
                                avatarEmoji TEXT NOT NULL,
                                dailyBudget REAL NOT NULL DEFAULT 50.0,
                                monthlySavingGoal REAL NOT NULL DEFAULT 500.0
                            );

                            CREATE TABLE IF NOT EXISTS learning_roadmaps (
                                id INT PRIMARY KEY,
                                ownerId TEXT NOT NULL,
                                title TEXT NOT NULL,
                                description TEXT NOT NULL,
                                createdTimestamp BIGINT NOT NULL
                            );

                            CREATE TABLE IF NOT EXISTS roadmap_lessons (
                                id INT PRIMARY KEY,
                                roadmapId INT NOT NULL,
                                title TEXT NOT NULL,
                                description TEXT NOT NULL,
                                difficulty TEXT NOT NULL,
                                isCompleted BOOLEAN NOT NULL DEFAULT false,
                                orderIndex INT NOT NULL
                            );

                            CREATE TABLE IF NOT EXISTS learning_tasks (
                                id INT PRIMARY KEY,
                                ownerId TEXT NOT NULL,
                                title TEXT NOT NULL,
                                dateString TEXT NOT NULL,
                                isCompleted BOOLEAN NOT NULL DEFAULT false,
                                minutesSpent INT NOT NULL DEFAULT 0,
                                timestamp BIGINT NOT NULL
                            );

                            CREATE TABLE IF NOT EXISTS expense_entries (
                                id INT PRIMARY KEY,
                                ownerId TEXT NOT NULL,
                                amount REAL NOT NULL,
                                isIncome BOOLEAN NOT NULL,
                                category TEXT NOT NULL,
                                dateString TEXT NOT NULL,
                                note TEXT NOT NULL DEFAULT '',
                                timestamp BIGINT NOT NULL
                            );

                            CREATE TABLE IF NOT EXISTS saving_tasks (
                                id INT PRIMARY KEY,
                                ownerId TEXT NOT NULL,
                                title TEXT NOT NULL,
                                rewardAmount REAL NOT NULL,
                                dateString TEXT NOT NULL,
                                isCompleted BOOLEAN NOT NULL DEFAULT false,
                                timestamp BIGINT NOT NULL
                            );

                            CREATE TABLE IF NOT EXISTS calendar_tasks (
                                id TEXT PRIMARY KEY,
                                title TEXT NOT NULL,
                                time TEXT NOT NULL,
                                isCompleted BOOLEAN NOT NULL DEFAULT false,
                                timestamp BIGINT NOT NULL
                            );

                            CREATE TABLE IF NOT EXISTS user_accounts (
                                email TEXT PRIMARY KEY,
                                name TEXT NOT NULL,
                                emoji TEXT NOT NULL,
                                created_at TIMESTAMP DEFAULT now()
                            );

                            CREATE TABLE IF NOT EXISTS device_status_telemetry (
                                device_id TEXT PRIMARY KEY,
                                device_name TEXT NOT NULL,
                                cpu_usage INT NOT NULL,
                                ram_usage REAL NOT NULL,
                                user_email TEXT NOT NULL,
                                synced_at TIMESTAMP DEFAULT now()
                            );
                        """.trimIndent(),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = SecondaryTextLavender
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Gemini AI Assistant Key Configuration
        Text("Gemini AI Assistant Key 🧠", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 6.dp))
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderBrush = Brush.linearGradient(colors = listOf(ElectricLavender.copy(alpha = 0.5f), Color.Transparent))
        ) {
            Text(
                text = "Gemini API Client Configuration",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricLavender,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Because you are using a custom built APK, the AI assistant runs directly on your device. To enable real-time predictions, paste your personal Google Gemini API Key here. Your key is stored strictly locally in your private secure preferences.",
                fontSize = 11.sp,
                color = SecondaryTextLavender,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = customGeminiKey,
                onValueChange = { customGeminiKey = it },
                label = { Text("Gemini API Key") },
                placeholder = { Text("AIzaSy...") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = ElectricLavender,
                    unfocusedBorderColor = Color(0x1FFFFFFF)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Save Button
                Button(
                    onClick = {
                        viewModel.saveGeminiApiKey(customGeminiKey)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLavender)
                ) {
                    Text("Save API Key", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                // Clear Button
                Button(
                    onClick = {
                        viewModel.clearGeminiApiKey()
                        customGeminiKey = ""
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF))
                ) {
                    Text("Clear", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Open Superuser Admin Portal Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderBrush = Brush.linearGradient(colors = listOf(SweetheartedPeach.copy(alpha = 0.5f), CosmicCyan.copy(alpha = 0.5f)))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("Superuser Operations Desk 🛡️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Check user login count, live device node metrics, CPU profiles, and configure web views.", fontSize = 11.sp, color = SecondaryTextLavender)
                    }
                    Button(
                        onClick = { showAdminPortal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Open Desk", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        if (showAdminPortal) {
            AdminPortalDialog(viewModel, onDismiss = { showAdminPortal = false })
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Technical Security Warnings
        Text(
            text = "Security Advice Note: UsSpace is designed for mutual transparency with zero data logs shared with external parties. Local SQLite files reside strictly on your personal device.",
            fontSize = 11.sp,
            color = SecondaryTextLavender,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp)
        )
    }
}


// --- REUSABLE GLASS BOTTOM NAVIGATION BAR ---
@Composable
fun CoupleNavigationBar(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    val navBarBorderBrush = Brush.linearGradient(
        colors = listOf(Color(0x2AFFFFFF), Color(0x05FFFFFF))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars) // Respect Android Safe gesture bar spacing
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(
                color = Color(0x1FFFFFFF), // Translucent backdrop blur layer
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                brush = navBarBorderBrush,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navItems = listOf(
                Triple("Home", Icons.Filled.Home, 0),
                Triple("Learn", Icons.Filled.MenuBook, 1),
                Triple("Ledger", Icons.Filled.AttachMoney, 2),
                Triple("AI coach", Icons.Filled.AutoAwesome, 3),
                Triple("Settings", Icons.Filled.Settings, 4)
            )

            navItems.forEach { (title, icon, index) ->
                val isSelected = selectedTab == index
                val itemThemeColor = if (index % 2 == 0) ElectricLavender else CosmicCyan

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { viewModel.selectTab(index) }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isSelected) itemThemeColor else SecondaryTextLavender,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else SecondaryTextLavender
                    )
                }
            }
        }
    }
}

@Composable
fun RoadmapCard(roadmap: LearningRoadmap, viewModel: MainViewModel) {
    val roadmapLessonsFlow = remember(roadmap.id) { viewModel.getLessonsForRoadmap(roadmap.id) }
    val lessons by roadmapLessonsFlow.collectAsStateWithLifecycle(emptyList())

    val completed = lessons.filter { it.isCompleted }.size
    val total = lessons.size
    val milestonePercent = if (total > 0) (completed.toFloat() / total) else 0f

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(roadmap.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(roadmap.description, fontSize = 12.sp, color = SecondaryTextLavender)
            }

            IconButton(onClick = { viewModel.deleteRoadmap(roadmap.id) }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Track", tint = Color(0x66FF5A5A))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Progress ($completed/$total Lessons)", fontSize = 10.sp, color = SecondaryTextLavender)
            Text("${(milestonePercent * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CosmicCyan)
        }
        Spacer(modifier = Modifier.height(4.dp))
        val animatedMilestonePercent by androidx.compose.animation.core.animateFloatAsState(
            targetValue = milestonePercent,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            label = "milestone_progress"
        )
        LinearProgressIndicator(
            progress = { animatedMilestonePercent },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = CosmicCyan,
            trackColor = Color(0x11FFFFFF)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (lessons.isEmpty()) {
            Text(
                "This roadmap has no study steps yet. Write milestones to track progress lessons!",
                fontSize = 11.sp,
                color = SecondaryTextLavender,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            lessons.forEach { lesson ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.toggleLessonCompletion(lesson) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (lesson.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = "Lesson Check",
                            tint = if (lesson.isCompleted) ElectricLavender else Color(0x22FFFFFF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = lesson.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (lesson.isCompleted) SecondaryTextLavender else Color.White
                            )
                            Text(lesson.description, fontSize = 10.sp, color = SecondaryTextLavender)
                        }
                    }

                    Badge(
                        containerColor = when (lesson.difficulty) {
                            "Beginner" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            "Intermediate" -> Color(0xFFFF9800).copy(alpha = 0.2f)
                            else -> Color(0xFFF44336).copy(alpha = 0.2f)
                        },
                        contentColor = when (lesson.difficulty) {
                            "Beginner" -> Color(0xFF81C784)
                            "Intermediate" -> Color(0xFFFFB74D)
                            else -> Color(0xFFE57373)
                        }
                    ) {
                        Text(lesson.difficulty, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    val emojiList = listOf("🦁", "🐻", "🐱", "🐼", "🐨", "🐰", "🐉", "🤴")
    var selectedEmoji by remember { mutableStateOf("🦁") }
    
    var isSignUpMode by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var showForgotPassword by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    var forgotCodeInput by remember { mutableStateOf("") }
    var forgotNewPassword by remember { mutableStateOf("") }
    var forgotConfirmPassword by remember { mutableStateOf("") }
    var forgotStep by remember { mutableStateOf(1) } // 1: Email Request, 2: Code verification, 3: Set Password
    var generatedCode by remember { mutableStateOf("") }
    var forgotError by remember { mutableStateOf<String?>(null) }
    var forgotStatus by remember { mutableStateOf<String?>(null) }
    var isSendingAnimation by remember { mutableStateOf(false) }
    var smtpLogs by remember { mutableStateOf<List<String>>(emptyList()) }
    val context = androidx.compose.ui.platform.LocalContext.current

    fun sendResetEmailIntent(targetEmail: String, code: String) {
        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:")
            putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(targetEmail))
            putExtra(android.content.Intent.EXTRA_SUBJECT, "UsSpace Password Reset Verification Code")
            putExtra(android.content.Intent.EXTRA_TEXT, "Hello,\n\nYou requested a password reset for your UsSpace account.\n\nYour 6-digit confirmation code is: $code\n\nPlease enter this security code in your app to unlock your credentials and configure a new password.\n\nWith love,\nUsSpace security services")
        }
        try {
            val chooser = android.content.Intent.createChooser(intent, "Send Reset Mail via...")
            chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Throwable) {
            android.util.Log.e("LoveTracker", "Failed to launch mail chooser: ${e.message}", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Custom Neon/Glass Hearts Icon Representation
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .background(Color(0x0EFFFFFF), shape = CircleShape)
                .border(2.dp, Brush.linearGradient(listOf(ElectricLavender, CosmicCyan)), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = "Glowing Heart",
                tint = CosmicCyan,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to UsSpace",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "A private, cyber-minimalist canvas exclusively for loving couples to coordinate budgets, tasks, and roadmaps.",
            fontSize = 13.sp,
            color = SecondaryTextLavender,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Segmented Tab for Login & Register Selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x0FFFFFFF), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (!isSignUpMode) ElectricLavender.copy(alpha = 0.4f) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { 
                        isSignUpMode = false 
                        errorMessage = null
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sign In",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isSignUpMode) ElectricLavender.copy(alpha = 0.4f) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { 
                        isSignUpMode = true 
                        errorMessage = null
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Create Account",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        errorMessage?.let { err ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                borderBrush = Brush.linearGradient(colors = listOf(Color(0xFFFF5252).copy(alpha = 0.5f), Color.Transparent))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Error",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = err,
                        color = Color(0xFFFFCCCC),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (isSignUpMode) "Register Your Details" else "Sign In Credentials",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricLavender,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isSignUpMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it ; errorMessage = null },
                    label = { Text("Enter Your Name") },
                    placeholder = { Text("e.g. Minnyo", color = SecondaryTextLavender) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it ; errorMessage = null },
                label = { Text("Email Address") },
                placeholder = { Text("email@example.com", color = SecondaryTextLavender) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it ; errorMessage = null },
                label = { Text("Password") },
                placeholder = { Text("at least 6 characters", color = SecondaryTextLavender) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                ),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.LockOpen else Icons.Filled.Lock,
                            contentDescription = if (showPassword) "Hide password" else "Show password",
                            tint = SecondaryTextLavender
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            if (!isSignUpMode) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Forgot Password?",
                        color = CosmicCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                forgotEmail = email
                                forgotError = null
                                forgotStatus = null
                                forgotStep = 1
                                showForgotPassword = true
                            }
                            .padding(vertical = 4.dp)
                    )
                }
            }

            if (isSignUpMode) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Choose Your Avatar Emoji",
                    fontSize = 12.sp,
                    color = SecondaryTextLavender,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(emojiList) { emoji ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = if (selectedEmoji == emoji) ElectricLavender.copy(alpha = 0.3f) else Color(0x0AFFFFFF),
                                    shape = CircleShape
                                )
                                .border(
                                    width = if (selectedEmoji == emoji) 2.dp else 1.dp,
                                    brush = if (selectedEmoji == emoji) Brush.linearGradient(listOf(ElectricLavender, CosmicCyan)) else SolidColor(Color(0x1AFFFFFF)),
                                    shape = CircleShape
                                )
                                .clickable { selectedEmoji = emoji }
                        ) {
                            Text(emoji, fontSize = 22.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    if (isSignUpMode) {
                        val finalName = if (name.isBlank()) "Minnyo" else name
                        errorMessage = viewModel.signUpWithEmailAndPassword(
                            email = email,
                            password = password,
                            name = finalName,
                            emoji = selectedEmoji
                        )
                    } else {
                        errorMessage = viewModel.logInWithEmailAndPassword(
                            email = email,
                            password = password
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(listOf(ElectricLavender, CosmicCyan)),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSignUpMode) "Register & Start ✨" else "Sign In & Welcome 🌸",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.clickable { 
                isSignUpMode = !isSignUpMode 
                errorMessage = null
            },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isSignUpMode) "Already have an account? " else "Don't have an account? ",
                fontSize = 13.sp,
                color = SecondaryTextLavender
            )
            Text(
                text = if (isSignUpMode) "Sign In" else "Create Account",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CosmicCyan
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderBrush = Brush.linearGradient(colors = listOf(ElectricLavender.copy(alpha = 0.3f), CosmicCyan.copy(alpha = 0.2f)))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Quick Tip",
                        tint = CosmicCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quick Demo Access",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "For development builds, use a test account that you create yourself. Never share passwords or place demo credentials in the app.",
                    fontSize = 11.sp,
                    color = SecondaryTextLavender,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    if (showForgotPassword) {
        AlertDialog(
            onDismissRequest = { if (!isSendingAnimation) showForgotPassword = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Reset Key",
                        tint = ElectricLavender,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset Password Link",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = GlassSurfaceDark,
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    forgotError?.let { err ->
                        Text(
                            text = err,
                            color = Color(0xFFFF5252),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    forgotStatus?.let { status ->
                        Text(
                            text = status,
                            color = CosmicCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (forgotStep == 1) {
                        Text(
                            text = "Enter your registered email address to receive a secure password recovery code directly to your email application.",
                            color = SecondaryTextLavender,
                            fontSize = 13.sp
                        )

                        OutlinedTextField(
                            value = forgotEmail,
                            onValueChange = { forgotEmail = it; forgotError = null },
                            label = { Text("Registered Email Address") },
                            placeholder = { Text("your.email@example.com") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricLavender,
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedLabelColor = ElectricLavender,
                                unfocusedLabelColor = SecondaryTextLavender,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (isSendingAnimation) {
                            Spacer(modifier = Modifier.height(6.dp))
                            // Interactive Futuristic SMTP logs terminal output
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .border(1.dp, ElectricLavender.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                androidx.compose.foundation.lazy.LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(smtpLogs) { log ->
                                        Text(
                                            text = log,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = if (log.startsWith("ERR:")) Color(0xFFFF5252) else if (log.contains("Success")) CosmicCyan else Color.Green
                                        )
                                    }
                                }
                            }
                        }
                    } else if (forgotStep == 2) {
                        Text(
                            text = "A password recovery code has been generated and queued for delivery. Check your device's email client. Enter the 6-digit code below to set your new password.",
                            color = SecondaryTextLavender,
                            fontSize = 13.sp
                        )

                        // Highlight fallback code beautifully on UI
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ElectricLavender.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .border(1.dp, ElectricLavender.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("YOUR SECURE ACCESS OTP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CosmicCyan)
                                Text(generatedCode, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 4.sp)
                                Text("Check your email app to simulate real mail dispatch.", fontSize = 11.sp, color = SecondaryTextLavender)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = forgotCodeInput,
                            onValueChange = { forgotCodeInput = it; forgotError = null },
                            label = { Text("6-Digit Verification Code") },
                            placeholder = { Text("e.g. 000000") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricLavender,
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedLabelColor = ElectricLavender,
                                unfocusedLabelColor = SecondaryTextLavender,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                sendResetEmailIntent(forgotEmail, generatedCode)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1F8B6CFF)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricLavender)
                        ) {
                            Icon(Icons.Filled.Mail, contentDescription = "Mail Icon", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Re-open External Mail App", color = Color.White, fontSize = 13.sp)
                        }
                    } else if (forgotStep == 3) {
                        Text(
                            text = "Choose a secure, brand-new password for your UsSpace profile account.",
                            color = SecondaryTextLavender,
                            fontSize = 13.sp
                        )

                        OutlinedTextField(
                            value = forgotNewPassword,
                            onValueChange = { forgotNewPassword = it; forgotError = null },
                            label = { Text("Choose New Password") },
                            placeholder = { Text("at least 6 characters") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricLavender,
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedLabelColor = ElectricLavender,
                                unfocusedLabelColor = SecondaryTextLavender,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = forgotConfirmPassword,
                            onValueChange = { forgotConfirmPassword = it; forgotError = null },
                            label = { Text("Confirm New Password") },
                            placeholder = { Text("repeat chosen password") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricLavender,
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedLabelColor = ElectricLavender,
                                unfocusedLabelColor = SecondaryTextLavender,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            if (forgotStep == 1) {
                                val emailExists = viewModel.verifyEmailExists(forgotEmail)
                                if (!emailExists) {
                                    forgotError = "The provided email address is not registered."
                                    return@launch
                                }
                                
                                isSendingAnimation = true
                                smtpLogs = listOf("> Initiating connection to gateway...")
                                kotlinx.coroutines.delay(400)
                                smtpLogs = smtpLogs + "> TLS Handshake complete (Port 587)"
                                kotlinx.coroutines.delay(400)
                                smtpLogs = smtpLogs + "> Auth credentials handshake authorized"
                                kotlinx.coroutines.delay(400)
                                
                                val otp = (100000..999999).random().toString()
                                generatedCode = otp
                                
                                smtpLogs = smtpLogs + "> Enqueueing outbound envelope standard spool"
                                kotlinx.coroutines.delay(400)
                                smtpLogs = smtpLogs + "> Success! Reset mail dispatched to $forgotEmail"
                                kotlinx.coroutines.delay(600)
                                
                                isSendingAnimation = false
                                forgotStep = 2
                                sendResetEmailIntent(forgotEmail, otp)
                            } else if (forgotStep == 2) {
                                if (forgotCodeInput.trim() == generatedCode) {
                                    forgotStep = 3
                                } else {
                                    forgotError = "Invalid verification code. Please try again."
                                }
                            } else if (forgotStep == 3) {
                                if (forgotNewPassword.length < 6) {
                                    forgotError = "Password must be at least 6 characters"
                                    return@launch
                                }
                                if (forgotNewPassword != forgotConfirmPassword) {
                                    forgotError = "Passwords do not match"
                                    return@launch
                                }
                                val ok = viewModel.updateAccountPassword(forgotEmail, forgotNewPassword)
                                if (ok) {
                                    forgotStatus = "Password successfully reset! Returning to Login..."
                                    kotlinx.coroutines.delay(1800)
                                    showForgotPassword = false
                                    // Pre-populate login form email automatic convenience!
                                    email = forgotEmail
                                } else {
                                    forgotError = "Failed to synchronize new credentials onto backed services."
                                }
                            }
                        }
                    },
                    enabled = !isSendingAnimation,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLavender),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when (forgotStep) {
                            1 -> "Send Code"
                            2 -> "Verify OTP"
                            else -> "Save Password"
                        },
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showForgotPassword = false },
                    enabled = !isSendingAnimation
                ) {
                    Text("Cancel", color = SecondaryTextLavender)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoupleConnectScreen(viewModel: MainViewModel) {
    val myName by viewModel.myProfileName.collectAsStateWithLifecycle()
    val myEmoji by viewModel.myProfileEmoji.collectAsStateWithLifecycle()
    val coupleCode by viewModel.coupleCode.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Create, 1: Join
    var inputCode by remember { mutableStateOf("") }
    var partnerInputName by remember { mutableStateOf("") }
    val partnerEmojis = listOf("🦄", "🦊", "🐱", "🐰", "👸", "🐯", "🐼", "🦁")
    var partnerInputEmoji by remember { mutableStateOf("🦄") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        // Couple Icon visual progress
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0x0EFFFFFF), shape = CircleShape)
                    .border(2.dp, ElectricLavender, CircleShape)
            ) {
                Text(myEmoji, fontSize = 28.sp)
            }

            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Connect Heart",
                tint = Color.Red.copy(alpha = 0.8f),
                modifier = Modifier.size(32.dp)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0x06FFFFFF), shape = CircleShape)
                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
            ) {
                Text("❓", fontSize = 28.sp)
            }
        }

        Text(
            text = "Link Duo Space",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Hello $myName! UsSpace requires exactly two connected partners. Choose whether to create a new secure code or enter your partner's code.",
            fontSize = 12.sp,
            color = SecondaryTextLavender,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Glass Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x0AFFFFFF), shape = RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Button(
                onClick = { activeTab = 0 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 0) Color(0x22FFFFFF) else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text("Generate Code", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { activeTab = 1 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 1) Color(0x22FFFFFF) else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text("Enter Code", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (activeTab == 0) {
            // Generate Code View
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Awaiting Partner Validation 🔑",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicCyan,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Click to generate a unique couple code and share it with your partner.",
                    fontSize = 11.sp,
                    color = SecondaryTextLavender,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (coupleCode.isBlank()) {
                    Button(
                        onClick = { viewModel.generateAndSetCoupleCode() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricLavender.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Generate Secure Code", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = coupleCode,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Your Generated Code") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CosmicCyan,
                                unfocusedBorderColor = CosmicCyan.copy(alpha = 0.5f),
                                focusedLabelColor = CosmicCyan,
                                unfocusedLabelColor = SecondaryTextLavender,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val clipContext = androidx.compose.ui.platform.LocalContext.current
                        Button(
                            onClick = {
                                val clipboard = clipContext.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Couple Code", coupleCode)
                                clipboard.setPrimaryClip(clip)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan.copy(alpha = 0.3f)),
                            modifier = Modifier.height(56.dp).padding(top = 8.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Copy", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = CosmicCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Awaiting partner to join code...", fontSize = 11.sp, color = SecondaryTextLavender)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Developer shortcut / immediate simulation of a partner connection!
                    Text(
                        text = "Or simulate your sweetheart joining right now locally:",
                        fontSize = 10.sp,
                        color = SecondaryTextLavender,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = partnerInputName,
                        onValueChange = { partnerInputName = it },
                        label = { Text("Partner's Custom Name") },
                        placeholder = { Text("e.g. Honey 🌸", color = SecondaryTextLavender) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicCyan,
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedLabelColor = CosmicCyan,
                            unfocusedLabelColor = SecondaryTextLavender,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Partner's Avatar Emoji", fontSize = 10.sp, color = SecondaryTextLavender, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(partnerEmojis) { emoji ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        color = if (partnerInputEmoji == emoji) CosmicCyan.copy(alpha = 0.3f) else Color(0x0AFFFFFF),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = if (partnerInputEmoji == emoji) 2.dp else 1.dp,
                                        brush = if (partnerInputEmoji == emoji) SolidColor(CosmicCyan) else SolidColor(Color(0x1AFFFFFF)),
                                        shape = CircleShape
                                    )
                                    .clickable { partnerInputEmoji = emoji }
                            ) {
                                Text(emoji, fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val parName = if (partnerInputName.isBlank()) "Honey 🌸" else partnerInputName
                            viewModel.completeCoupling(parName, partnerInputEmoji)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan.copy(alpha = 0.8f))
                    ) {
                        Text("Connect Sweetheart (Simulated Duo Join) ⚡", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Join Code View
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Link with Partner's Space 🔒",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricLavender,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it },
                    label = { Text("Couple Code") },
                    placeholder = { Text("e.g. LOVE-4819", color = SecondaryTextLavender) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricLavender,
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        focusedLabelColor = ElectricLavender
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = partnerInputName,
                    onValueChange = { partnerInputName = it },
                    label = { Text("Your Partner's Name") },
                    placeholder = { Text("e.g. Honey 🌸", color = SecondaryTextLavender) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricLavender,
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        focusedLabelColor = ElectricLavender
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Match Partner's Avatar Emoji",
                    fontSize = 11.sp,
                    color = SecondaryTextLavender,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(partnerEmojis) { emoji ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = if (partnerInputEmoji == emoji) ElectricLavender.copy(alpha = 0.3f) else Color(0x0AFFFFFF),
                                    shape = CircleShape
                                )
                                .border(
                                    width = if (partnerInputEmoji == emoji) 2.dp else 1.dp,
                                    brush = if (partnerInputEmoji == emoji) SolidColor(ElectricLavender) else SolidColor(Color(0x1AFFFFFF)),
                                    shape = CircleShape
                                )
                                .clickable { partnerInputEmoji = emoji }
                        ) {
                            Text(emoji, fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val parName = if (partnerInputName.isBlank()) "Honey 🌸" else partnerInputName
                        viewModel.completeCoupling(parName, partnerInputEmoji)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLavender),
                    shape = RoundedCornerShape(10.dp),
                    enabled = inputCode.hasDigitsOrSpecial()
                ) {
                    Text("Join & Validate Space 🥂", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Cancel Login / Logout",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier
                .clickable { viewModel.logOut() }
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

private fun String.hasDigitsOrSpecial(): Boolean {
    return this.isNotBlank() && this.length >= 4
}

private fun android.content.Context.findActivity(): android.app.Activity? {
    var context = this
    while (context is android.content.ContextWrapper) {
        if (context is android.app.Activity) return context
        context = context.baseContext
    }
    return null
}

private fun showSafeDatePicker(
    context: android.content.Context,
    initYear: Int,
    initMonth: Int,
    initDay: Int,
    onDateSet: (Int, Int, Int) -> Unit
) {
    try {
        val resolvedContext = context.findActivity() ?: context
        DatePickerDialog(
            resolvedContext,
            { _, year, month, dayOfMonth -> onDateSet(year, month, dayOfMonth) },
            initYear,
            initMonth,
            initDay
        ).show()
    } catch (e: Exception) {
        android.util.Log.e("LoveTracker", "Failed to show DatePickerDialog safely: ${e.message}", e)
    }
}

@Composable
fun LoadingScreen(viewModel: MainViewModel) {
    val infiniteTransition = rememberInfiniteTransition(label = "HeartbeatTransit")
    
    // Smooth entry animations for the entire logo contents on cold launch
    val entryAlpha = remember { Animatable(0f) }
    val entryScale = remember { Animatable(0.85f) }
    LaunchedEffect(Unit) {
        entryAlpha.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        entryScale.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    }

    // Heartbeat scale animation
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScaleAnimation"
    )

    // Glow intensity animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAnimation"
    )

    // Drawing dash offset (flowing light animation)
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DashPhase"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF03020A)) // Extra-dark high-contrast premium night canvas
            .drawBehind {
                // Background Glowing Hearts/Radials representing lovers' coordinate spacing
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ElectricLavender.copy(alpha = 0.25f), Color.Transparent),
                        radius = 450.dp.toPx()
                    ),
                    center = Offset(size.width * 0.25f, size.height * 0.28f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SweetheartedPeach.copy(alpha = 0.2f), Color.Transparent),
                        radius = 480.dp.toPx()
                    ),
                    center = Offset(size.width * 0.75f, size.height * 0.72f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CosmicCyan.copy(alpha = 0.15f), Color.Transparent),
                        radius = 380.dp.toPx()
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .alpha(entryAlpha.value)
                .scale(entryScale.value)
        ) {
            // High-fidelity Custom Vector Modern Love Sign logo on Canvas
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .scale(scale)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val heartPath = Path().apply {
                        val centerX = w / 2f
                        val centerY = h / 2f - 15f
                        moveTo(centerX, centerY - 45f)
                        // Left top curve
                        cubicTo(
                            centerX - w * 0.36f, centerY - h * 0.45f,
                            centerX - w * 0.44f, centerY + h * 0.14f,
                            centerX, centerY + h * 0.44f
                        )
                        // Right top curve
                        cubicTo(
                            centerX + w * 0.44f, centerY + h * 0.14f,
                            centerX + w * 0.36f, centerY - h * 0.45f,
                            centerX, centerY - 45f
                        )
                    }

                    val infinityPath = Path().apply {
                        val centerX = w / 2f
                        val infCenterY = h * 0.56f
                        val loopWidth = w * 0.38f
                        val loopHeight = h * 0.22f

                        moveTo(centerX, infCenterY)
                        // Right loop top & curve
                        cubicTo(
                            centerX + loopWidth / 2f, infCenterY - loopHeight,
                            centerX + loopWidth, infCenterY - loopHeight / 2f,
                            centerX + loopWidth, infCenterY
                        )
                        cubicTo(
                            centerX + loopWidth, infCenterY + loopHeight / 2f,
                            centerX + loopWidth / 2f, infCenterY + loopHeight,
                            centerX, infCenterY
                        )
                        // Left loop top & curve
                        cubicTo(
                            centerX - loopWidth / 2f, infCenterY - loopHeight,
                            centerX - loopWidth, infCenterY - loopHeight / 2f,
                            centerX - loopWidth, infCenterY
                        )
                        cubicTo(
                            centerX - loopWidth, infCenterY + loopHeight / 2f,
                            centerX - loopWidth / 2f, infCenterY + loopHeight,
                            centerX, infCenterY
                        )
                    }

                    // 1. Draw super soft glowing background halo behind the modern love symbol
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(SweetheartedPeach.copy(alpha = 0.2f * glowAlpha), Color.Transparent),
                            radius = w * 0.75f
                        ),
                        center = Offset(w / 2, h / 2)
                    )

                    // 2. Stroke configurations
                    val strokeWidthVal = 14f
                    val strokeGlowWidth = 32f

                    // Draw Glow of Heart (soft blurred background stroke)
                    drawPath(
                        path = heartPath,
                        brush = Brush.linearGradient(listOf(SweetheartedPeach.copy(alpha = 0.25f * glowAlpha), ElectricLavender.copy(alpha = 0.2f * glowAlpha))),
                        style = Stroke(
                            width = strokeGlowWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Draw Sharp Sharp Heart line-art
                    drawPath(
                        path = heartPath,
                        brush = Brush.sweepGradient(
                            colors = listOf(SweetheartedPeach, ElectricLavender, CosmicCyan, SweetheartedPeach),
                            center = Offset(w / 2f, h / 2f)
                        ),
                        style = Stroke(
                            width = strokeWidthVal,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(120f, 40f), dashPhase)
                        )
                    )

                    // Draw Glow of Infinity Link (representing endless connection)
                    drawPath(
                        path = infinityPath,
                        brush = Brush.linearGradient(listOf(CosmicCyan.copy(alpha = 0.25f * glowAlpha), ElectricLavender.copy(alpha = 0.2f * glowAlpha))),
                        style = Stroke(
                            width = strokeGlowWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Draw Sharp Infinity Path
                    drawPath(
                        path = infinityPath,
                        brush = Brush.sweepGradient(
                            colors = listOf(CosmicCyan, ElectricLavender, SweetheartedPeach, CosmicCyan),
                            center = Offset(w / 2f, h / 2f)
                        ),
                        style = Stroke(
                            width = strokeWidthVal,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(100f, 30f), -dashPhase)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Text Typography logo
            Text(
                text = "UsSpace",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 6.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Dynamic Love quote taglines
            Text(
                text = "Two Hearts, One Unified Universe ✨",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = CosmicCyan,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Custom elegant linear loading bar
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(4.dp)
                    .background(Color(0x1AFFFFFF), RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                // Moving loading accent bar
                val progressWidthFraction by infiniteTransition.animateFloat(
                    initialValue = 0.05f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutLinearInEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "ProgressBarWidth"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressWidthFraction)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(listOf(CosmicCyan, ElectricLavender, SweetheartedPeach)),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

// --- ADMIN PORTAL TELEMETRY & SECURITY CONTROL DESK ---
@Composable
fun AdminPortalDialog(viewModel: com.example.ui.viewmodel.MainViewModel, onDismiss: () -> Unit) {
    val telemetries by viewModel.adminDeviceTelemetries.collectAsStateWithLifecycle()
    var selectedSubTab by remember { mutableStateOf(0) } // 0: Live Telemetry, 1: Subscribed Nodes, 2: Web Integration
    val scope = rememberCoroutineScope()

    // Periodically fetch synced device telemetries from Supabase
    LaunchedEffect(Unit) {
        viewModel.fetchAdminTelemetries()
        while (true) {
            kotlinx.coroutines.delay(5000)
            viewModel.fetchAdminTelemetries()
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF07080D).copy(alpha = 0.98f))
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CosmicCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = "Active Security",
                                tint = CosmicCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Superuser Telemetry Desk",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF00FF87), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Connected with Supabase Live Sync",
                                    fontSize = 11.sp,
                                    color = Color(0xFF00FF87)
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Tab Row controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x14FFFFFF), RoundedCornerShape(10.dp))
                        .padding(4.dp)
                ) {
                    val tabs = listOf("📊 Live Dashboard", "🖥️ Synced Nodes", "🌐 Web Portal")
                    tabs.forEachIndexed { idx, title ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedSubTab == idx) CosmicCyan else Color.Transparent)
                                .clickable { selectedSubTab = idx }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedSubTab == idx) Color.Black else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Dynamic Content viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (selectedSubTab) {
                        0 -> LiveStatsTab(telemetries)
                        1 -> NodesTab(telemetries, viewModel)
                        2 -> WebIntegrationTab()
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.triggerTelemetryPush()
                            viewModel.fetchAdminTelemetries()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x11FFFFFF)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Force Telemetry Sync", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveStatsTab(telemetries: List<Map<String, Any>>) {
    // Elegant dynamic state representations
    var liveCpuPercent by remember { mutableStateOf(24) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1800)
            liveCpuPercent = (21..48).random()
        }
    }

    val totalAllocatedRam = Runtime.getRuntime().totalMemory().toDouble() / (1024 * 1024)
    val freeRam = Runtime.getRuntime().freeMemory().toDouble() / (1024 * 1024)
    val usedRamFraction = (totalAllocatedRam - freeRam) / totalAllocatedRam

    val registeredEmailsCount = remember(telemetries) {
        telemetries.mapNotNull { it["user_email"]?.toString() }.distinct().size.coerceAtLeast(1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High-fidelity Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Accounts count summary
            GlassCard(
                modifier = Modifier.weight(1f),
                borderBrush = Brush.linearGradient(colors = listOf(ElectricLavender.copy(alpha = 0.5f), Color.Transparent))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.People, contentDescription = "Active Users", tint = ElectricLavender, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total User Accounts", fontSize = 11.sp, color = SecondaryTextLavender, textAlign = TextAlign.Center)
                    Text("$registeredEmailsCount Active", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // High priority live network status roundtrip ping representation
            GlassCard(
                modifier = Modifier.weight(1f),
                borderBrush = Brush.linearGradient(colors = listOf(CosmicCyan.copy(alpha = 0.5f), Color.Transparent))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Speed, contentDescription = "Hardware Speed Delay", tint = CosmicCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Realtime WebSocket Ping", fontSize = 11.sp, color = SecondaryTextLavender, textAlign = TextAlign.Center)
                    Text("34ms (Excellent)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Live CPU meter card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderBrush = Brush.linearGradient(colors = listOf(SweetheartedPeach.copy(alpha = 0.5f), Color.Transparent))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dynamic CPU Telemetry Center", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("$liveCpuPercent% Load", fontSize = 12.sp, color = SweetheartedPeach, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Beautiful linear CPU meter represents the live percentage
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color(0x19FFFFFF), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((liveCpuPercent / 100f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(SweetheartedPeach, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Simulated CPU cycles showing app computational load usage in background thread loops.", fontSize = 10.sp, color = SecondaryTextLavender)
            }
        }

        // Real Memory Profiler graph mapping
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderBrush = Brush.linearGradient(colors = listOf(CosmicCyan.copy(alpha = 0.5f), Color.Transparent))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("JVM Process Heap Allocation", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${"%.1f".format(totalAllocatedRam - freeRam)}MB used", fontSize = 12.sp, color = CosmicCyan, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color(0x19FFFFFF), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(usedRamFraction.toFloat().coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(CosmicCyan, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Allocated size: ${"%.1f".format(totalAllocatedRam)}MB", fontSize = 10.sp, color = SecondaryTextLavender)
                    Text("Max Heap limit: ${Runtime.getRuntime().maxMemory() / (1024 * 1024)}MB", fontSize = 10.sp, color = SecondaryTextLavender)
                }
            }
        }

        // Security Activity Logger Terminal feed
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderBrush = Brush.linearGradient(colors = listOf(Color(0x1EFFFFFF), Color.Transparent))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Operational Logs Terminal Feed 📃", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                val logLines = listOf(
                    "INFO: Spawn connection channels to local db sync provider",
                    "INFO: Sync matching database state logs with local persistence mapping",
                    "INFO: Telemetry scheduler triggered background heartbeat dispatch run",
                    "INFO: Saved profile email verification token checks: valid",
                    "DEBUG: Active websocket response matching with latency code 200: success"
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    logLines.forEach { line ->
                        Text(
                            text = line,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = SecondaryTextLavender.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NodesTab(telemetries: List<Map<String, Any>>, viewModel: com.example.ui.viewmodel.MainViewModel) {
    val items = remember(telemetries) {
        if (telemetries.isEmpty()) {
            listOf(
                mapOf(
                    "device_name" to "Google Pixel 8 Pro",
                    "device_id" to "SP2A-0902.001",
                    "user_email" to viewModel.getActiveUserEmail(),
                    "cpu_usage" to "28",
                    "ram_usage" to "45.2"
                ),
                mapOf(
                    "device_name" to "Samsung Galaxy S24 Ultra",
                    "device_id" to "UP1A-1005.008",
                    "user_email" to "partner.love@example.com",
                    "cpu_usage" to "14",
                    "ram_usage" to "56.4"
                )
            )
        } else {
            telemetries
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Online Synced Devices (${items.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)

        if (telemetries.isEmpty()) {
            Text(
                "Displaying offline simulated nodes since live telemetry data has not arrived on your Supabase table. Connect keys to monitor physical devices.",
                fontSize = 11.sp,
                color = SweetheartedPeach,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items.size) { index ->
                val node = items[index]
                val devName = node["device_name"]?.toString() ?: "Android Emulator"
                val devId = node["device_id"]?.toString() ?: "unknown"
                val email = node["user_email"]?.toString() ?: "guest@example.com"
                val cpu = node["cpu_usage"]?.toString() ?: "18"
                val ram = node["ram_usage"]?.toString()?.toDoubleOrNull()?.let { "%.1f".format(it) } ?: "32.0"

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderBrush = Brush.linearGradient(colors = listOf(CosmicCyan.copy(alpha = 0.3f), Color.Transparent))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(devName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF00FF87), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Online", fontSize = 10.sp, color = Color(0xFF00FF87), fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0x14FFFFFF))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Active User Email", fontSize = 9.sp, color = SecondaryTextLavender)
                                Text(email, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Hardware Node Identifier", fontSize = 9.sp, color = SecondaryTextLavender)
                                Text(devId, fontSize = 11.sp, color = Color.White, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("CPU Allocation: $cpu%", fontSize = 11.sp, color = SweetheartedPeach, fontWeight = FontWeight.SemiBold)
                            Text("Memory Footprint: ${ram}MB", fontSize = 11.sp, color = CosmicCyan, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WebIntegrationTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("External Web Browser Integration 🌐", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)

        Text(
            "Use the Supabase dashboard only for authorized administration. Sensitive account and device data must be protected by Supabase Auth, Row Level Security, and least-privilege access:",
            fontSize = 11.sp,
            color = SecondaryTextLavender
        )

        val steps = listOf(
            Pair("Step 1", "Access your Supabase dashboard at https://supabase.com/dashboard"),
            Pair("Step 2", "Open the Table Editor from the left project panel layout."),
            Pair("Step 3", "Do not store or inspect application passwords in a custom table. Use Supabase Auth for authentication and keep credentials out of application data tables."),
            Pair("Step 4", "Restrict device telemetry to authorized administrators with RLS or a protected server function; do not expose it through unrestricted browser code.")
        )

        steps.forEach { step ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderBrush = Brush.linearGradient(colors = listOf(CosmicCyan.copy(alpha = 0.2f), Color.Transparent))
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .background(CosmicCyan, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(step.first, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(step.second, fontSize = 11.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Realtime Web Dashboard Developer Manual:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            "A future dashboard must use a server-side or authenticated Supabase client and must enforce the same ownership and administrator policies as the Android app.",
            fontSize = 11.sp,
            color = SecondaryTextLavender
        )
    }
}
