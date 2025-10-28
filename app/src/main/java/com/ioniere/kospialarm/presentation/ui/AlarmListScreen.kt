package com.ioniere.kospialarm.presentation.ui

import androidx.compose.foundation.layout.*
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
import com.ioniere.kospialarm.domain.model.Alarm
import com.ioniere.kospialarm.domain.model.AlarmType
import com.ioniere.kospialarm.domain.model.KospiData
import com.ioniere.kospialarm.presentation.viewmodel.AlarmListUiState
import com.ioniere.kospialarm.presentation.viewmodel.AlarmListViewModel

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
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "알림 추가")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 코스피 현재 정보
            KospiInfoCard(kospiData, onRefresh = { viewModel.loadKospiData() })

            // 알림 목록
            when (val state = uiState) {
                is AlarmListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is AlarmListUiState.Success -> {
                    if (state.alarms.isEmpty()) {
                        EmptyAlarmList()
                    } else {
                        AlarmList(
                            alarms = state.alarms,
                            onDeleteAlarm = { viewModel.deleteAlarm(it) }
                        )
                    }
                }

                is AlarmListUiState.Error -> {
                    ErrorMessage(state.message)
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
 * 코스피 정보 카드.
 *
 * @param kospiData 코스피 데이터
 * @param onRefresh 새로고침 콜백
 */
@Composable
fun KospiInfoCard(
    kospiData: KospiData?,
    onRefresh: () -> Unit
) {
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
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "변동: ${String.format("%.2f", kospiData.changePercent)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (kospiData.changePercent >= 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            } else {
                Text("코스피 데이터를 불러오는 중...")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRefresh) {
                Text("새로고침")
            }
        }
    }
}

/**
 * 알림 목록.
 *
 * @param alarms 알림 목록
 * @param onDeleteAlarm 삭제 콜백
 */
@Composable
fun AlarmList(
    alarms: List<Alarm>,
    onDeleteAlarm: (Alarm) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(alarms) { alarm ->
            AlarmCard(alarm = alarm, onDelete = { onDeleteAlarm(alarm) })
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
    Card(
        modifier = Modifier.fillMaxWidth()
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
                Text(
                    text = "${alarm.percentage}% $typeText",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "기준: ${String.format("%.2f", alarm.baseValue)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            TextButton(onClick = onDelete) {
                Text("삭제")
            }
        }
    }
}

/**
 * 빈 알림 목록 메시지.
 */
@Composable
fun EmptyAlarmList() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("설정된 알림이 없습니다")
    }
}

/**
 * 에러 메시지.
 *
 * @param message 에러 메시지
 */
@Composable
fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
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
                    listOf(5, 10, 15, 20).forEach { percentage ->
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
