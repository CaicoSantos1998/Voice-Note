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

        textNotes.setText("Maria from Boulevard street owes 100 dollars");

        tts = new TextToSpeech(this, status ->  {
            if(status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        bttSpeaker.setOnClickListener(v -> speakText());

        bttMic.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
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
                String oldText = textNotes.getText().toString();
                String newText = result.get(0);
                if (!oldText.isEmpty()) {
                    textNotes.setText(oldText + "\n" + newText);
                } else {
                    textNotes.setText(newText);
                }
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
}