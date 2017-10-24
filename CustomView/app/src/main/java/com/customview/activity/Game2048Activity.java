package com.customview.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.customview.R;
import com.customview.view.game2048.Game2048Layout;

public class Game2048Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2048);

        final TextView tvScore = (TextView) findViewById(R.id.score);
        final Game2048Layout game = (Game2048Layout) findViewById(R.id.game2048);
        game.setGameListener(new Game2048Layout.Game2048Listener() {
            @Override
            public void onScoreChange(final int score) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvScore.setText(score + "");
                    }
                });
            }

            @Override
            public void onGameOver() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(Game2048Activity.this).setTitle("GAME OVER")
                                .setMessage("YOU HAVE GOT " + tvScore.getText())
                                .setPositiveButton("RESTART", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        game.restart();
                                    }
                                }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                               finish();
                            }
                        }).show();
                    }
                });
            }
        });
        findViewById(R.id.levelUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.levelUp();
            }
        });
        findViewById(R.id.levelDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.levelDown();
            }
        });
    }
}
