package com.jhinchley.recipereader20;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by jhinchley on 7/17/16.
 */

public class SpeechListener {
    public void promptSpeechInput() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        //this could be a string that could change
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),getString(R.string.speech_not_supported),Toast.LENGTH_SHORT).show();
        }
    }
}
