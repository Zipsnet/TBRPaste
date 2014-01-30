package org.teamblueridge.paste;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

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


public class CreatePasteActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    TextView pasteUrlLabel;
    EditText pasteNameEditText;
    String pasteNameString;
    EditText pasteContentEditText;
    String pasteContentString;
    String pasteUrlString;
    String userName;
    String uploadUrl;
    String uploadingText;
    SharedPreferences prefs;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_create_paste);

        //Just define the variables here.
        pasteUrlLabel = (TextView) findViewById(R.id.textView4);
        pasteNameEditText = (EditText) findViewById(R.id.editText1);
        pasteNameString = pasteNameEditText.getText().toString();
        pasteContentEditText = (EditText) findViewById(R.id.editText2);
        pasteContentString = pasteContentEditText.getText().toString();
    }

    public void onResume() {
        super.onResume();
        /*if (!prefs.getString("pref_name", "").isEmpty()) {
            userName = prefs.getString("pref_name", "");
        } else {*/
        userName = "Mobile User";
        //}

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, MainActivity.PlaceholderFragment.newInstance(position + 1))
                .commit();

    }

    class uploadPaste extends AsyncTask<String, String, String> {

        // post the content in the background while showing the dialog
        public String doInBackground(String... args) {
            HttpClient httpclient = new DefaultHttpClient();
            uploadUrl = "http://paste.teamblueridge.org/api/create";
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
        public void onPostExecute(String paste_url) {

            runOnUiThread(new Runnable() {
                public void run() {
                    //Create a clickable link from pasteUrlString for user (opens in web browser)
                    String linkText = "<a href=\"" + pasteUrlString + "\">" + pasteUrlString + "</a>";
                    pasteUrlLabel.setText(Html.fromHtml(linkText));
                    pasteUrlLabel.setMovementMethod(LinkMovementMethod.getInstance());

                }
            });
        }
    }


}
