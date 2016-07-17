package com.jhinchley.recipereader20;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by jhinchley on 7/17/16.
 */

public class Conversation extends Activity{
    //ints for pause duration
    private final int LONG_DURATION = 5000;
    private final int SHORT_DURATION =1200;
    public void conversation(Speaker speaker,String step,boolean side) {

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
}
