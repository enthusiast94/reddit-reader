package com.enthusiast94.reddit_reader.app.network;

import com.enthusiast94.reddit_reader.app.App;
import com.enthusiast94.reddit_reader.app.R;
import com.enthusiast94.reddit_reader.app.models.Post;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manas on 03-09-2015.
 */
public class PostsManager {

    private static final String TAG = PostsManager.class.getSimpleName();
    private static final String UNAUTH_API_BASE = "http://www.reddit.com";

    public static void getPosts(String subreddit, String sort, String after, final Callback<List<Post>> callback) {
        // set default values for arguments
        if (sort == null) sort = "hot";

        // build posts url
        String postsUrl = UNAUTH_API_BASE;
        if (subreddit == null) {
            postsUrl += "/" + sort + ".json";
        } else {
            postsUrl += "/r/" + subreddit + "/" + sort + ".json";
        }
        if (after != null) {
            postsUrl += "?after=" + after;
        }

        // send request to server
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.get(postsUrl, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray children = response.getJSONObject("data").getJSONArray("children");

                    List<Post> posts = new ArrayList<Post>();

                    for (int i=0; i<children.length(); i++) {;
                        JSONObject postData = children.getJSONObject(i).getJSONObject("data");

                        Post post = new Post();
                        post.setFullName(postData.getString("name"));
                        post.setLikes(!postData.isNull("likes") ? postData.getBoolean("likes") : null);
                        post.setDomain(postData.getString("domain"));
                        post.setSubreddit(postData.getString("subreddit"));
                        post.setAuthor(postData.getString("author"));
                        post.setScore(postData.getInt("score"));
                        post.setCreated(postData.getString("created_utc"));
                        post.setNsfw(postData.getBoolean("over_18"));
                        post.setThumbnail(postData.getString("thumbnail"));
                        post.setUrl(postData.getString("url"));
                        post.setTitle(postData.getString("title"));
                        post.setNumComments(postData.getInt("num_comments"));
                        post.setPermalink(postData.getString("permalink"));
                        post.setSelftext(postData.getString("selftext"));

                        posts.add(post);
                    }

                    if (callback != null) callback.onSuccess(posts);
                } catch (JSONException e) {
                    if (callback != null)
                        callback.onFailure(App.getAppContext().getResources().getString(R.string.error_parsing));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (callback != null) callback.onFailure(responseString);
            }
        });
    }
}
