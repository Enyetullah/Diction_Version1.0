package com.example.diction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

/**
 * MainActivity class handles the main screen of the application,
 * which includes text-to-speech and speech recognition functionalities.
 */
public class MainActivity extends AppCompatActivity {
    // UI components
    TextView mainDes;
    TextView rep;
    TextView repOp;
    Button wordbtn;
    Button tutorialBtn;
    ImageView microphone;

    // TextToSpeech instance
    TextToSpeech mainSpeaker;

    // SpeechRecognizer instance
    SpeechRecognizer speech;
    private Intent intrecognizer;

    // Permission request code
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    // Timeout constants for speech recognition
    private static final int SPEACHTIMEOUT = 5000;
    private static final int SPEACH = 10000;

    // Dummy view for intent redirection
    private View Dummy;

    // Handler and Runnable for handling speech timeouts
    private Handler handle = new Handler();
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            if (speech != null) {
                speech.stopListening();
                speech.cancel();
            }
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request audio recording permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                PackageManager.PERMISSION_GRANTED);

        // Initialize speech recognizer intent
        intrecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intrecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Bind UI components
        mainDes = findViewById(R.id.mainDescription);
        wordbtn = findViewById(R.id.wordButton);
        tutorialBtn = findViewById(R.id.tutorialButton);
        rep = findViewById(R.id.repeat);
        repOp = findViewById(R.id.repeatOptions);

        // Check if device has a microphone and request permission
        if (hasMicrophone()) {
            microphonePermission();
            initializeSpeech();  // Initialize SpeechRecognizer
            speech.startListening(intrecognizer);  // Start listening immediately
        }

        // Initialize TextToSpeech
        mainSpeaker = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mainSpeaker.setLanguage(Locale.ENGLISH);

                    // Speak the texts with specific utterance IDs
                    mainDesText();
                    tutorialBtnText();
                    wordBtnText();
                    repeatText(); // The last text to be spoken
                }
            }
        });
    }

    /**
     * Initializes the SpeechRecognizer and sets up the RecognitionListener.
     */
    private void initializeSpeech() {
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                handle.postDelayed(run, SPEACHTIMEOUT);
            }

            @Override
            public void onBeginningOfSpeech() {
                handle.removeCallbacks(run);
            }

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                handle.postDelayed(run, SPEACH);
            }

            @Override
            public void onError(int error) {
                speech.startListening(intrecognizer);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> match =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (match != null && !match.isEmpty()) {
                    String val = match.get(0);
                    if (val.equalsIgnoreCase("repeat")) {
                        speech.stopListening();
                        repeatOptionText();
                    } else if (val.equalsIgnoreCase("Document")) {
                        speech.stopListening();
                        speech.destroy();
                        wordDocMover(Dummy);
                    } else if (val.equalsIgnoreCase("Tutorial")) {
                        speech.stopListening();
                        speech.destroy();
                        tutorialMover(Dummy);
                    } else {
                        speech.startListening(intrecognizer);
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    /**
     * Speaks the main description text.
     */
    private void mainDesText() {
        String text = mainDes.getText().toString();
        if (!text.isEmpty()) {
            mainSpeaker.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }

    /**
     * Speaks the word button text.
     */
    private void wordBtnText() {
        String text = wordbtn.getText().toString();
        if (!text.isEmpty()) {
            mainSpeaker.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }

    /**
     * Speaks the tutorial button text.
     */
    private void tutorialBtnText() {
        String text = tutorialBtn.getText().toString();
        if (!text.isEmpty()) {
            mainSpeaker.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }

    /**
     * Speaks the repeat text.
     */
    private void repeatText() {
        String text = rep.getText().toString();
        if (!text.isEmpty()) {
            mainSpeaker.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }

    /**
     * Speaks the repeat option text.
     */
    private void repeatOptionText() {
        String text = repOp.getText().toString();
        if (!text.isEmpty()) {
            mainSpeaker.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }

    /**
     * Checks if the device has a microphone.
     *
     * @return true if the device has a microphone, false otherwise
     */
    private boolean hasMicrophone() {
        PackageManager pack = this.getPackageManager();
        return pack.hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    /**
     * Requests microphone permission from the user.
     */
    private void microphonePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    /**
     * Moves to the Tutorial activity.
     *
     * @param V the view that triggers this method
     */
    public void tutorialMover(View V) {
        Intent in = new Intent(MainActivity.this, Tutorial.class);
        speech.destroy();
        startActivity(in);
    }

    /**
     * Moves to the Word Document activity.
     *
     * @param V the view that triggers this method
     */
    public void wordDocMover(View V) {
        Intent in = new Intent(MainActivity.this, wordDoc.class);
        speech.destroy();
        startActivity(in);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speech != null) {
            speech.destroy();
        }
        if (mainSpeaker != null) {
            mainSpeaker.stop();
            mainSpeaker.shutdown();
        }
    }
}
