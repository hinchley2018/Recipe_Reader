package com.jhinchley.recipereader20;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by jhinchley on 7/17/16.
 */

public class SpeechListener extends Activity {
    //code for speech input
    private final int REQ_CODE_SPEECH_INPUT = 100;
    Intent intent;
    public SpeechListener(Intent passed_intent){
        intent=passed_intent;
    }
    public void promptSpeechInput() {


        intent.setAction(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        //this could be a string that could change
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,R.string.speech_prompt);
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),R.string.speech_not_supported,Toast.LENGTH_SHORT).show();
        }
    }
}
