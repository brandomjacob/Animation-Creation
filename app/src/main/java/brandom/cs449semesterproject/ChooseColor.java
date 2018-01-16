package brandom.cs449semesterproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;



public class ChooseColor extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_color);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Colors to choose from.
    public void Choose_Colors(View view)
    {
        String s = view.getTag().toString();
        switch (s)
        {
            case "BLACK":
                MainActivity.paintView.change_color(Color.BLACK);
                finish();
                return;

            case "RED":
                MainActivity.paintView.change_color(Color.RED);
                finish();
                return;

            case "GREEN":
                MainActivity.paintView.change_color(Color.GREEN);
                finish();
                return;

            case "BLUE":
                MainActivity.paintView.change_color(Color.BLUE);
                finish();
                return;

            case "YELLOW":
                MainActivity.paintView.change_color(Color.YELLOW);
                finish();
                return;
        }

    }

}

