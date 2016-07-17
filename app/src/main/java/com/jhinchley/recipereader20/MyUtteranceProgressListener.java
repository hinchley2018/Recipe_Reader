package com.jhinchley.recipereader20;

import android.content.Intent;
import android.speech.tts.UtteranceProgressListener;

/**
 * Created by jhinchley on 7/17/16.
 */

public class MyUtteranceProgressListener extends UtteranceProgressListener {

    @Override
    public void onStart(String utteranceId) {

    }

    @Override
    public void onDone(String utteranceId) {
        if (utteranceId.equals("Do you want anything repeated?")|| utteranceId.equals("I don't understand that, please try again!")){
            //prompt the user for the recipe to search for
            SpeechListener speechListener = new SpeechListener(new Intent());
            speechListener.promptSpeechInput();
        }
    }

    @Override
    public void onError(String utteranceId) {

    }

}