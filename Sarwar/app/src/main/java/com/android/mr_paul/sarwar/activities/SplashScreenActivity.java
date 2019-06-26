package com.android.mr_paul.sarwar.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.android.mr_paul.sarwar.R;

import java.util.Random;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        TextView quote = findViewById(R.id.main_quote);
        TextView quoteBy = findViewById(R.id.quote_by);

        String[] quotes = {
            "Giving is not just about making a donation. It is about making a difference.",
            "The measure of life is not its duration, but its donation.",
            "You have two hands, one for helping yourself, the other for helping others.",
            "We make a living by what we get but we make a life by what we give.",
            "No one has ever become poor by giving.",
            "Not all of us can do great things. But we can do small things with great love.",
            "I have found that among its other benefits, giving liberates the soul of giver.",
            "It's better to donate than accumulate.",
            "It's not how much we give but how much love we put into giving.",
            "Helping people by charity is the most human thing we can do.",
            "All we need is to build human relations with our deeds. That's the only thing that lasts forever",
            "We can't help everyone, but everyone can help someone.",
            "No act of kindness, no matter how small, is ever wasted.",
            "What we do for ourselves dies with us. What we do for others and the world remains & is immortal.",
            "Stay cool, stay calm and donate!"

        };

        String[] quotesBy = {
            "- Katley Calvin",
            "- Peter Marshall",
            "- Audrey Hepburn",
            "- Winston Churchill",
            "- Anne Frank",
            "- Mother Teresa",
            "- Maya Angelou",
            "- Anonymous",
            "- Mother Teresa",
            "- Oprah Winfrey",
            "- Nana Patekar",
            "- Dr. Loretta Scott",
            "- Richard Branson",
            "- Albert Pine",
            "- Developer @ Sarwar"

        };

        // randomly choose a quote to display on the splash screen
        Random random = new Random();
        int max = quotes.length-1;
        int min= 0;
        int index = random.nextInt((max - min) + 1) + min;

        // this if statement is just to avoid crashing, if any logic is coded wrong!
        if(index < quotes.length){
            // then display the quote
            quote.setText(quotes[index]);
            quoteBy.setText(quotesBy[index]);
        }


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                SplashScreenActivity.this.finish();

            }
        }, 2500); // show splash Screen for 2.5 seconds

    }
}
