package com.hsmomo.lottogen.presentation.generate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hsmomo.lottogen.domain.usecase.GenerationType
import com.hsmomo.lottogen.presentation.components.LottoBall
import com.hsmomo.lottogen.presentation.components.NumberSetRow
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GenerateScreen(
    onShowMessage: (String) -> Unit,
    viewModel: GenerateViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is GenerateContract.Effect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (state.isMixedMode) {
            TopAppBar(
                title = { Text("혼합선택") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.sendIntent(GenerateContract.Intent.ExitMixedMode) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )

            Text(
                text = "번호를 선택하세요 (최대 6개)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "선택된 번호: ${state.selectedNumbers.size}/6",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..45).forEach { number ->
                    LottoBall(
                        number = number,
                        size = 44.dp,
                        isSelectable = true,
                        isSelected = number in state.selectedNumbers,
                        onClick = { viewModel.sendIntent(GenerateContract.Intent.ToggleNumber(number)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.sendIntent(GenerateContract.Intent.ResetSelection) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("초기화")
                }

                Button(
                    onClick = { viewModel.sendIntent(GenerateContract.Intent.GenerateMixed) },
                    enabled = state.canGenerateMixed && !state.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("발행하기")
                    }
                }
            }
        } else {
            Text(
                text = "번호 발행",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.sendIntent(GenerateContract.Intent.Generate(GenerationType.RECOMMENDED)) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = !state.isLoading
                ) { Text("추천번호") }

                Button(
                    onClick = { viewModel.sendIntent(GenerateContract.Intent.Generate(GenerationType.HIGH_PROBABILITY)) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = !state.isLoading
                ) { Text("높은확률") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.sendIntent(GenerateContract.Intent.Generate(GenerationType.LOW_PROBABILITY)) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = !state.isLoading
                ) { Text("낮은확률") }

                OutlinedButton(
                    onClick = { viewModel.sendIntent(GenerateContract.Intent.EnterMixedMode) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = !state.isLoading
                ) { Text("혼합선택") }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading && state.generatedSets.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("번호 생성 중...")
            }
        } else if (state.generatedSets.isNotEmpty()) {
            Text(
                text = "생성된 번호",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.generatedSets, key = { it.numberSet.numbersSortedText }) { setWithBookmark ->
                    NumberSetRow(
                        numbers = setWithBookmark.numberSet.sortedNumbers,
                        isBookmarked = setWithBookmark.isBookmarked,
                        onBookmarkToggle = {
                            viewModel.sendIntent(GenerateContract.Intent.ToggleBookmark(setWithBookmark.numberSet))
                        }
                    )
                }
            }
        } else if (!state.isMixedMode) {
            Text(
                text = "버튼을 눌러 번호를 생성하세요",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(32.dp)
            )
        }
    }
}