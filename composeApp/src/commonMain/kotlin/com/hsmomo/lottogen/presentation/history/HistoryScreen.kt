package com.hsmomo.lottogen.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hsmomo.lottogen.domain.model.WinningDraw
import com.hsmomo.lottogen.presentation.components.LottoBall
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryScreen(
    onShowMessage: (String) -> Unit,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is HistoryContract.Effect.ShowError -> onShowMessage(effect.message)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "당첨 히스토리",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.sendIntent(HistoryContract.Intent.UpdateSearchQuery(it)) },
            label = { Text("번호 검색") },
            placeholder = { Text("예: 3,12,19") },
            leadingIcon = {
                IconButton(onClick = {
                    viewModel.sendIntent(HistoryContract.Intent.Search)
                    keyboardController?.hide()
                }) {
                    Icon(Icons.Default.Search, contentDescription = "검색")
                }
            },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.sendIntent(HistoryContract.Intent.ClearSearch) }) {
                        Icon(Icons.Default.Clear, contentDescription = "지우기")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                viewModel.sendIntent(HistoryContract.Intent.Search)
                keyboardController?.hide()
            }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (state.isSearchActive && state.searchNumbers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "검색된 번호: ${state.searchNumbers.joinToString(", ")} 를 모두 포함한 회차",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            state.isLoading -> {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                }
            }
            state.displayDraws.isEmpty() -> {
                Text(
                    text = if (state.isSearchActive) "검색 결과가 없습니다" else "당첨 데이터가 없습니다",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(32.dp)
                )
            }
            else -> {
                Text(
                    text = "총 ${state.displayDraws.size}개 회차",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.displayDraws, key = { it.drawNo }) { draw ->
                        DrawHistoryItem(draw = draw)
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawHistoryItem(draw: WinningDraw) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${draw.drawNo}회차", style = MaterialTheme.typography.titleMedium)
                Text(text = draw.drawDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                draw.numbers.forEach { number -> LottoBall(number = number, size = 32.dp) }
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "+", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                LottoBall(number = draw.bonus, size = 32.dp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "1등 당첨금: ${draw.formattedPrizeAmount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}