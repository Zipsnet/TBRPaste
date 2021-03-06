package org.teamblueridge.paste;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    TextView pasteUrlLabel;
    TextView urlTextView;
    EditText pasteNameEditText;
    String pasteNameString;
    EditText pasteContentEditText;
    String pasteContentString;
    String pasteUrlString;
    String userName;
    String pasteDomain;
    String uploadUrl;
    String uploadingText;
    String toastText;
    SharedPreferences prefs;
    // Progress Dialog
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Just define the variables here.
        pasteUrlLabel = (TextView) findViewById(R.id.textView4);
        pasteNameEditText = (EditText) findViewById(R.id.editText1);
        pasteNameString = pasteNameEditText.getText().toString();
        pasteContentEditText = (EditText) findViewById(R.id.editText2);
        pasteContentString = pasteContentEditText.getText().toString();
        urlTextView = (TextView) findViewById(R.id.textView3);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

    }

    public void onResume() {
        super.onResume();
        if (!prefs.getString("pref_name", "").isEmpty()) {
            userName = prefs.getString("pref_name", "");
        } else {
            userName = "Mobile User";
        }

        //Hide URL text depending on preferences
        if (!prefs.getBoolean("pref_url", true)) {
            pasteUrlLabel.setVisibility(View.GONE);
            urlTextView.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) urlTextView.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.BELOW, R.id.editText1);
            urlTextView.setLayoutParams(params);
        } else {
            setContentView(R.layout.activity_main);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else {
            if (item.getItemId() == R.id.action_paste) {

                //Upload if paste content is not empty, otherwise show error message
                if (!pasteContentString.isEmpty()) {
                    //Execute paste upload in separate thread
                    new uploadPaste().execute();

                    //Call toast that pasteUrl is copied to the clipboard
                    toastText = getResources().getString(R.string.paste_toast);
                    Context context = getApplicationContext();
                    CharSequence text = toastText;
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(context, text, duration).show();
                } else {
                    //Call toast after error message if URL is hidden
                    if (prefs.getBoolean("pref_url", true)) {
                        pasteUrlLabel.setText(R.string.paste_noText);
                    } else {
                        toastText = getResources().getString(R.string.paste_noText);
                        Context context = getApplicationContext();
                        CharSequence text = toastText;
                        int duration = Toast.LENGTH_SHORT;
                        Toast.makeText(context, text, duration).show();
                    }
                }

                //Clear out the old data in the paste
                pasteNameEditText.setText("");
                pasteContentEditText.setText("");

                return true;
            } else {
                return false;
            }
        }
    }

    //This is the code for the async task used for http traffic
    class uploadPaste extends AsyncTask<String, String, String> {

        //Let the user know that something is happening.
        @Override
        protected void onPreExecute() {
            uploadingText = getResources().getString(R.string.paste_upload);
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage(uploadingText);
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        // post the content in the background while showing the dialog
        protected String doInBackground(String... args) {
            HttpClient httpclient = new DefaultHttpClient();
            if (!prefs.getString("pref_domain", "").isEmpty()) {
                pasteDomain = prefs.getString("pref_domain", "");
            } else {
                pasteDomain = "paste.teamblueridge.org";
            }
            uploadUrl = "http://" + pasteDomain + "/api/create";
            HttpPost httppost = new HttpPost(uploadUrl);
            try {
                // HTTP Header data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("title", pasteNameString));
                nameValuePairs.add(new BasicNameValuePair("text", pasteContentString));
                nameValuePairs.add(new BasicNameValuePair("name", userName));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                InputStream in = response.getEntity().getContent();
                StringBuilder stringbuilder = new StringBuilder();
                BufferedReader bfrd = new BufferedReader(new InputStreamReader(in), 1024);
                String line;
                while ((line = bfrd.readLine()) != null)
                    stringbuilder.append(line);
                pasteUrlString = stringbuilder.toString();
            } catch (ClientProtocolException e) {
                Log.d("TeamBlueridge", e.toString());
            } catch (IOException e) {
                Log.d("TeamBlueridge", e.toString());
            }
            return null;
        }

        //Since we used a dialog, we need to disable it
        protected void onPostExecute(String paste_url) {
            //Dismiss the dialog after getting all products
            pDialog.dismiss();
            //Copy pasteUrl to clipboard
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("TBRPaste", pasteUrlString);
            clipboard.setPrimaryClip(clip);

            //Display paste URL if allowed in preferences
            runOnUiThread(new Runnable() {
                public void run() {
                    //Create a clickable link from pasteUrlString for user (opens in web browser)
                    if (prefs.getBoolean("pref_url", true)) {
                        String linkText = "<a href=\"" + pasteUrlString + "\">" + pasteUrlString + "</a>";
                        pasteUrlLabel.setText(Html.fromHtml(linkText));
                        pasteUrlLabel.setMovementMethod(LinkMovementMethod.getInstance());
                    }

                }
            });
        }
    }

}
