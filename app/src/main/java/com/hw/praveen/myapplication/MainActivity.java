package com.hw.praveen.myapplication;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import  android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Spinner psw_length;
    ProgressDialog pd;
    TextView passwordTxt;

    //parameters for Util.getPassword
    public int passwordLength = -1;
    public boolean passwordNum = false;
    public boolean passwordUpper = false;
    public boolean passwordLower = false;
    public boolean passwordSpecial = false;
    public Bundle bd;
    public Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize the spinner with length values
        psw_length = (Spinner)findViewById(R.id.password_length);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.password_length_array,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        psw_length.setAdapter(adapter);
        psw_length.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                passwordLength = position - 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //The password textfield to display generated password
        passwordTxt = (TextView)findViewById(R.id.password);

        //Prosess dialog to display while tasks are running in background
        pd = new ProgressDialog(this);
        pd.setMessage("Generating Passwords...");
        pd.setCancelable(false);

        //Handler for generate password using thread
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg)
            {
                switch (msg.what)
                {
                    case GenerateThread.STATUS_START:
                        pd.show();
                        break;
                    case GenerateThread.STATUS_STEP:
                        //break;
                    case GenerateThread.STATUS_END:
                        pd.dismiss();
                        //retrieve data from the thread and display
                        final String finalPwd = msg.getData().getString("Password");
                        passwordTxt.setText(finalPwd);
                        break;
                }

                return true;
            }
        });
    }//END of onCreate

    //This method is invoked when the on clicking
    public void genAsynTask(View view){
        //retrieve the current status of checkboxes in order to generate the password
        CheckBox check_num = (CheckBox) findViewById(R.id.check_numbers);
        passwordNum = check_num.isChecked();
        CheckBox check_upp = (CheckBox) findViewById(R.id.check_upp_case);
        passwordUpper = check_upp.isChecked();
        CheckBox check_low = (CheckBox) findViewById(R.id.check_low_case);
        passwordLower = check_low.isChecked();
        CheckBox check_spec = (CheckBox) findViewById(R.id.spec_char);
        passwordSpecial = check_spec.isChecked();

        //if length is not selected from the specified range ask user to select.
        if(passwordLength == -1){
            Toast.makeText(this,"select a length for password",Toast.LENGTH_SHORT).show();
        }
        else {
            //Populate the parameters for getPassword into bundle
            //call asynchronous task only if at least one check box is selected.
            if (passwordSpecial || passwordLower || passwordUpper || passwordNum) {
                bd = new Bundle();
                bd.putInt("passwordLength", passwordLength);
                bd.putBoolean("passwordNum", passwordNum);
                bd.putBoolean("passwordUpper", passwordUpper);
                bd.putBoolean("passwordLower", passwordLower);
                bd.putBoolean("passwordSpecial", passwordSpecial);

                new AsyncTest().execute(bd);
            }
            else{
                //if no check box is selected then ask user to select atleast one.
                Toast.makeText(this, "select at lease one checkbox", Toast.LENGTH_SHORT).show();
            }
        }
    }//END of genAsynTask

    //this method is called on clicking generate password(thread) button is pressed
    public void genThread(View view){
        //retrieve the current status of checkboxes in order to generate the password
        CheckBox check_num = (CheckBox) findViewById(R.id.check_numbers);
        passwordNum = check_num.isChecked();
        CheckBox check_upp = (CheckBox) findViewById(R.id.check_upp_case);
        passwordUpper = check_upp.isChecked();
        CheckBox check_low = (CheckBox) findViewById(R.id.check_low_case);
        passwordLower = check_low.isChecked();
        CheckBox check_spec = (CheckBox) findViewById(R.id.spec_char);
        passwordSpecial = check_spec.isChecked();

        //if length is not selected from the specified range ask user to select.
        if(passwordLength == -1){
            Toast.makeText(this,"select a length for password",Toast.LENGTH_SHORT).show();
        }
        else{
            if (passwordSpecial || passwordLower || passwordUpper || passwordNum) {
                //start a thread to generate password only if at least one check box is selected.
                new Thread(new GenerateThread(passwordLength,passwordNum,passwordUpper,passwordLower,passwordSpecial)).start();
            }
            else
            {
                //if no check box is selected then ask user to select atleast one.
                Toast.makeText(this,"select at lease one checkbox",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Inner class to generate password using asynchronous task
    class AsyncTest extends AsyncTask<Bundle,Integer,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected String doInBackground(Bundle... params) {
            String str =Util.getPassword(bd.getInt("passwordLength"),
                    bd.getBoolean("passwordNum"),
                    bd.getBoolean("passwordUpper"),
                    bd.getBoolean("passwordLower"),
                    bd.getBoolean("passwordSpecial")
            ).toString();
            return str;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            passwordTxt.setText(result);
            pd.dismiss();

        }
    }//EDN AsyncTest class

    //Inner class to generate password using thread
    private class GenerateThread implements Runnable
    {

        final static int  STATUS_START =0x00;
        final static int  STATUS_STEP =0x01;
        final static int  STATUS_END =0x02;
        int len=0;
        boolean num,upl,lc,sc;
        String Password;
        Bundle bundle ;

        public GenerateThread(int l,boolean a,boolean b,boolean c,boolean d)
        {
            len =l;
            num =a;
            upl=b;
            lc=c;
            sc=d;
        }
        @Override
        public void run()
        {
            Message message = new Message();
            message.what = STATUS_START;
            handler.sendMessage(message);
            Password = Util.getPassword(len,num,upl,lc,sc);

            bundle = new Bundle();
            bundle.putString("Password",Password);

            message = new Message();
            message.setData(bundle);
            message.what=STATUS_END;
            handler.sendMessage(message);
        }
    }
}//END of GenerateThread class



