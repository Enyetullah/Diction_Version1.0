package com.example.diction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

/**
 * wordDoc class provides functionality for a word processing document
 * where users can change fonts, text size, and dictate text using voice commands.
 */
public class wordDoc extends AppCompatActivity {
    EditText mainPage; // Main text area for the document
    TextView DocSpeak; // TextView for instructions on document speech
    TextView changeF; // TextView for instructions on changing font
    TextView fontSelection; // TextView for instructions on selecting font
    TextView fontSizeSelection; // TextView for instructions on selecting text size
    TextView sizeChange; // TextView for instructions on changing text size
    TextView enterMain; // TextView for instructions on entering the main document
    TextToSpeech wordSpeak; // Text-to-Speech engine for providing instructions
    SpeechRecognizer speech; // Speech recognizer for handling voice commands
    private Intent intrecognizer; // Intent for recognizing speech
    private boolean isFontSelection = false; // Flag for font selection mode
    private boolean isTextSize = false; // Flag for text size selection mode
    private boolean isMainPage = false; // Flag for main page mode
    private boolean ignoreResult = false; // Flag to ignore speech result temporarily
    // Permission request code for recording audio
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_doc);

        mainPage = findViewById(R.id.Doc);
        DocSpeak = findViewById(R.id.DocText);
        changeF = findViewById(R.id.fontChange);
        fontSelection = findViewById(R.id.fontSelect);
        fontSizeSelection = findViewById(R.id.fontSize);
        sizeChange = findViewById(R.id.changeSize);
        enterMain = findViewById(R.id.enterDoc);

        intrecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intrecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Start listening immediately if the device has a microphone
        if (hasMicrophone()) {
            microphonePermission();
            initializeSpeech();  // Initialize SpeechRecognizer first
        }

        // Initialize TextToSpeech engine
        wordSpeak = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    wordSpeak.setLanguage(Locale.ENGLISH);
                    speak();
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
                // Callback when the speech recognizer is ready to listen
            }

            @Override
            public void onBeginningOfSpeech() {
                // Callback when speech input begins
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Callback to receive the RMS (Root Mean Square) value of the sound input
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Callback to receive a buffer of audio data
            }

            @Override
            public void onEndOfSpeech() {
                // Callback when speech input ends
            }

            @Override
            public void onError(int error) {
                // Restart listening on error
                speech.startListening(intrecognizer);
            }

            @Override
            public void onResults(Bundle results) {
                // Handle speech recognition results
                ArrayList<String> match =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String val = "";
                val = match.get(0);
                if (match != null) {
                    if (ignoreResult) {
                        ignoreResult = false; // Reset the flag
                        speech.startListening(intrecognizer);
                        return; // Skip appending the text
                    }
                    // Handle font selection mode
                    if (val.equalsIgnoreCase("Font") && !isFontSelection &&
                            !isMainPage && !isTextSize) {
                        fontSelectionSpeaker();
                        isFontSelection = true;
                    } else if (isFontSelection && !isMainPage && !isTextSize) {
                        speech.startListening(intrecognizer);
                        val = match.get(0);
                        fontSelect(val);
                    }
                    // Handle text size selection mode
                    if (val.equalsIgnoreCase("Size") && !isTextSize &&
                            !isMainPage && !isFontSelection) {
                        textSizeSelect();
                        isTextSize = true;
                    } else if (isTextSize && !isMainPage && !isFontSelection) {
                        speech.startListening(intrecognizer);
                        val = match.get(0);
                        textSize(val);
                    }
                    // Handle document mode
                    if (val.equalsIgnoreCase("Document") && !isMainPage &&
                            !isFontSelection && !isTextSize) {
                        enterDocument();
                        isMainPage = true;
                    } else if (isMainPage) {
                        if (val.equalsIgnoreCase("Go Back Diction")) {
                            isMainPage = false;
                            wordSpeak.speak("Document Exited",TextToSpeech.QUEUE_ADD,null);
                        } else if (val.equalsIgnoreCase("backspace")) {
                            deleteLastWord();
                        } else {
                            speech.startListening(intrecognizer);
                            val = match.get(0);
                            mainPage.append(val + " ");
                        }
                    } else if(val.equalsIgnoreCase("back")){
                        mainBack();
                        wordSpeak.speak("Document Exited",TextToSpeech.QUEUE_ADD,null);
                    }

                    if (val.equalsIgnoreCase("Speak")) {
                        mainSpeak();
                    }

                    speech.startListening(intrecognizer);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Callback for partial speech recognition results
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Callback for additional events related to speech recognition
            }
        });
        speech.startListening(intrecognizer);
    }

    /**
     * Speaks the text in DocSpeak TextView.
     */
    private void speak() {
        String text = DocSpeak.getText().toString();
        if (!text.isEmpty()) {
            wordSpeak.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }

    /**
     * Speaks the text in changeF TextView.
     */
    private void fontChangeSpeak() {
        String text = changeF.getText().toString();
        if (!text.isEmpty()) {
            wordSpeak.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }

    /**
     * Speaks the text in fontSelection TextView.
     */
    private void fontSelectionSpeaker() {
        String text = fontSelection.getText().toString();
        if (!text.isEmpty()) {
            wordSpeak.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }

    /**
     * Speaks the text in enterMain TextView and sets ignoreResult flag.
     */
    private void enterDocument() {
        String text = enterMain.getText().toString();
        if (!text.isEmpty()) {
            wordSpeak.speak(text, TextToSpeech.QUEUE_ADD, null);
            ignoreResult = true;
        }
    }

    /**
     * Speaks the text in fontSizeSelection TextView.
     */
    private void textSizeSelect() {
        String text = fontSizeSelection.getText().toString();
        if (!text.isEmpty()) {
            wordSpeak.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }

    /**
     * Speaks the text in sizeChange TextView.
     */
    private void textSizeChange() {
        String text = sizeChange.getText().toString();
        if (!text.isEmpty()) {
            wordSpeak.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }

    /**
     * Speaks the text in mainPage EditText.
     */
    private void mainSpeak() {
        wordSpeak.speak(mainPage.getText().toString(), TextToSpeech.QUEUE_ADD, null);
    }

    /**
     * Changes the font of the given EditText.
     *
     * @param tex          The EditText to change the font for
     * @param fontResouceId The resource ID of the new font
     */
    private void changeFont(EditText tex, int fontResouceId) {
        Typeface type = ResourcesCompat.getFont(this, fontResouceId);
        tex.setTypeface(type);
    }

    /**
     * Changes the font of mainPage EditText.
     *
     * @param f The resource ID of the new font
     */
    private void fontChange(int f) {
        changeFont(mainPage, f);
    }

    /**
     * Selects the font based on the given value.
     *
     * @param val The spoken value for font selection
     */
    private void fontSelect(String val) {
        if (val.equalsIgnoreCase("time")) {
            fontChange(R.font.gvtime);
            fontChangeSpeak();
            isFontSelection = false;
        } else if (val.equalsIgnoreCase("bold")) {
            fontChange(R.font.comfortaabold);
            fontChangeSpeak();
            isFontSelection = false;
        } else if (val.equalsIgnoreCase("light")) {
            fontChange(R.font.comfortaalight);
            fontChangeSpeak();
            isFontSelection = false;
        } else if (val.equalsIgnoreCase("medium")) {
            fontChange(R.font.comfortaameduim);
            fontChangeSpeak();
            isFontSelection = false;
        } else if (val.equalsIgnoreCase("regular")) {
            fontChange(R.font.comfortaaregular);
            fontChangeSpeak();
            isFontSelection = false;
        } else if (val.equalsIgnoreCase("semi bold")) {
            fontChange(R.font.comfortaasemibold);
            fontChangeSpeak();
            isFontSelection = false;
        }
    }

    /**
     * Sets the text size of mainPage EditText based on the given value.
     *
     * @param val The spoken value for text size
     */
    private void textSize(String val) {
        try {
            float size = Float.parseFloat(val);
            mainPage.setTextSize(size);
            textSizeChange();
            isTextSize = false;
        } catch (NumberFormatException e) {

        }
    }

    /**
     * Deletes the last word from mainPage EditText.
     */
    private void deleteLastWord() {
        String text = mainPage.getText().toString().trim();
        int lastSpace = text.lastIndexOf(' ');
        if (lastSpace != -1) {
            String newText = text.substring(0, lastSpace + 1);
            mainPage.setText(newText);
            mainPage.setSelection(newText.length());
        } else {
            mainPage.setText("");
        }
    }

    /**
     * Navigates back to the MainActivity.
     */
    private void mainBack() {
        Intent intent = new Intent(this, MainActivity.class);
        speech.destroy();
        startActivity(intent);
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
     * Requests permission to use the microphone if not already granted.
     */
    private void microphonePermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speech != null) {
            speech.destroy();
        }
        if (wordSpeak != null) {
            wordSpeak.stop();
            wordSpeak.shutdown();
        }
    }
}
