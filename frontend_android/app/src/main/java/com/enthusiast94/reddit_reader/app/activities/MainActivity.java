package com.enthusiast94.reddit_reader.app.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.enthusiast94.reddit_reader.app.R;
import com.enthusiast94.reddit_reader.app.fragments.PostsFragment;


public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private String subreddit;
    private String sort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Find views
         */

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        /**
         * Setup AppBar
         */

        setSupportActionBar(toolbar);

        /**
         * Add posts fragment dynamically if it doesn't already exist
         */

        PostsFragment postsFragment = (PostsFragment) getSupportFragmentManager().findFragmentByTag(PostsFragment.TAG);
        if (postsFragment == null) {
            postsFragment = PostsFragment.newInstance(subreddit, sort);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, postsFragment, PostsFragment.TAG)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
