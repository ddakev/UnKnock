package kb.hackgsu.daniel.knockboard;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float[] history;
    private float[] tHistory;
    float median;
    float iqr;
    private int dataN;
    private int necdata=100;
    int spike=0;
    private int timestamp;
    ArrayList<Double> spikes;
    double[] proportions;
    Button rp;
    int iqrCoeff;
    int timeBench1,timeBench2;
    public int hWindow, wWindow;
    int trainingTaps;
    CustomDrawableView cdv;
    CustomDrawableView[] cArr;
    TextView title;
    TextView subtitle;

    protected void setUnlocked()
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        TextView accz = (TextView) findViewById(R.id.accZ);
        //accz.setVisibility(View.VISIBLE);
        //accz.setText(String.valueOf("Unlocked"));
        Intent serviceIntent = new Intent(this,KnockLockBackground.class);
        this.startService(serviceIntent);
        this.finish(); // activity gets minimized, service should be invisible
        //this.moveTaskToBack(true);
                        /*new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        System.exit(0);
                                    }
                                },
                                SensorManager.SENSOR_DELAY_NORMAL);*/
    }

    protected void processPattern(String[] pattern)
    {
        proportions = new double[pattern.length-1];
        TextView accx = (TextView) findViewById(R.id.accX);
        accx.setText("");
        for(int i=1; i<pattern.length; i++)
        {
            proportions[i-1]=(Double.parseDouble(pattern[i])-Double.parseDouble(pattern[i-1]))/(Double.parseDouble(pattern[pattern.length-1])-Double.parseDouble(pattern[0]));
            accx.setText(accx.getText()+String.valueOf(proportions[i-1])+" ");
        }
    }

    protected void recordPattern()
    {
        if(rp.getText().equals("Record Pattern")) {
            trainingTaps=0;
            title.setText("Recording Pattern");
            subtitle.setText("Your pattern should be between 4 and 10 knocks");
            RelativeLayout rl = (RelativeLayout) findViewById(R.id.rellay);
            rl.setBackgroundColor(0x7F00F77B);
            for(int i=0; i<=9; i++)
            {
                if(i<=3) cArr[i].setColor(0xFF404041,true);
                else cArr[i].setColor(0xFF404041,false);
                addContentView(cArr[i], new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
            }
            timeBench1=timestamp;
            rp.setText("Waiting for knocks");
            TextView accy = (TextView) findViewById(R.id.accY);
            accy.setText("");
            rp.setEnabled(false);
            spikes.clear();
        }
        else if(rp.getText().equals("Stop Recording"))
        {
            title.setText("Knock 2 Unlock");
            subtitle.setText("Enter the correct knock pattern to unlock device");
            RelativeLayout rl = (RelativeLayout) findViewById(R.id.rellay);
            rl.setBackgroundColor(0x7F304DA1);
            for(int i=0; i<=9; i++)
            {
                ((ViewGroup)cArr[i].getParent()).removeView(cArr[i]);
            }
            timeBench2=timestamp;
            if(Math.abs(spikes.get(0)-timeBench1)<=2) spikes.remove(0);
            if(Math.abs(spikes.get(spikes.size()-1)-timeBench2)<=2) spikes.remove(spikes.size()-1);
            rp.setText("Record Pattern");
            try {
                deleteFile("lockData.txt");
                FileOutputStream outputStream = openFileOutput("lockData.txt", Context.MODE_PRIVATE);
                String data = "";
                for(int i=0; i<spikes.size(); i++) {
                    data += String.valueOf(spikes.get(i));
                    if (i != spikes.size() - 1) data += ",";
                }
                outputStream.write(data.getBytes());
                outputStream.close();
                processPattern(data.split(","));
                spikes.clear();
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView accx = (TextView) findViewById(R.id.accX);
        TextView accy = (TextView) findViewById(R.id.accY);
        TextView accz = (TextView) findViewById(R.id.accZ);
        rp = (Button) findViewById(R.id.button2);
        rp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recordPattern();
            }
        });
        accx.setVisibility(View.INVISIBLE);
        accy.setVisibility(View.INVISIBLE);
        accz.setVisibility(View.INVISIBLE);
        title = (TextView) findViewById(R.id.title);
        subtitle = (TextView) findViewById(R.id.subtitle);
        history = new float[necdata];
        tHistory = new float[necdata];
        dataN = 0;
        timestamp=-1;
        spikes = new ArrayList<Double>();
        iqrCoeff=4;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        String fileName = "lockData.txt";
        try {
            InputStream inputStream = openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString = "";
            String[] stringBuilder = new String[100];
            while ( (receiveString = bufferedReader.readLine()) != null ) {
               stringBuilder=receiveString.split(",");
               processPattern(stringBuilder);
            }
            inputStream.close();
        }
        catch (FileNotFoundException e) {
            recordPattern();
        } catch (IOException e) {
            recordPattern();
        }
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        cdv = new CustomDrawableView(this,0xCCFF6464,125,true);
        cArr = new CustomDrawableView[10];
        for(int i=0; i<=9; i++)
        {
            if(i<=3) cArr[i] = new CustomDrawableView(this,0xFF404041,20,true);
            else cArr[i] = new CustomDrawableView(this,0xFF404041,20,false);
            cArr[i].setXc(cArr[i].getXc() - 270 + i * 60);
            cArr[i].setYc(cArr[i].getYc() * 8 / 5);

            //addContentView(cArr[i], new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        }
        //addContentView(cArr[0], new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        addContentView(cdv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
    }

    private boolean checkLastN()
    {
        int n = proportions.length;
        //proportions = new double[]{0.17f,0.11f,0.05f,0.17f,0.31f,0.19f};
        double[] measured = new double[n];
        double delta = 0.1f;
        double indDelta = 0.05f;
        int s = spikes.size();
        double total=spikes.get(s-1).doubleValue()-spikes.get(s-n-1).doubleValue();
        for(int i=s-n; i<=s-1; i++)
        {
            measured[i-s+n]=(double)(spikes.get(i).doubleValue()-spikes.get(i-1).doubleValue())/total;
        }
        double sum=0;
        for(int i=0; i<=n-1; i++)
        {
            if(Math.abs(proportions[i]-measured[i])>indDelta) return false;
            sum += Math.abs(proportions[i]-measured[i]);
            /*if(Math.abs(proportions[i]-measured[i])>proportions[i]*delta)
                return false;*/
        }

        TextView tv = (TextView) findViewById(R.id.accZ);
        tv.setVisibility(View.VISIBLE);
        /*if(total==0)*/ tv.setText(String.valueOf(sum));
        //else tv.setText(String.valueOf(sum));
        //return false;
        return true;
        //if(sum<delta) return true;
        //else return false;
    }

    public void onSensorChanged(SensorEvent event){
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER)
        {
            timestamp++;
            TextView accx = (TextView) findViewById(R.id.accX);
            TextView accy = (TextView) findViewById(R.id.accY);
            TextView accz = (TextView) findViewById(R.id.accZ);
            if(spike>0&&dataN-1-spike>=10) {
                float max = history[spike];
                int mIndex = spike;
                int i = spike - 1;
                while (Math.abs(history[i] - median) > 1.5 * iqr || Math.abs(history[i - 1] - median) > 1.5 * iqr || Math.abs(history[i + 1] - median) > 1.5 * iqr) {
                    if (max < history[i]) {
                        max = history[i];
                        mIndex = i;
                    }
                    i--;
                }
                spikes.add(new Double((double) tHistory[mIndex]));
                if (rp.getText().equals("Waiting for knocks") && spikes.size() >= 4)
                {
                    rp.setText("Stop Recording");
                    rp.setEnabled(true);
                }
                if (rp.getText().equals("Waiting for knocks") || rp.getText().equals("Stop Recording"))
                {
                    cArr[trainingTaps].setColor(0xFF2DB34A,true);
                    cArr[trainingTaps].invalidate();
                    trainingTaps++;
                    accy.setText(accy.getText() + String.valueOf((double) (spikes.get(spikes.size() - 1))) + " ");
                }
                if (rp.getText().equals("Record Pattern"))
                {
                    //accz.setVisibility(View.VISIBLE);
                    accz.setText(accz.getText() + String.valueOf((double) (spikes.get(spikes.size()-1))) + " ");
                }
                if(rp.getText().equals("Stop Recording")&&spikes.size()>=10)
                {
                    recordPattern();
                }
                if(rp.getText().equals("Record Pattern")) {
                    try {
                        if (proportions.length > 0 && spikes.size() >= proportions.length) {
                            if (checkLastN()) {
                                setUnlocked();
                            }
                        }
                    } catch (Exception e) {
                        //do nothing
                    }
                }
                //accy.setVisibility(View.VISIBLE);
                //accy.setText(String.valueOf((double)(tHistory[i]+tHistory[spike])/2));
                spike=0;
            }
            final float alpha = 0.8f;
            float[] gravity;
            gravity = new float[]{0,0,9.81f};
            float[] linear_acceleration = new float[3];
            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            // Remove the gravity contribution with the high-pass filter.
            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            //set circle radius and color according to net acceleration

            //cdv.refreshDrawableState();
            //((ViewGroup)cdv.getParent()).removeView(cdv);
            //addContentView(cdv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

            if(dataN < necdata)
            {
                history[dataN]=(float)Math.sqrt(linear_acceleration[0]*linear_acceleration[0]+linear_acceleration[1]*linear_acceleration[1]+linear_acceleration[2]*linear_acceleration[2]);
                tHistory[dataN] = timestamp;
                dataN++;
            }
            else
            {
                spike--;
                for(int i=1; i<necdata; i++)
                {
                    history[i-1]=history[i];
                    tHistory[i-1]=tHistory[i];
                }
                history[necdata-1]=(float)Math.sqrt(linear_acceleration[0]*linear_acceleration[0]+linear_acceleration[1]*linear_acceleration[1]+linear_acceleration[2]*linear_acceleration[2]);
                tHistory[necdata-1]=timestamp;
            }
            float[] temp = new float[dataN];
            for(int i=0; i<dataN; i++)
            {
                temp[i]=history[i];
            }
            Arrays.sort(temp);
            median = temp[dataN/2];
            iqr = temp[dataN*3/4]-temp[dataN/4];
            double tempRes=0;
            for(int i=Math.max(0,dataN-20); i<=dataN-1; i++)
            {
                tempRes += (history[i]-median);
            }
            tempRes /= 20;
            tempRes=Math.abs(tempRes);
            int radius=0;
            if(tempRes>=iqr) radius = (int)(Math.log((tempRes/iqr))*50+250);
            else radius = (int)((tempRes/iqr)*50+250);
            cdv.setRadius(radius);
            cdv.invalidate();
            if(history[dataN-1]<median-iqrCoeff*iqr || history[dataN-1]>median+iqrCoeff*iqr)
            {
                spike=dataN-1;
                //accx.setText(String.valueOf(history[dataN-1]));
            }
            else
            {

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

}
