package com.example.fasic.creitive;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class BlogList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_list);

        Intent intent = this.getIntent();
        String s = intent.getStringExtra("token");

        // TextView tv = (TextView) findViewById(R.id.textView);
        // tv.setText(s);

        fillTable();
    }

    private void fillTable() {

        LinearLayout ll = this.findViewById(R.id.holder);

        View mTableRow = null;
        for(int i = 0 ; i < 10; i ++) {
            mTableRow = View.inflate(this, R.layout.blog_item, null);

            TextView cb = mTableRow.findViewById(R.id.title);
            cb.setText("whatever sdfdsfsdvcxvcxvxcvxcvxcvxcvggggggffffffffggg" + i);

            ImageView imageView = mTableRow.findViewById(R.id.imageView);
            Picasso.get().load("https://www.creitive.com/upload/images/Post/1495547029.9380b/main/thumbnail.jpeg").into(imageView);


            mTableRow.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    startNextActivity();
                }
            });

            mTableRow.setTag(i);

            ll.addView(mTableRow);


        }

    }

    private void startNextActivity(){
        Intent i = new Intent(getBaseContext(), Blog.class);
        startActivity(i);
    }
}
