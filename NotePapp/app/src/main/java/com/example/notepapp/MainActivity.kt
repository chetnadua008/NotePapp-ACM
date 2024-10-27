package com.example.notepapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.notepapp.ui.theme.NotePappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotePappTheme {
                MyScaffold()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun MyScaffold() {
    val context = LocalContext.current
    val notesList = remember { mutableStateListOf<String>() }

    // Load notes from Shared Preferences when the app starts
    LaunchedEffect(Unit) {
        notesList.addAll(loadNotes(context))
    }

    var showNoteInput by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "NotePapp") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2F4F4F), titleContentColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNoteInput = true },
                containerColor = Color(0xFFFF5722)
            ) {
                Text("+")
            }
        },
        content = { paddingValues ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0xFF121212))
                    .padding(paddingValues)
            ) {
                items(notesList) { note ->
                    NoteItem(note) {
                        // Delete note functionality
                        notesList.remove(note)
                        saveNotes(context, notesList) // Save updated notes list
                    }
                }
            }

            if (showNoteInput) {
                NoteInput(
                    onAddNote = { newNote ->
                        // Add new note to the list
                        notesList.add(newNote)
                        // Save notes to Shared Preferences
                        saveNotes(context, notesList)
                        showNoteInput = false
                    },
                    onCancel = {
                        showNoteInput = false
                    }
                )
            }
        }
    )
}

@Composable
fun NoteItem(note: String, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = note,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Note",
                    tint = Color(0xFF2F4F4F),
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}

@Composable
fun NoteInput(onAddNote: (String) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    val maxLength = 200

    AlertDialog(
        containerColor = Color(0xFF5F7F7F),
        onDismissRequest = { onCancel() },
        title = { Text("New Note") },
        text = {
            Column(modifier = Modifier.padding(8.dp)) {
                TextField(
                    value = title,
                    onValueChange = { if (it.length <= maxLength)  title = it },
                    label = { Text("Note") },
                    placeholder = { Text("Enter new Note") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAddNote(title)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F4F))
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F4F))) {
                Text("Cancel")
            }
        }
    )
}

// Functions to load and save notes in Shared Preferences
fun loadNotes(context: Context): List<String> {
    val sharedPreferences = context.getSharedPreferences("NotesPrefs", Context.MODE_PRIVATE)
    val notes = mutableListOf<String>()
    // Load each note using a key pattern
    var i = 0
    while (true) {
        val note = sharedPreferences.getString("note_$i", null) ?: break
        notes.add(note)
        i++
    }
    return notes
}

fun saveNotes(context: Context, notes: List<String>) {
    val sharedPreferences = context.getSharedPreferences("NotesPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    // Clear existing notes before saving new ones
    editor.clear()
    // Save each note with a unique key
    notes.forEachIndexed { index, note ->
        editor.putString("note_$index", note)
    }
    editor.apply()
}
