package com.jhinchley.recipereader20;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by jhinchley on 7/17/16.
 */

//class to make my life easier with tts
public class Speaker implements TextToSpeech.OnInitListener{
    private TextToSpeech tts;
    private boolean ready = false;
    private boolean allowed = true;
    private String done ="";

    public Speaker(Context context){
        tts = new TextToSpeech(context,this);
    }
    /*
    public boolean isAllowed(){
        return allowed;
    }

    public void allow(boolean allowed){
        this.allowed= allowed;
    }
    */
    @Override
    public void onInit(int status) {
        //if tts is installed
        if (status == TextToSpeech.SUCCESS){

            //set the lang
            tts.setLanguage(Locale.US);

            //set a listener
            tts.setOnUtteranceProgressListener(new MyUtteranceProgressListener());

            //set flag so i know its installed
            ready = true;
        }else {
            ready = false;
        }
    }
    @SuppressWarnings("deprecation")
    private void ttsSpeakUnder20(String text,HashMap hash) {
        tts.speak(text,TextToSpeech.QUEUE_ADD,hash);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsSpeakGreater21(String text,Bundle bundle) {
        String utteranceId = text;//this.hashCode()+"";
        tts.speak(text, TextToSpeech.QUEUE_ADD, bundle, utteranceId);
    }
    public void speak(String text){


        //Speak only if the TTS is ready
        //and the user has allowed speech
        if(ready && allowed) {

            //speak using the correct version of the api
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Bundle bundle = new Bundle();
                bundle.putString(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
                bundle.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"Unique ID");
                ttsSpeakGreater21(text, bundle);
            } else {
                HashMap<String, String> hash = new HashMap<String,String>();
                hash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"Unique ID");
                hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM,String.valueOf(AudioManager.STREAM_NOTIFICATION));
                ttsSpeakUnder20(text,hash);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void ttsSilenceUnder20(int durationInt) {
        long durationLong = new Long(durationInt);
        tts.playSilence(durationLong, TextToSpeech.QUEUE_ADD, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsSilenceGreater21(int duration) {
        tts.playSilentUtterance(duration,TextToSpeech.QUEUE_ADD,null);
    }

    //pause the speech
    public void pause(int duration){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsSilenceGreater21(duration);
        } else {
            ttsSilenceUnder20(duration);

        }
    }

    public class MyUtteranceProgressListener extends UtteranceProgressListener {

        @Override
        public void onStart(String utteranceId) {

        }

        @Override
        public void onDone(String utteranceId) {
            if (utteranceId.equals("Do you want anything repeated?")|| utteranceId.equals("I don't understand that, please try again!")){
                //prompt the user for the recipe to search for
                SpeechListener speechListener = new SpeechListener();
                speechListener.promptSpeechInput();
            }
        }

        @Override
        public void onError(String utteranceId) {

        }

    }

    //Free up resources
    public void destroy(){tts.shutdown();}

}
