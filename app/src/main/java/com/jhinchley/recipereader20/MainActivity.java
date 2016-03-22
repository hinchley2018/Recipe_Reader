package com.jhinchley.recipereader20;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity {

    //Find the directory for SD Card using the API
    String recipe_path = Environment.getExternalStorageDirectory().toString()+"/Recipes";

    //create file object
    File f = new File(recipe_path);

    //a list of files in the recipe_path
    File[] file_list = f.listFiles();

    //spinner to display the recipes I can download
    Spinner recipeSpinner;

    //textviews to display information
    TextView recipeView,Recipe_Name,ratings,Description;

    //submit button
    Button Submit_Button;

    //list to hold files
    List<String> fileArray;

    //string to hold recipename
    String recipeName;

    //list to hold ingredients and directions respectively
    List<String> ingredientArray, directionsArray;

    //code for speech input
    private final int REQ_CODE_SPEECH_INPUT = 100;

    //check code to validate tts
    private final int CHECK_CODE = 0x1;

    //ints for pause duration
    private final int LONG_DURATION = 5000;
    private final int SHORT_DURATION =1200;

    //hold the state of the program so I can check it in onActivityResult
    private String myStep = "";
    private boolean side =true;

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
                    promptSpeechInput();
                }
            }

            @Override
            public void onError(String utteranceId) {

            }

        }

        //Free up resources
        public void destroy(){tts.shutdown();}


    }

    //speaker object to allow me to tts easier
    private Speaker speaker;

    private void checkTTS(){

        //checks if TTS engine is installed on the device
        Intent check = new Intent();

        //check if tts is installed
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_CODE);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CHECK_CODE: {
                if(resultCode==TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){

                    //create an instance of speaker class
                    speaker=new Speaker(this);
                }else{

                    //if no tts engine exists install one
                    Intent install = new Intent();
                    install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(install);
                }
                break;
            }

            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    //the line below has the speech result as string i need to do something with it...
                    Toast.makeText(getBaseContext(),result.get(0), Toast.LENGTH_LONG).show();

                    StringBuilder stringBuilder = new StringBuilder();

                    for (int i =0; i<result.size();i++){
                        stringBuilder.append(result.get(i)+'\n');
                    }
                    recipeView.setText(stringBuilder);
                    //expected inputs
                    /*
                    //yes
                    //no
                    //how much myIngredient
                    //wait/sleep for mytime unit(seconds || minutes || hours)
                    //information/repeat/what is/was step/part/direction myNumber(1,2..100 || 1st, 2nd, 3rd ... 100th)
                    /*/
                    if (result.contains("yes")||result.contains("repeat")){

                        //repeat same thing they were on
                        conversation(myStep,false);
                    }
                    //if the user is starting
                    else if ( (result.get(0).split(" ")[0].equals("recipe")) && (result.get(0).split(" ")[1].equals("for")) ){
                        //Log.d("Search Recipe",result.get(0));

                        //split the speech result into a list of strings and remove
                        //"recipe for" so that only the recipename remains

                    }
                    //if user is done send them directions
                    else if (result.contains("no")){

                        if (myStep.equals("ingredients")){

                            //if they have finished ingredients send them to directions
                            myStep = "directions";

                            conversation(myStep,false);
                        }
                        else {
                            //i dont know what to do yet
                            conversation("unknown",false);
                        }
                    }
                    //if the user wants to know how much of something send to conversation
                    else if (result.get(0).split(" ")[0].equals("how")&&result.get(0).split(" ")[1].equals("much")){
                        for (int i = 0; i<ingredientArray.size();i++){
                            for (int j =0; j<result.size();j++){

                                String[] mylist = result.get(j).split(" ");
                                for (int k =0; k<mylist.length;k++){
                                    if (ingredientArray.get(i).contains(mylist[k])){
                                        conversation(ingredientArray.get(i),false);
                                        break;

                                    }

                                }
                            }
                        }

                    }

                    //send the program to sleep for specified period of time
                    else if (result.get(0).split(" ")[0].equals("sleep")||result.get(0).split(" ")[0].equals("wait")){
                        //sleep the ui thread so entire program waits until user is ready

                        //expecting this to be the quantity of time
                        int time_quantity = Integer.parseInt(result.get(0).split(" ")[1]);

                        //expecting this to be the units of time
                        String time_units = result.get(0).split(" ")[2];

                        //sleep is in ms convert time_quantity to ms using its units
                        long sleep_time = 0;

                        //try converting it if I know the units to do the math
                        if (time_units.equals("seconds")){
                            sleep_time = time_quantity*1000;
                        }
                        else if (time_units.equals("minutes")){
                            sleep_time = time_quantity*60*1000;
                        }
                        else if (time_units.equals("hours")){
                            sleep_time = time_quantity*60*60*1000;
                        }

                        //else the time to sleep is 0 ms bc I dont want to go to break my program

                        SystemClock.sleep(sleep_time);
                    }
                    break;
                }
            }
        }
    }
    //read it out one step at a time or the user can ask about ingredients by index
    private void promptSpeechInput() {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set layout
        setContentView(R.layout.activity_main);

        //textview for recipe file
        recipeView    = (TextView)findViewById(R.id.recipe_File);

        //textview for recipe name
        Recipe_Name   = (TextView)findViewById(R.id.Recipe_Name);

        //textview for recipe ratings
        ratings       = (TextView)findViewById(R.id.ratings);

        //textview for recipe description
        Description   = (TextView)findViewById(R.id.Description);

        //submit button for user to download a recipe
        Submit_Button = (Button)findViewById(R.id.Submit_Button);

        //recipe list to hold recipes
        List<String> recipeArray = new ArrayList<String>();

        //add a placeholder so onchangelistener doesnt break
        recipeArray.add("Choose One");

        //set ringer to normal so user can hear
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        //check status of TTS engine
        checkTTS();

        // hide the action bar
        //getActionBar().hide();

        //iterate through file list
        for (int i=0; i< file_list.length;i++){
            //Log.d("Files","FileName: " + file_list[i].getName());

            //add file contents to recipe list
            recipeArray.add(file_list[i].getName());

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,recipeArray);

            recipeSpinner = (Spinner) findViewById(R.id.spinner1);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            recipeSpinner.setAdapter(adapter);

            recipeSpinner.setSelection(0, false);

            recipeSpinner.setOnItemSelectedListener(new MyOnItemSelectedListener());

        }

        //sleep the ui so that user has time to react
        SystemClock.sleep(500);

        //prompt the user for the recipe to search for
        promptSpeechInput();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        speaker.destroy();
    }


    public class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (parent.getSelectedItem() != "Choose One") {

                //Toast.makeText(parent.getContext(), Long.toString(parent.getItemIdAtPosition(position)), Toast.LENGTH_SHORT).show();
                Recipe_Name.setText("");
                ratings.setText("");
                Description.setText("");

                Recipe_Name.setVisibility(View.VISIBLE);
                ratings.setVisibility(View.VISIBLE);
                Description.setVisibility(View.VISIBLE);
                Submit_Button.setVisibility(View.VISIBLE);

                File myRecipe = new File(Environment.getExternalStorageDirectory(),"/Recipes/"+parent.getSelectedItem().toString());
                StringBuilder text = new StringBuilder();
                recipeName = "";
                fileArray = new ArrayList<String>();
                ingredientArray = new ArrayList<String>();
                directionsArray = new ArrayList<String>();

                try{
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(myRecipe));
                    String line;

                    while((line=bufferedReader.readLine()) != null){
                        fileArray.add(line);

                    }

                    recipeName=fileArray.get(0);

                    fileArray.remove(0);

                    fileArray.remove(0);

                    //add elements of fileArray to ingredientArray until you reach the directions
                    for (int i =0;i<fileArray.indexOf("Directions");i++) {
                        ingredientArray.add(fileArray.get(i));
                    }
                    for (int i = fileArray.indexOf("Directions")+1;i<fileArray.size();i++){
                        directionsArray.add(fileArray.get(i));
                    }
                    //Toast.makeText(parent.getContext(),directionsArray.get(directionsArray.size()-1),Toast.LENGTH_SHORT).show();
                    //Toast.makeText(parent.getContext(), ingredientArray.get(ingredientArray.size()-1), Toast.LENGTH_SHORT).show();;


                    myStep = "ingredients";
                    conversation(myStep,side);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                }

                //recipeView.setText(text);
            }


        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private void conversation(String step,boolean side) {

        //toast which step user is at
        Toast.makeText(getApplicationContext(),step,Toast.LENGTH_LONG).show();

        if (step.equals("ingredients")) {

            speaker.speak("Ingredients for " + recipeName + ".");

            speaker.pause(SHORT_DURATION);

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < ingredientArray.size(); i++) {

                speaker.speak(ingredientArray.get(i));

                sb.append(ingredientArray.get(i) + '\n');

                speaker.pause(SHORT_DURATION);
            }
            recipeView.setText(sb);
        }

        else if (step.equals("directions")) {

            speaker.speak("Directions for " + recipeName + ".");

            speaker.pause(SHORT_DURATION);

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < directionsArray.size(); i++) {

                speaker.speak(directionsArray.get(i));

                sb.append(directionsArray.get(i) + '\n');

                speaker.pause(LONG_DURATION);
            }
            recipeView.setText(sb);
        }

        else if (step.equals("unknown")){
            speaker.speak("I don't understand that, please try again!");
        }

        else {
            for (int i = 0; i<ingredientArray.size();i++){

                if (step.equals(ingredientArray.get(i))){

                    speaker.speak(ingredientArray.get(i));
                    break;
                }
            }
            for (int i = 0; i<directionsArray.size();i++){

                if (step.equals(directionsArray.get(i))){

                    speaker.speak(directionsArray.get(i));
                    break;
                }
            }
        }

        speaker.pause(SHORT_DURATION);

        speaker.speak("Do you want anything repeated?");

        speaker.pause(SHORT_DURATION);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
