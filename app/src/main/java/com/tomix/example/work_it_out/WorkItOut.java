package com.tomix.example.work_it_out;

import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;



public class WorkItOut extends ActionBarActivity {

    private char[] symbol = {'-','+','x','/'};
    private int  score, totalNumberOfQuestion,
            totalNumberAnswered, level;
    private int correctAnswer;
    private Handler handler;
    private int guessRows;
    private TextView firstValueTextView;
    private TextView secondValueTextView;
    private TextView arithmeticSymbolTextView;
    private TextView scoreTextView;
    private TextView answerTextView;
    private TextView timerTextView;
    private TextView resultTextView;
    private TableLayout buttonTableLayout;
    private int levelQuestion;
    private int soundIdTimeUp;
    private int soundIdLose;
    private int soundIdWin;



    private static SoundPool soundPool;

    private float volume;

    private boolean loaded = false;

    // Stream type.
    private static final int streamType = AudioManager.STREAM_MUSIC;

    private Context context;

    private CountDownTimer cTimer = null;

    Random randomNumber = new Random();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.work_it_out);

        AudioManager audioManager;

        firstValueTextView = (TextView) findViewById(R.id.firstValueTextView);
        secondValueTextView = (TextView) findViewById(R.id.secondValueTextView);
        arithmeticSymbolTextView = (TextView) findViewById(R.id.arithmeticSymbolTextView);
        scoreTextView = (TextView) findViewById(R.id.scoreTextView);
        answerTextView = (TextView) findViewById(R.id.answerTextView);

        buttonTableLayout = (TableLayout) findViewById(R.id.buttonTableLayout);
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        timerTextView = (TextView) findViewById(R.id.timerTextView);

        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);

        // AudioManager audio settings for adjusting the volume
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // Current volume Index of particular stream type.
        float currentVolumeIndex = (float) audioManager.getStreamVolume(streamType);

        // Get the maximum volume index for a particular stream type.
        float maxVolumeIndex  = (float) audioManager.getStreamMaxVolume(streamType);

        // Volume (0 --> 1)
        this.volume = currentVolumeIndex / maxVolumeIndex;

        // When Sound Pool load complete.
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        this.soundIdTimeUp = soundPool.load(this,  R.raw.buzzer, 1);
        this.soundIdLose = soundPool.load(this,  R.raw.boo, 2);
        this.soundIdWin = soundPool.load(this,  R.raw.applause, 3);



        guessRows = 1;
        handler = new Handler();
        level = 0;

        start(level);
    }

    private void startTimer(int time) {

        cTimer = new CountDownTimer(time, 1000) {

            public void onTick(long millisUntilFinished) {
                timerTextView.setText("Seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                timerTextView.setText("Done!");
                timeNow();
            }
        };
        cTimer.start();
    }

    private void cancelTimer() {
        if (cTimer != null ){
            cTimer.cancel();
        }
    }

    private void timeNow() {

            AlertDialog.Builder builder = new AlertDialog.Builder(WorkItOut.this);

            builder.setTitle("Time Up");

            builder.setMessage("Sorry You Lose Because you couldn't beat time");

            builder.setPositiveButton("End Game", new DialogInterface.OnClickListener(){
                @Override
                public  void  onClick(DialogInterface dialog, int which){
                    WorkItOut.this.finish();
                }
            });

            builder.setNegativeButton("Restart", new DialogInterface.OnClickListener() {
                @Override
                public  void  onClick(DialogInterface dialog, int which){
                    WorkItOut.this.start(0);
                }
            });

            AlertDialog timeUp = builder.create();

            if (loaded) {
                float leftVolume = volume;
                float rightVolume = volume;

                soundPool.play( this.soundIdTimeUp, leftVolume, rightVolume, 1, 0, 1f);
            }

            timeUp.show();


    }

    private void start(int level){

        score = 0;
        totalNumberAnswered = 0;

        switch (level) {

            case 0: totalNumberOfQuestion = 10;
                    startTimer(60000);
                    levelQuestion = 10;
                    loadQuestion();
                break;

            case 1: totalNumberOfQuestion = 10;
                    startTimer(50000);
                    levelQuestion = 10;
                    loadQuestion();
                break;

            case 2: totalNumberOfQuestion = 10;
                    startTimer(45000);
                    levelQuestion = 12;
                    loadQuestion();
                break;

            case 3: totalNumberOfQuestion = 15;
                    startTimer(40000);
                    levelQuestion = 12;
                    loadQuestion();
                break;

            case 4: totalNumberOfQuestion = 15;
                    startTimer(30000);
                    levelQuestion = 15;
                    loadQuestion();
                break;
        }

        scoreTextView.setText(String.valueOf(score));

    }

    private void loadQuestion(){

        int valueLeft = 1 + randomNumber.nextInt(levelQuestion);
        int valueRight = 1 + randomNumber.nextInt(levelQuestion);
        int result = 0;

        resultTextView.setText("");

        int randomSymbol =  randomNumber.nextInt(4);
        int temp;

        switch (symbol[randomSymbol]){
            case '+' : result = valueLeft + valueRight;
                arithmeticSymbolTextView.setText("+");
                break;
            case '/' :
                if(valueLeft < valueRight) {
                    temp = valueLeft;
                    valueLeft = valueRight;
                    valueRight = temp;
                }
                while( valueLeft % valueRight != 0) {
                    valueRight = 1 + randomNumber.nextInt(10);
                }

                result = valueLeft / valueRight;
                arithmeticSymbolTextView.setText("/");
                break;
            case 'x' : result = valueLeft * valueRight;
                arithmeticSymbolTextView.setText("x");
                break;
            case '-' :
                if(valueLeft < valueRight) {
                    temp = valueLeft;
                    valueLeft = valueRight;
                    valueRight = temp;
                }
                result = valueLeft - valueRight;
                arithmeticSymbolTextView.setText("-");
                break;
        }

        correctAnswer = formQuestion(valueLeft, valueRight, result);

        // clear prior answer Buttons  from tables
        for (int row = 0; row < buttonTableLayout.getChildCount(); ++row )
            ((TableRow) buttonTableLayout.getChildAt(row)).removeAllViews();

        LayoutInflater inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE );

        for ( int row = 0; row < guessRows; row++ ){

            TableRow currentTableRow = getTableRow(row);

            for (int column = 0; column < 3; column++) {

                Button newGuessButton =
                        (Button) inflater.inflate(R.layout.guess_button, null);

                int randomGuesses = randomNumber.nextInt(levelQuestion) + randomNumber.nextInt(5);
                newGuessButton.setText(String.valueOf(randomGuesses));

                newGuessButton.setOnClickListener(enterButtonListener);
                currentTableRow.addView(newGuessButton);
            }
        }

        int row = randomNumber.nextInt(guessRows);
        int column = randomNumber.nextInt(3);
        TableRow randomTableRow = getTableRow(row);
        ((Button)randomTableRow.getChildAt(column)).setText(String.valueOf(correctAnswer));

    }

    private int formQuestion(int valueLeft, int valueRight, int result) {

        int questionMark = 1 + randomNumber.nextInt(5);

        if(questionMark % 2 == 0) {

            firstValueTextView.setText(String.valueOf(valueLeft));
            secondValueTextView.setText("?");
            answerTextView.setText(String.format("%d", result));
            return valueRight;
        }

        if(questionMark % 2 == 1) {

            firstValueTextView.setText("?");
            secondValueTextView.setText(String.valueOf(valueRight));
            answerTextView.setText(String.format("%d", result));
            return valueLeft;
        }

        if(questionMark % 2 == 1 && questionMark % 3 == 0) {
            firstValueTextView.setText(String.valueOf(valueLeft));
            secondValueTextView.setText(String.valueOf(valueRight));
            answerTextView.setText("?");
            return  result;
        }
        return 0;
    }

    private TableRow getTableRow(int row) {
        return (TableRow) buttonTableLayout.getChildAt(row);
    }

    private void submitAnswer(Button guessButton) {
        String Answer = guessButton.getText().toString();
        int guess = Integer.parseInt(Answer);
        int answer = correctAnswer;


        if ( guess == answer ) {

            resultTextView.setText(R.string.correct);
            resultTextView.setTextColor(getResources().getColor(R.color.correct_answer));

            disableButton();

            score = ++totalNumberAnswered;
            scoreTextView.setText(String.valueOf(score));

            if (loaded) {
                float leftVolume = volume;
                float rightVolume = volume;

                soundPool.play( this.soundIdWin, leftVolume, rightVolume, 1, 0, 1f);
            }



            if ( totalNumberOfQuestion == totalNumberAnswered ) {

                cancelTimer();

                AlertDialog.Builder builder = new AlertDialog.Builder(WorkItOut.this);

                builder.setTitle("Congratulations!");

                builder.setMessage("You've Won!!!!!!!!");
                builder.setPositiveButton("Move to Next Level", new DialogInterface.OnClickListener(){
                   @Override
                    public  void  onClick(DialogInterface dialog, int which){
                       level += 1;
                       WorkItOut.this.start(level);
                   }
                });

                builder.setNegativeButton("Restart", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        WorkItOut.this.start(level);
                    }
                });

                AlertDialog userWon = builder.create();

                userWon.show();
            }
            else {
                handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                loadQuestion();
                            }
                        }, 1000);
            }
        }
        else {
            resultTextView.setText(R.string.incorrect_answer);
            resultTextView.setTextColor(getResources().getColor(R.color.incorrect_answer));
            guessButton.setEnabled(false);

            cancelTimer();

            if (loaded) {
                float leftVolume = volume;
                float rightVolume = volume;

                soundPool.play( this.soundIdLose, leftVolume, rightVolume, 1, 0, 1f);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(WorkItOut.this);

            builder.setTitle("You Lose!");

            builder.setMessage("You are on the way to Greatness");
            builder.setPositiveButton("Play Level Again", new DialogInterface.OnClickListener(){
                @Override
                public  void  onClick(DialogInterface dialog, int which){

                    WorkItOut.this.start(level);
                }
            });

            builder.setNegativeButton("End Game", new DialogInterface.OnClickListener() {
                @Override
                public  void  onClick(DialogInterface dialog, int which){
                    WorkItOut.this.finish();
                }
            });

            AlertDialog userLose = builder.create();

            userLose.show();
        }
    }

    private void disableButton() {

        for (int row = 0; row < buttonTableLayout.getChildCount(); row++) {

            TableRow tableRow = (TableRow) buttonTableLayout.getChildAt(row);
            for (int i = 0; i < tableRow.getChildCount(); i++ )
                tableRow.getChildAt(i).setEnabled(false);
        }
    }

    public OnClickListener enterButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            submitAnswer((Button) v);
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
       //  int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}
