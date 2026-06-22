package com.github.caicosantos1998.voicenote;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private String currentLanguage = "pt";

    private DatabaseVoiceNote databaseVoiceNote;
    private EditText textNotes;
    private ImageButton bttSpeaker;
    private ImageButton bttMic;
    private TextToSpeech tts;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        textNotes = findViewById(R.id.txt_notes);
        bttSpeaker = findViewById(R.id.btt_speaker);
        bttMic = findViewById(R.id.btt_mic);

        tts = new TextToSpeech(this, status ->  {
            if(status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("pt", "BR"));
            }
        });
        databaseVoiceNote = new DatabaseVoiceNote(this);
        String savedNotes = databaseVoiceNote.getAllNotes();
        textNotes.setText(savedNotes);
        String namesHome = databaseVoiceNote.getTagName(currentLanguage);
        textNotes.setText(namesHome);

        bttSpeaker.setOnClickListener(v -> speakText());

        bttMic.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage.
                    equals("en") ? "en-US" : "pt-BR");
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, currentLanguage.
                    equals("en") ? "Speak now..." : "Fale agora...");

            try {
                startActivityForResult(intent, 100);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Your device does not support voice command!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {

                String newText = result.get(0);

                processText(newText);
            }
        }
    }
    private void speakText() {
        String text = textNotes.getText().toString();
        if(!text.isEmpty()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if(tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void processText(String fullText) {
        String voiceInput = fullText.toLowerCase().trim()
                .replaceAll("[.\\,?\\!]", "");

        boolean attemptsChangeTheme = voiceInput.contains("tema") || voiceInput.contains("modo") ||
                voiceInput.contains("theme") || voiceInput.contains("mode") ||
                voiceInput.contains("cor") || voiceInput.contains("tela") ||
                voiceInput.contains("color") || voiceInput.contains("screen") ||
                voiceInput.contains("claro") || voiceInput.contains("escuro") ||
                voiceInput.contains("light") || voiceInput.contains("dark") ||
                voiceInput.contains("night") || voiceInput.contains("day") ||
                voiceInput.contains("noite") || voiceInput.contains("dia");
        if(attemptsChangeTheme) {
            if (voiceInput.contains("modo claro") || voiceInput.contains("modo dia") ||
                    voiceInput.contains("light mode")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(this, currentLanguage.equals("en")
                        ? "Light mode activated" : "Modo claro ativado", Toast.LENGTH_SHORT).show();
                return;
            }
            if (voiceInput.contains("modo escuro") || voiceInput.contains("modo noite") ||
                    voiceInput.contains("dark mode") || voiceInput.contains("modo noturno")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(this, currentLanguage.equals("en")
                        ? "Dark mode activated" : "Modo escuro ativado", Toast.LENGTH_SHORT).show();
                return;
            }
            String messageAlert = currentLanguage.equals("en")
                    ? "To change theme say: 'Light mode' or 'Dark mode'"
                    : "Para mudar o tema fale: 'Modo claro' ou 'Modo escuro'";
            Toast.makeText(this, messageAlert, Toast.LENGTH_LONG).show();
            return;
        }

        boolean isSearching = voiceInput.contains("buscar")||voiceInput.contains("procurar")||
                voiceInput.contains("encontre") || voiceInput.contains("search") ||
                voiceInput.contains("find");

        if(isSearching) {
            String searchName = voiceInput
                    .replace("buscar", "")
                    .replace("procurar", "")
                    .replace("encontre", "")
                    .replace("search", "")
                    .replace("find", "")
                    .trim();
            if(!searchName.isEmpty()){
                String results = databaseVoiceNote.searchPerson(searchName);
                if(!results.isEmpty()){
                    textNotes.setText(results);
                    if(getSupportActionBar()!=null){
                        getSupportActionBar().setTitle(searchName.toUpperCase());
                    }
                }
            } else {
                Toast.makeText(this, currentLanguage.equals("en")?"No records found":
                        "Nenhum registro encontrado", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String regexPattern;
        if (currentLanguage.equals("en")) {
            regexPattern = "^(.*?) from (.*?) owes (\\d+)";
        } else {
            regexPattern = "^(.*?) (?:da|do) (.*?) deve (\\d+)";
        }

        Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(fullText.trim());
        if(matcher.find()){
            String name = matcher.group(1).trim();
            String location = matcher.group(2).trim();
            String valueString = matcher.group(3).trim();
            int value = Integer.parseInt(valueString);

            String message = currentLanguage.equals("en")
                    ? "Name: " + name + "\nFrom: " + location + "\nOwes: $" + value
                    : "Nome: " + name + "\nLocal: " + location + "\nDeve: R$ " + value;
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            databaseVoiceNote.insertNote(name, location, value);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(currentLanguage
                        .equals("en") ? "VOICE NOTE" : "NOTAS DE VOZ");
            }
            String tagName = databaseVoiceNote.getTagName(currentLanguage);
            textNotes.setText(tagName);
        } else {
            String alert = currentLanguage.equals("en")
                    ? "Incorrect pattern. Try: [Name] from [Street] owes [Value]"
                    : "Padrão incorreto. Tente: [Nome] da [Rua] deve [Valor]";
            Toast.makeText(this, alert, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.lang_pt) {
            currentLanguage = "pt";
            if (tts != null) tts.setLanguage(new Locale("pt", "BR"));
            Toast.makeText(this, "Idioma alterado para Português", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.lang_en) {
            currentLanguage = "en";
            if (tts != null) tts.setLanguage(Locale.US);
            Toast.makeText(this, "Language changed to English", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.theme_light) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            return true;
        } else if (id == R.id.theme_dark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}