package com.customview.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.customview.R;
import com.customview.view.CustomMessageListItem;

public class CustomDrawableStateActivity extends ListActivity {

    private Message[] messages = new Message[]{
            new Message("How about me?", true),
            new Message("Plans are always full", false),
            new Message("Reality ", false),
            new Message("make up to it", false),
            new Message("Come out", true),
            new Message("How about me?", true),
            new Message("You have to step out", false),
            new Message("Baby step", true),
            new Message("How about me?", false),
            new Message("Tai", false),
            new Message("Japan", false),
            new Message("Theta is just fine", true),
            new Message("And Xian", true),
            new Message("Dont hesitate", false),
            new Message("How about me?", false),
            new Message("Oops", true),
            new Message("Be bigger in movement", false),
            new Message("Smaller in words", false),
            new Message("How about me?", false),
            new Message("Wowow~", true),
            new Message("How about me?", false),
            new Message("Youre fine", false),
    };

    private class Message{
        public Message(String text, boolean readed){
            this.text = text;
            this.readed = readed;
        }
        String text;
        boolean readed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getListView().setAdapter(new ArrayAdapter<Message>(this, -1, messages){
            private LayoutInflater layoutInflater = LayoutInflater.from(getContext());

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null){
                    convertView = layoutInflater.inflate(R.layout.custom_drawable_state_item, parent, false);
                }
                final Message message = getItem(position);
                final CustomMessageListItem messageListItem = (CustomMessageListItem) convertView;
                ((TextView) messageListItem.findViewById(R.id.idMsgItemText)).setText(message.text);
                messageListItem.setMessageReaded(message.readed);
                messageListItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (message.readed){
                            Toast.makeText(CustomDrawableStateActivity.this, "Already readed!", Toast.LENGTH_SHORT).show();
                        } else {
                            message.readed = true;
                            messageListItem.setMessageReaded(true);
                        }
                    }
                });
                return convertView;
            }
        });
    }
}
