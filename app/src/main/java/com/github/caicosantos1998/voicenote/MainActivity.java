package com.github.caicosantos1998.voicenote;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText textNotes;
    private ImageButton bttSpeaker;
    private ImageButton bttMic;
    private TextToSpeech tts;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textNotes = findViewById(R.id.txt_notes);
        bttSpeaker = findViewById(R.id.btt_speaker);
        bttMic = findViewById(R.id.btt_mic);

        tts = new TextToSpeech(this, status ->  {
            if(status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("pt", "BR"));
            }
        });

        bttSpeaker.setOnClickListener(v -> speakText());

        bttMic.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR");
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

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
        String regexPattern = "^(.*?) (?:da|do) (.*?) deve (\\d+)";

        Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(fullText.trim());
        if(matcher.find()){
            String name = matcher.group(1).trim();
            String location = matcher.group(2).trim();
            String valueString = matcher.group(3).trim();
            int value = Integer.parseInt(valueString);

            String message = "Nome: " + name + "\nLocal: " + location + "\nDeve: R$ " + value;
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            String oldText = textNotes.getText().toString();
            if (!oldText.isEmpty()) {
                textNotes.setText(oldText + "\n" + fullText);
            } else {
                textNotes.setText(fullText);
            }

        } else {
            Toast.makeText(this,
                    "Não entendi o padrão. Tente: [Nome] da [Rua] deve [Valor]",
                    Toast.LENGTH_LONG).show();
        }
    }
}