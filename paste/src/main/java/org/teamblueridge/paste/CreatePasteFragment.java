package org.teamblueridge.paste;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class CreatePasteFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_paste, container, false);
    }
    public void onActivityCreated(Bundle bundle){
        Intent startCreatePaste = new Intent(getActivity(), CreatePasteActivity.class);
        startActivity(startCreatePaste);
    }
}
