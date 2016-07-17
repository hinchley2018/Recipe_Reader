package com.jhinchley.recipereader20;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AsyncResponse{

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
    Button Submit_Button,Speech_Button;

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

    @Override
    public void processResponse(String jsonResponse) {
        //JSONObject json =
    }

    //speaker object to allow me to tts easier
    private Speaker speaker;
    private Conversation conversation;

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
                        conversation.conversation(myStep,false);
                    }
                    //if the user is starting
                    else if ( (result.get(0).split(" ")[0].equals("recipe")) && (result.get(0).split(" ")[1].equals("for")) ){
                        Log.i("Search Recipe",result.get(0));
                        String recipename ="";
                        String[] recipevar = result.get(0).split(" ");
                        for (int j =0;j<recipevar.length;j++){
                            if (j>1){
                                recipename+=recipevar[j]+=" ";
                            }
                        }
                        Log.e("Recipe Result",recipename);

                        //send json data to server for processing
                        send_data_to_server(recipename);


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set layout
        setContentView(R.layout.activity_main);

        //textview for recipe file
        recipeView    = (TextView)findViewById(R.id.recipe_File);
        Recipe_Name   = (TextView)findViewById(R.id.Recipe_Name);
        ratings       = (TextView)findViewById(R.id.ratings);
        Description   = (TextView)findViewById(R.id.Description);

        //submit button for user to download a recipe
        Submit_Button = (Button)findViewById(R.id.Submit_Button);
        Speech_Button = (Button)findViewById(R.id.Speech_Button);

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
        SpeechListener speechListener = new SpeechListener();
        speechListener.promptSpeechInput();
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

    public void send_data_to_server(String Recipe){

        //get the values from the views
        //later we want them to be able to type what they actually wanted
        //Recipe=FirstNameView.getText().toString();

        //create a JSONObject so I can post data...
        JSONObject post_dict = new JSONObject();

        //debug
        //Toast.makeText(getApplication(),"F:"+FirstName+",L:"+LastName+",A:"+Age,Toast.LENGTH_SHORT).show();

        //try to put the recipe in the jsonObject
        try {
            post_dict.put("recipe",Recipe);
            //debug
            Log.i("My JSONObject",post_dict.toString());
            Toast.makeText(getApplication(),"My Json:"+post_dict.toString(),Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (post_dict.length()>0){
            new SendJsonDataToServer(this).execute(String.valueOf(post_dict));
        }
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

