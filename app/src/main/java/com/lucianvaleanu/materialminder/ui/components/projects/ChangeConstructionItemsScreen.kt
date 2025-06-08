package com.lucianvaleanu.materialminder.ui.components.projects

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.lucianvaleanu.materialminder.model.ConstructionItem
import com.lucianvaleanu.materialminder.model.ProjectItem
import com.lucianvaleanu.materialminder.model.ProjectItemDraft
import com.lucianvaleanu.materialminder.service.speech.SpeechToTextService
import com.lucianvaleanu.materialminder.ui.components.construction_item.ConstructionItemCardWithCount
import java.math.BigDecimal
import java.text.Normalizer

private fun convertRomanianNumberWordToIntChange(word: String): Int? {
    return when (word.lowercase().replace("ă", "a").replace("â", "a").replace("î", "i")
        .replace("ș", "s").replace("ț", "t")) {
        "unu", "una", "o" -> 1
        "doi", "doua" -> 2
        "trei" -> 3
        "patru" -> 4
        "cinci" -> 5
        "sase" -> 6
        "sapte" -> 7
        "opt" -> 8
        "noua" -> 9
        "zece" -> 10
        "unsprezece" -> 11
        "doisprezece" -> 12
        "treisprezece" -> 13
        "paisprezece" -> 14
        "cincisprezece" -> 15
        "saisprezece" -> 16
        "saptesprezece" -> 17
        "optsprezece" -> 18
        "nouasprezece" -> 19
        "douazeci" -> 20
        else -> word.toIntOrNull()
    }
}

private fun normalizeItemName(name: String): String {
    val nfdNormalizedString = Normalizer.normalize(name, Normalizer.Form.NFD)
    val nonSpacingMarksRegex = "\\p{Mn}".toRegex()
    return nonSpacingMarksRegex.replace(nfdNormalizedString, "").lowercase().trim()
}


internal fun parseSpokenItemsChange(text: String): List<Pair<String, Int>> {
    val parsedItems = mutableListOf<Pair<String, Int>>()
    val regex =
        "(.*?)\\s+([a-zA-ZăâîșțĂÂÎȘȚ0-9]+(?:\\s+[a-zA-ZăâîșțĂÂÎȘȚ0-9]+)*?)\\s+bucăți\\b".toRegex(
            RegexOption.IGNORE_CASE
        )
    var remainingText = text

    var matchResult = regex.find(remainingText)
    while (matchResult != null) {
        val rawItemName = matchResult.groupValues[1].trim()
        val quantityWord = matchResult.groupValues[2].trim()
        val quantity =
            convertRomanianNumberWordToIntChange(quantityWord) ?: 1

        if (rawItemName.isNotEmpty()) {
            parsedItems.add(rawItemName to quantity)
        }
        if (matchResult.range.last + 1 < remainingText.length) {
            remainingText = remainingText.substring(matchResult.range.last + 1)
            matchResult = regex.find(remainingText)
        } else {
            matchResult = null
        }
    }

    if (parsedItems.isEmpty() && text.isNotBlank()) {
        val simplifiedRegex = "(.*?)\\s+bucăți\\b".toRegex(RegexOption.IGNORE_CASE)
        val simpleMatch = simplifiedRegex.find(text)
        if (simpleMatch != null) {
            val itemName = simpleMatch.groupValues[1].trim()
            if (itemName.isNotEmpty()) {
                parsedItems.add(itemName to 1)
            }
        } else {
            val trimmedText = text.trim()
            if (trimmedText.isNotEmpty()) {
                parsedItems.add(trimmedText to 1)
            }
        }
    }
    Log.d("SpeechParseChange", "Input text: '$text'. Parsed into: $parsedItems")
    return parsedItems
}


@Composable
fun ChangeConstructionItemsScreen(
    navController: NavController,
    constructionObjectsList: List<ConstructionItem>,
    initialProjectItems: List<ProjectItem>,
    onCancel: () -> Unit,
    currentSearchQuery: String,
    onSearchQueryChanged: (String) -> Unit
) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf(currentSearchQuery) }
    val locallyAddedItems = remember { mutableStateListOf<ConstructionItem>() }

    LaunchedEffect(currentSearchQuery) {
        if (searchText != currentSearchQuery) {
            searchText = currentSearchQuery
        }
    }

    val selectedItems: SnapshotStateMap<Any, Int> = remember(initialProjectItems) {
        mutableStateMapOf<Any, Int>().apply {
            initialProjectItems.forEach { projectItem ->
                put(projectItem.itemId, projectItem.quantity)
            }
        }
    }

    var isRecording by remember { mutableStateOf(false) }
    var speechService by remember { mutableStateOf<SpeechToTextService?>(null) }
    var isBound by remember { mutableStateOf(false) }

    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { Log.d("SpeechToTextChange", "Ready for speech") }
            override fun onBeginningOfSpeech() { Log.d("SpeechToTextChange", "Beginning of speech") }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { Log.d("SpeechToTextChange", "End of speech") }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                    SpeechRecognizer.ERROR_SERVER -> "Error from server"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown speech recognition error"
                }
                Log.e("SpeechToTextChange", "Error: $errorMessage (code: $error)")
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                isRecording = false
            }

            override fun onResults(results: Bundle?) {
                val allRecognizedText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.joinToString(" ") ?: ""
                Log.d("SpeechToTextChange", "Raw recognized text: $allRecognizedText")

                if (allRecognizedText.isNotBlank()) {
                    val parsedSpokenItemsFromText = parseSpokenItemsChange(allRecognizedText)
                    Log.d("SpeechToTextChange", "Parsed items from text: $parsedSpokenItemsFromText")

                    if (parsedSpokenItemsFromText.isNotEmpty()) {
                        val newSelectedItemsMap = mutableStateMapOf<Any, Int>().apply { putAll(selectedItems) }
                        val currentLocallyAddedList = locallyAddedItems.toMutableList()
                        var madeChanges = false

                        parsedSpokenItemsFromText.forEach { (spokenName, spokenQuantity) ->
                            val normalizedSpokenName = normalizeItemName(spokenName)
                            val existingItemInGlobalList = constructionObjectsList.find {
                                normalizeItemName(it.name) == normalizedSpokenName && it.id != null
                            }

                            if (existingItemInGlobalList != null) {
                                val itemId = existingItemInGlobalList.id!!
                                newSelectedItemsMap[itemId] = spokenQuantity
                                newSelectedItemsMap.remove(spokenName)
                                newSelectedItemsMap.remove(normalizedSpokenName)
                                currentLocallyAddedList.removeIf { la -> normalizeItemName(la.name) == normalizedSpokenName && la.id == null }
                                madeChanges = true
                            } else {
                                val existingLocallyAddedItem = currentLocallyAddedList.find {
                                    normalizeItemName(it.name) == normalizedSpokenName && it.id == null
                                }
                                if (existingLocallyAddedItem != null) {
                                    newSelectedItemsMap[existingLocallyAddedItem.name] = spokenQuantity
                                    madeChanges = true
                                } else {
                                    val newItem = ConstructionItem(
                                        name = spokenName,
                                        price = BigDecimal.ZERO,
                                        image = "",
                                        id = null
                                    )
                                    currentLocallyAddedList.add(newItem)
                                    newSelectedItemsMap[spokenName] = spokenQuantity
                                    madeChanges = true
                                }
                            }
                        }

                        if (madeChanges) {
                            selectedItems.clear()
                            selectedItems.putAll(newSelectedItemsMap)
                            locallyAddedItems.clear()
                            locallyAddedItems.addAll(currentLocallyAddedList.distinctBy { normalizeItemName(it.name) })
                        }
                    } else {
                        Toast.makeText(context, "Could not understand items and quantities.", Toast.LENGTH_LONG).show()
                    }
                }
                isRecording = false
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as SpeechToTextService.SpeechToTextBinder
                speechService = binder.getService()
                isBound = true
            }
            override fun onServiceDisconnected(arg0: ComponentName) {
                speechService = null
                isBound = false
                if (isRecording) isRecording = false
            }
        }
    }
    LaunchedEffect(Unit) {
        Intent(context, SpeechToTextService::class.java).also { intent ->
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            if (isBound) {
                speechService?.stopListening()
                try {
                    context.unbindService(serviceConnection)
                } catch (e: IllegalArgumentException) {
                    Log.w("ChangeItemsScreen", "Service not registered or already unbound: $e")
                }
                isBound = false
            }
            isRecording = false
        }
    }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            if (isBound && speechService != null) {
                speechService?.startListening(recognitionListener)
            } else {
                isRecording = false
                Toast.makeText(context, "Speech service not ready. Try again.", Toast.LENGTH_SHORT).show()
            }
        } else {
            isRecording = false
            Toast.makeText(context, "Microphone permission denied.", Toast.LENGTH_SHORT).show()
        }
    }
    fun startRecordingInternal() {
        if (!isBound || speechService == null) {
            Toast.makeText(context, "Speech service not available.", Toast.LENGTH_SHORT).show()
            isRecording = false
            return
        }
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)) {
            PackageManager.PERMISSION_GRANTED -> speechService?.startListening(recognitionListener)
            else -> requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    fun handleMicButtonClick() {
        if (isRecording) {
            speechService?.stopListening()
        } else {
            isRecording = true
            startRecordingInternal()
        }
    }

    val displayedList = remember(constructionObjectsList, locallyAddedItems.toList()) {
        val globalItemNormalizedNames = constructionObjectsList.map { normalizeItemName(it.name) }.toSet()
        val uniqueLocallyAdded = locallyAddedItems.filter {
            !globalItemNormalizedNames.contains(normalizeItemName(it.name))
        }.distinctBy { normalizeItemName(it.name) }

        constructionObjectsList + uniqueLocallyAdded
    }

    val filteredList = remember(displayedList, searchText) {
        if (searchText.isBlank()) {
            displayedList
        } else {
            val normalizedSearchText = normalizeItemName(searchText)
            displayedList.filter {
                normalizeItemName(it.name).contains(normalizedSearchText)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { newText ->
                    searchText = newText
                    onSearchQueryChanged(newText)
                },
                label = { Text("Search materials") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredList.chunked(2), key = { row -> row.joinToString { item -> (item.id ?: normalizeItemName(item.name)).toString() } }) { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { constructionObject ->
                        val itemKey: Any = constructionObject.id ?: constructionObject.name
                        val currentQuantity = selectedItems[itemKey] ?: 0
                        Box(modifier = Modifier.weight(1f).padding(4.dp)) {
                            ConstructionItemCardWithCount(
                                constructionItem = constructionObject,
                                navController = navController,
                                modifier = Modifier,
                                initialCount = currentQuantity,
                                onCountChange = { newCount ->
                                    val keyForUpdate: Any = constructionObject.id ?: constructionObject.name
                                    val newMap = mutableStateMapOf<Any, Int>().apply { putAll(selectedItems) }
                                    if (newCount > 0) {
                                        newMap[keyForUpdate] = newCount
                                    } else {
                                        newMap.remove(keyForUpdate)
                                        if (constructionObject.id == null) {
                                            locallyAddedItems.removeIf { normalizeItemName(it.name) == normalizeItemName(constructionObject.name) }
                                        }
                                    }
                                    selectedItems.clear()
                                    selectedItems.putAll(newMap)
                                }
                            )
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = {
                    if (isRecording) {
                        speechService?.stopListening()
                        isRecording = false
                    }
                    onCancel()
                },
                containerColor = Color.White,
                contentColor = Color.Gray
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Cancel")
            }

            FloatingActionButton(
                onClick = { handleMicButtonClick() },
                containerColor = Color.White,
                contentColor = if (isRecording) Color.Red else Color.Gray
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = if (isRecording) "Stop Recording" else "Start Recording"
                )
            }

            FloatingActionButton(
                onClick = {
                    if (isRecording) {
                        speechService?.stopListening()
                        isRecording = false
                    }
                    val projectItemDrafts = selectedItems
                        .mapNotNull { (identifier, quantity) ->
                            if (quantity > 0) {
                                ProjectItemDraft(itemIdentifier = identifier, quantity = quantity)
                            } else {
                                null
                            }
                        }
                        .filter { draft ->
                            (draft.itemIdentifier is Int && draft.itemIdentifier > 0) ||
                                    (draft.itemIdentifier is String && draft.itemIdentifier.isNotBlank())
                        }

                    Log.d("ChangeItemsScreen", "Updated project item drafts being sent: $projectItemDrafts")
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "updatedProjectItemDrafts",
                        ArrayList(projectItemDrafts)
                    )
                    navController.popBackStack()
                },
                containerColor = Color.White,
                contentColor = Color.Gray
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Confirm")
            }
        }
    }
}