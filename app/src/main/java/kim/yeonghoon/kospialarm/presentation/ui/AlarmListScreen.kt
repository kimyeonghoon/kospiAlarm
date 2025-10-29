package kim.yeonghoon.kospialarm.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kim.yeonghoon.kospialarm.domain.model.Alarm
import kim.yeonghoon.kospialarm.domain.model.AlarmType
import kim.yeonghoon.kospialarm.domain.model.KospiData
import kim.yeonghoon.kospialarm.presentation.viewmodel.AlarmListUiState
import kim.yeonghoon.kospialarm.presentation.viewmodel.AlarmListViewModel
/**
 * 알림 목록 화면.
 *
 * @param viewModel AlarmListViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    viewModel: AlarmListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val kospiData by viewModel.kospiData.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    // 현재 알림 개수 확인
    val alarmCount = when (val state = uiState) {
        is AlarmListUiState.Success -> state.alarms.size
        else -> 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KOSPI 알림") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            // 알림이 2개 미만일 때만 표시
            if (alarmCount < 2) {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "알림 추가")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 투자 전략 가이드
            item {
                StrategyGuideCard()
            }

            // 코스피 현재 정보
            item {
                KospiInfoCard(kospiData = kospiData)
            }

            // 알림 목록
            when (val state = uiState) {
                is AlarmListUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is AlarmListUiState.Success -> {
                    if (state.alarms.isEmpty()) {
                        item {
                            EmptyAlarmListContent()
                        }
                    } else {
                        items(state.alarms) { alarm ->
                            AlarmCard(
                                alarm = alarm,
                                onDelete = { viewModel.deleteAlarm(alarm) }
                            )
                        }
                    }
                }

                is AlarmListUiState.Error -> {
                    item {
                        ErrorMessageContent(state.message)
                    }
                }
            }
        }

        // 알림 추가 다이얼로그
        if (showDialog) {
            AddAlarmDialog(
                currentKospi = kospiData?.index ?: 0.0,
                onDismiss = { showDialog = false },
                onConfirm = { alarm ->
                    viewModel.createAlarm(alarm)
                    showDialog = false
                }
            )
        }
    }
}

/**
 * 투자 전략 가이드 카드.
 */
@Composable
fun StrategyGuideCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "💡 투자 전략 가이드",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "• 매일 인버스 적립: 3,000원",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "• 롱 포지션: 절대 매도 금지",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "• 조정 시: 추가 매수 자금 활용",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "✓ 인버스 = 헷지 목적 (함부로 안 팔게 됨)\n✓ 롱 100% 유지 = 타이밍 놓칠 걱정 없음\n✓ 소액 루틴 = 충동적 결정 방지",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * 코스피 정보 카드.
 *
 * @param kospiData 코스피 데이터
 */
@Composable
fun KospiInfoCard(kospiData: KospiData?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (kospiData != null) {
                Text(
                    text = "KOSPI ${String.format("%.2f", kospiData.index)}",
                    style = MaterialTheme.typography.displaySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "변동: ${String.format("%.2f", kospiData.changePercent)}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (kospiData.changePercent >= 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            } else {
                Text("코스피 데이터를 불러오는 중...")
            }
        }
    }
}


/**
 * 알림 카드.
 *
 * @param alarm 알림
 * @param onDelete 삭제 콜백
 */
@Composable
fun AlarmCard(
    alarm: Alarm,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val typeText = if (alarm.type == AlarmType.RISE) "상승" else "하락"
                val targetValue = if (alarm.type == AlarmType.RISE) {
                    alarm.baseValue * (1 + alarm.percentage / 100.0)
                } else {
                    alarm.baseValue * (1 - alarm.percentage / 100.0)
                }

                Text(
                    text = "${alarm.percentage}% $typeText (${String.format("%.2f", targetValue)})",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "기준: ${String.format("%.2f", alarm.baseValue)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = { showDeleteDialog = true }) {
                Text("삭제")
            }
        }
    }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("알림 삭제") },
            text = {
                val typeText = if (alarm.type == AlarmType.RISE) "상승" else "하락"
                Text("${alarm.percentage}% $typeText 알림을 삭제하시겠습니까?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

/**
 * 빈 알림 목록 메시지 (LazyColumn item용).
 */
@Composable
fun EmptyAlarmListContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("설정된 알림이 없습니다")
    }
}

/**
 * 에러 메시지 (LazyColumn item용).
 *
 * @param message 에러 메시지
 */
@Composable
fun ErrorMessageContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("오류: $message", color = MaterialTheme.colorScheme.error)
    }
}

/**
 * 알림 추가 다이얼로그.
 *
 * @param currentKospi 현재 코스피 값
 * @param onDismiss 취소 콜백
 * @param onConfirm 확인 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmDialog(
    currentKospi: Double,
    onDismiss: () -> Unit,
    onConfirm: (Alarm) -> Unit
) {
    var selectedPercentage by remember { mutableStateOf(5) }
    var selectedType by remember { mutableStateOf(AlarmType.RISE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("알림 추가") },
        text = {
            Column {
                Text("현재 코스피: ${String.format("%.2f", currentKospi)}")
                Spacer(modifier = Modifier.height(16.dp))

                Text("퍼센트 선택:")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10).forEach { percentage ->
                        FilterChip(
                            selected = selectedPercentage == percentage,
                            onClick = { selectedPercentage = percentage },
                            label = { Text("$percentage%") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("타입 선택:")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == AlarmType.RISE,
                        onClick = { selectedType = AlarmType.RISE },
                        label = { Text("상승") }
                    )
                    FilterChip(
                        selected = selectedType == AlarmType.FALL,
                        onClick = { selectedType = AlarmType.FALL },
                        label = { Text("하락") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val alarm = Alarm(
                        baseValue = currentKospi,
                        percentage = selectedPercentage,
                        type = selectedType
                    )
                    onConfirm(alarm)
                }
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
