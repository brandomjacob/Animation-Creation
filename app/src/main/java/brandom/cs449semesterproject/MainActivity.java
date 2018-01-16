package brandom.cs449semesterproject;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.DrawableUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity
{
    static public PaintView paintView;
    private static int frame = 0;
    private static int total_frames = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        paintView = (PaintView) findViewById(R.id.paintView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);
        paintView.setDrawingCacheEnabled(true);

        checkReadPermission();
        System.out.println(Environment.getExternalStorageState());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Various options for user in app.
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {


        switch(item.getItemId())
        {
            case R.id.colors:
                Intent choose_color = new Intent(this, ChooseColor.class);
                startActivity(choose_color);
                return true;

            case R.id.eraser:
                paintView.change_color(PaintView.DEFAULT_BG_COLOR);
                return true;

            case R.id.clear:
                Alert_message_clear("Are you sure?");
                return true;

            case R.id.next_frame:
                next_frame();
                return true;

            case R.id.previous_frame:
                previous_frame();
                return true;

            case R.id.export:
                export();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static void MoveFile(String path_source, String path_destination) throws IOException {
        File file_Source = new File(path_source);
        File file_Destination = new File(path_destination);

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(file_Source).getChannel();
            destination = new FileOutputStream(file_Destination).getChannel();

            long count = 0;
            long size = source.size();
            while((count += destination.transferFrom(source, count, size-count))<size);
            file_Source.delete();
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }



    public void export()
    {

        String[] cmd;

        cmd = new String[9];
        cmd[0] = "-f";
        cmd[1] = "image2";
        cmd[2] = "-start_number";
        cmd[3] = "0";
        cmd[4] = "-framerate";
        cmd[5] = "12";
        cmd[6] = "-i";
        cmd[7] = "/data/user/0/brandom.cs449semesterproject/files/%d.png"; //Image sequence
        cmd[8] = "/data/user/0/brandom.cs449semesterproject/files/output10.mp4";



        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {System.out.println(" export starting.");}

                @Override
                public void onProgress(String message) {}

                @Override
                public void onFailure(String message)
                {
                    System.out.println("export Failure.");
                }

                @Override
                public void onSuccess(String message)
                {
                    System.out.println("Export Success");



                    try
                    {

                        MoveFile("/data/user/0/brandom.cs449semesterproject/files/output.mp4", "/output.mp4");
                    }
                    catch(IOException e)
                    {
                        System.out.println(e.getMessage());
                    }


                }

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
        }
    }

    //Required to write to external memory.
    private void checkReadPermission()
    {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    //Selects frame to load..
    public void load_image(int frame_to_load)
    {
        String fileName = (frame_to_load) + ".png";
        Uri uri = Uri.fromFile(getFileStreamPath(fileName));

        System.out.println(uri);

        ImageView imageview = (ImageView) findViewById(R.id.imageView);
        imageview.setImageURI(uri);
        imageview.invalidate();
    }

    //Sends user to next frame or creates new frame if at end of images.
    public void next_frame()
    {
        if (frame < total_frames)
        {
            try
            {
                load_image(++frame);
                paintView.clear();
            }

            catch (NullPointerException e)
            {
                Log.e("load_image()", e.getMessage());
            }
            return;

        }

        Bitmap bm = Bitmap.createBitmap(paintView.getWidth(), paintView.getHeight(), Bitmap.Config.ARGB_8888);
        ImageView imageview = (ImageView)findViewById(R.id.imageView);
        Canvas canvas = new Canvas(bm);
        paintView.draw(canvas);

        if(saveImageToInternalStorage(bm))
        {
            System.out.println("Success");
        }

        if (++frame > total_frames)
        {
            ++total_frames;
        }

        paintView.clear();
        imageview.setImageResource(android.R.color.transparent);
    }

    //Sends user to previous image created.
    public void previous_frame()
    {
        if (frame == 0)
            return;

        try
        {
            load_image(--frame);
            paintView.clear();
        }
        catch (NullPointerException e)
        {
            Log.e("load_image()", e.getMessage());
        }
    }

    //Save to internal storage
    public boolean saveImageToInternalStorage(Bitmap image)
    {

        try
        {
            FileOutputStream fos = this.openFileOutput(frame + ".png", this.MODE_PRIVATE);

            image.compress(Bitmap.CompressFormat.PNG, 60, fos);
            fos.close();

            return true;
        }
        catch (Exception e)
        {
            Log.e("saveToInternalStorage()", e.getMessage());
            return false;
        }
    }

    //Warning message for clearing image.
    public void Alert_message_clear(String title)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
                paintView.clear();   // stop chronometer here

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}