
package com.example.welllife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.welllife.data.WellLifeDatabase
import com.example.welllife.data.repository.DashboardRepositoryImpl
import com.example.welllife.ui.activity.ActivityScreen
import com.example.welllife.ui.activity.ActivityViewModel
import com.example.welllife.ui.activity.ActivityViewModelFactory
import com.example.welllife.ui.dashboard.DashboardUiState
import com.example.welllife.ui.dashboard.DashboardViewModel
import com.example.welllife.ui.dashboard.DashboardViewModelFactory
import com.example.welllife.ui.goals.GoalsScreen
import com.example.welllife.ui.progress.ProgressScreen
import com.example.welllife.ui.hydration.HydrationScreen
import com.example.welllife.ui.hydration.HydrationViewModel
import com.example.welllife.ui.theme.WelllifeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WelllifeTheme {
                WellLifeApp()
            }
        }
    }
}

private enum class Screen(
    val title: String,
    val description: String
) {
    HYDRATION("Hydration", "Log and review your water intake"),
    SLEEP("Sleep", "Review your sleep duration and history"),
    ACTIVITY("Physical activity", "Log exercise and review daily minutes"),
    GOALS("Wellness goals", "Set personal targets and reminders"),
    HISTORY("Progress and history", "Review your weekly wellness trends")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WellLifeApp() {

    var selectedScreenName by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val selectedScreen = selectedScreenName?.let {
        Screen.valueOf(it)
    }

    val context = LocalContext.current

    val database = remember(context) {
        WellLifeDatabase.getDatabase(context)
    }

    val repository = remember(database) {
        DashboardRepositoryImpl(
            database.waterDao(),
            database.sleepDao(),
            database.activityDao(),
            database.wellnessGoalDao()
        )
    }

    val activity = context as ComponentActivity

    val viewModel = remember(activity, repository) {
        ViewModelProvider(
            activity,
            DashboardViewModelFactory(repository)
        )[DashboardViewModel::class.java]
    }

    // Create HydrationViewModel with both required DAOs
    val hydrationViewModel = remember(activity, database) {
        ViewModelProvider(
            activity,
            object : ViewModelProvider.Factory {

                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {
                    if (modelClass.isAssignableFrom(
                            HydrationViewModel::class.java
                        )
                    ) {
                        @Suppress("UNCHECKED_CAST")
                        return HydrationViewModel(
                            database.waterDao(),
                            database.wellnessGoalDao()
                        ) as T
                    }

                    throw IllegalArgumentException(
                        "Unknown ViewModel class"
                    )
                }
            }
        )[HydrationViewModel::class.java]
    }

    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = selectedScreen != null) {
        selectedScreenName = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(selectedScreen?.title ?: "WellLife")
                },
                navigationIcon = {
                    if (selectedScreen != null) {
                        TextButton(
                            onClick = {
                                selectedScreenName = null
                            }
                        ) {
                            Text("Back")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->

        val contentModifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()

        if (selectedScreen == null) {

            DashboardScreen(
                modifier = contentModifier,
                uiState = uiState,
                onOpenScreen = { screen ->
                    selectedScreenName = screen.name
                }
            )

        } else if (selectedScreen == Screen.HYDRATION) {

            HydrationScreen(
                viewModel = hydrationViewModel,
                modifier = contentModifier
            )

        } else {
            FeatureScreen(
                screen = selectedScreen,
                modifier = contentModifier,
                uiState = uiState,
                repository = repository,
                database = database,
                activity = activity
            )
        }
    }
}

@Composable
private fun DashboardScreen(
    modifier: Modifier,
    uiState: DashboardUiState,
    onOpenScreen: (Screen) -> Unit
) {

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Your wellness overview",
            style = MaterialTheme.typography.headlineSmall
        )

        when (uiState) {

            is DashboardUiState.Loading -> {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DashboardUiState.Error -> {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Error loading dashboard: ${uiState.message}",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            is DashboardUiState.Success -> {

                val data = uiState.data

                Screen.entries.forEach { screen ->

                    Card(
                        onClick = {
                            onOpenScreen(screen)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {

                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.titleMedium
                            )

                            val summaryText = when (screen) {
                                Screen.HYDRATION -> data.waterSummary
                                Screen.SLEEP -> data.sleepSummary
                                Screen.ACTIVITY -> data.activitySummary
                                Screen.GOALS -> data.goalsSummary
                                Screen.HISTORY ->
                                    "Tracked activity & trend summary"
                            }

                            Text(
                                text = summaryText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = screen.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureScreen(
    screen: Screen,
    modifier: Modifier,
    uiState: DashboardUiState,
    repository: com.example.welllife.data.repository.DashboardRepository,
    database: WellLifeDatabase,
    activity: ComponentActivity
) {
    when (screen) {
        Screen.ACTIVITY -> {
            val activityViewModel = remember(activity, database) {
                ViewModelProvider(
                    activity,
                    ActivityViewModelFactory(database.activityDao())
                )[ActivityViewModel::class.java]
            }
            ActivityScreen(viewModel = activityViewModel, modifier = modifier)
        }
        Screen.GOALS -> {
            val data = (uiState as? DashboardUiState.Success)?.data
            if (data == null) {
                Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                GoalsScreen(data.goals, repository, modifier)
            }
        }
        Screen.HISTORY -> {
            val data = (uiState as? DashboardUiState.Success)?.data
            if (data == null) {
                Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                ProgressScreen(data.sevenDayProgress, modifier)
            }
        }
        else -> Column(
            modifier = modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(screen.description, style = MaterialTheme.typography.titleLarge)
            Text("This feature is being built.")
        }
    }
}
