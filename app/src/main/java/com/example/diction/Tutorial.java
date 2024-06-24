package com.example.diction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Tutorial class provides a tutorial for the application, explaining various commands
 * using TextToSpeech and recognizes user voice commands with SpeechRecognizer.
 */
public class Tutorial extends AppCompatActivity {
    SpeechRecognizer speech; // Speech recognizer for handling voice commands
    Intent intRecognizer; // Intent for recognizing speech
    TextToSpeech wordSpeak; // Text-to-Speech engine for providing tutorial instructions
    // Permission request code for recording audio
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        // Initialize the speech recognizer intent
        intRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Check if the device has a microphone and request permission
        if (hasMicrophone()) {
            microphonePermission();
            initializeSpeech();  // Initialize SpeechRecognizer first
            speech.startListening(intRecognizer); // Start listening for speech immediately
        }

        // Initialize TextToSpeech engine
        wordSpeak = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR)
                {
                    wordSpeak.setLanguage(Locale.ENGLISH);
                    // Provide tutorial instructions using TextToSpeech
                    wordSpeak.speak("This is the Tutorial. Here I will give you information on " +
                            "how each command works. If you would like information on Font say Font"
                            + " If you would like information on size, Say Size, If you would " +
                            "like information on the Document, Say Document. If you would like to"
                            + " go back to the main page say, back", TextToSpeech.QUEUE_ADD, null);
                }
            }
        });
    }

    /**
     * Initializes the SpeechRecognizer and sets up the RecognitionListener.
     */
    private void initializeSpeech()
    {
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
                speech.startListening(intRecognizer);
            }

            @Override
            public void onResults(Bundle results) {
                // Handle speech recognition results
                ArrayList<String> match =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String val = "";
                if(val != null)
                {
                    val = match.get(0);
                    if(val.equalsIgnoreCase("Font"))
                    {
                        // Provide information about font options
                        wordSpeak.speak("The Following are the fonts available. Number 1 By " +
                                        "Saying Bold, you will be able to access Comfortaa Bold. " +
                                        "Number 2 By Saying light, you will be able to access " +
                                        "Comfortaa light. Number 3 By Saying medium, you will be " +
                                        "able to access Comfortaa Medium. Number 4 By Saying" +
                                        " Regular, you will be able to access Comfortaa Regular. " +
                                        "Number 5 By Saying Semi Bold, you will"
                                        + " be able to access Comfortaa Semi Bold. Number 6" +
                                        "By Saying time, you will be able to access gvTime.",
                                TextToSpeech.QUEUE_ADD, null);
                    }
                    else if(val.equalsIgnoreCase("Size"))
                    {
                        // Provide information about size options
                        wordSpeak.speak("By saying any number, you can change the size of your" +
                                        "text accordingly. For Example, if you would like the size"+
                                        " of your text to be 100, just say 100 after you state " +
                                        "the size command and it will automatically change the " +
                                        "size to 100.", TextToSpeech.QUEUE_ADD, null);
                    } else if (val.equalsIgnoreCase("Document")) {
                        // Provide information about document options
                        wordSpeak.speak("The following are the actions which are available " +
                                        "in your document currently. If you speak any word, " +
                                        "the document will automatically append the page with " +
                                        "that word. If you say backspace, the document will delete"+
                                        " the last word. Finally, if you say go back diction, " +
                                        "the document will automatically take you back to the " +
                                        "command selection page of the word Doc",
                                TextToSpeech.QUEUE_ADD, null);
                    } else if (val.equalsIgnoreCase("Back")) {
                        // Go back to the main activity
                        mainBack();
                    }
                    speech.startListening(intRecognizer); // Restart listening
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
                android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    /**
     * Navigates back to the main activity.
     */
    private void mainBack()
    {
        Intent intent = new Intent(this, MainActivity.class);
        speech.destroy();
        startActivity(intent);
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
