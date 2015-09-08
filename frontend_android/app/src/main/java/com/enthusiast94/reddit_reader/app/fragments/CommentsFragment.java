package com.enthusiast94.reddit_reader.app.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.enthusiast94.reddit_reader.app.R;
import com.enthusiast94.reddit_reader.app.events.ViewContentEvent;
import com.enthusiast94.reddit_reader.app.models.Comment;
import com.enthusiast94.reddit_reader.app.models.Post;
import com.enthusiast94.reddit_reader.app.network.Callback;
import com.enthusiast94.reddit_reader.app.network.CommentsManager;
import com.enthusiast94.reddit_reader.app.utils.Helpers;
import com.enthusiast94.reddit_reader.app.utils.OnItemSelectedListener;
import com.enthusiast94.reddit_reader.app.utils.TextViewLinkHandler;
import de.greenrobot.event.EventBus;

import java.util.List;

/**
 * Created by manas on 07-09-2015.
 */
public class CommentsFragment extends Fragment {

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private RecyclerView commentsRecyclerView;
    private static final String SELECTED_POST_BUNDLE_KEY = "selected_subreddit_key";

    public static CommentsFragment newInstance(Post selectedPost) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SELECTED_POST_BUNDLE_KEY, selectedPost);
        CommentsFragment commentsFragment = new CommentsFragment();
        commentsFragment.setArguments(bundle);

        return commentsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        /**
         * Find views
         */

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_circular);
        commentsRecyclerView = (RecyclerView) view.findViewById(R.id.comments_recyclerview);

        /**
         * Retrieve selected subreddit from arguments
         */

        final Post selectedPost = getArguments().getParcelable(SELECTED_POST_BUNDLE_KEY);


        /**
         * Setup toolbar
         */

        toolbar.setTitle(selectedPost.getTitle());
        toolbar.setSubtitle(selectedPost.getSubreddit());
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        /**
         * Load comments and set recyclerview adapter
         */

        CommentsManager.getComments(selectedPost.getSubreddit(), selectedPost.getId(), "best", new Callback<List<Comment>>() {

            @Override
            public void onSuccess(List<Comment> data) {
                progressBar.setVisibility(View.INVISIBLE);
                commentsRecyclerView.setVisibility(View.VISIBLE);

                if (getActivity() != null) {
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                    commentsRecyclerView.setAdapter(new CommentsAdapter(getActivity(), data, selectedPost, linearLayoutManager));
                    commentsRecyclerView.setLayoutManager(linearLayoutManager);
                    // disable change animation
                    commentsRecyclerView.getItemAnimator().setSupportsChangeAnimations(false);
                }
            }

            @Override
            public void onFailure(String message) {
                progressBar.setVisibility(View.INVISIBLE);
                commentsRecyclerView.setVisibility(View.INVISIBLE);

                if (getActivity() != null) {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    private static class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnItemSelectedListener {

        private Context context;
        private List<Comment> comments;
        private Post selectedPost;
        private LinearLayoutManager linearLayoutManager;
        private int previouslySelectedPosition;
        private int currentlySelectedPosition;
        private int[] childCommentIndicatorColors;

        public CommentsAdapter(Context context, List<Comment> comments, Post selectedPost, LinearLayoutManager linearLayoutManager) {
            this.context = context;
            this.comments = comments;
            this.selectedPost = selectedPost;
            this.linearLayoutManager = linearLayoutManager;

            previouslySelectedPosition = -1;
            currentlySelectedPosition = -1;

            // retrieve child comment indicator colors
            Resources res = context.getResources();
            childCommentIndicatorColors = new int[]{
                    res.getColor(R.color.teal_500),
                    res.getColor(R.color.blue_500),
                    res.getColor(R.color.purple_500),
                    res.getColor(R.color.light_green_500),
                    res.getColor(R.color.deep_orange_500)
            };
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);

            if (viewType == 0) {
                return new PostsFragment.PostViewHolder(context,
                        inflater.inflate(R.layout.row_posts_recyclerview, parent, false), this);
            } else {
                return new CommentViewHolder(inflater.inflate(R.layout.row_comments_recyclerview, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == 0) {
                ((PostsFragment.PostViewHolder) holder).bindItem(selectedPost, currentlySelectedPosition);
            } else {
                ((CommentViewHolder) holder).bindItem(comments.get(position-1));
            }
        }

        @Override
        public int getItemCount() {
            return 1 + comments.size();
        }

        @Override
        public void onItemSelected(int position) {
            currentlySelectedPosition = position;

            notifyItemChanged(currentlySelectedPosition);
            notifyItemChanged(previouslySelectedPosition);

            previouslySelectedPosition = currentlySelectedPosition;
        }

        private class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private View rootLayout;
            private TextView authorTextView;
            private TextView scoreTextView;
            private TextView createdTextView;
            private TextView bodyTextView;
            private View childCommentIndicator;
            private View buttonsContainerTop;
            private Button nextButton;
            private Button previousButton;
            private View buttonsContainerBottom;

            public CommentViewHolder(View itemView) {
                super(itemView);

                rootLayout = itemView.findViewById(R.id.root_layout);
                authorTextView = (TextView) itemView.findViewById(R.id.author_textview);
                scoreTextView = (TextView) itemView.findViewById(R.id.score_textview);
                createdTextView = (TextView) itemView.findViewById(R.id.created_textview);
                bodyTextView = (TextView) itemView.findViewById(R.id.body_textview);
                childCommentIndicator = itemView.findViewById(R.id.child_comment_indicator);
                buttonsContainerTop = itemView.findViewById(R.id.buttons_container_top);
                nextButton = (Button) buttonsContainerTop.findViewById(R.id.next_parent_comment_button);
                previousButton = (Button) buttonsContainerTop.findViewById(R.id.previous_parent_comment_button);
                buttonsContainerBottom = itemView.findViewById(R.id.buttons_container_bottom);

                // set event listeners
                itemView.setOnClickListener(this);
                nextButton.setOnClickListener(this);
                previousButton.setOnClickListener(this);
                bodyTextView.setOnClickListener(this);
            }

            public void bindItem(Comment comment) {
                authorTextView.setText(comment.getAuthor());
                scoreTextView.setText(comment.getScore() + " " + context.getResources().getString(R.string.label_points));
                createdTextView.setText(comment.getCreated());
                // the inner fromHtml unescapes html entities, while the outer fromHtml returns a formatted Spannable
                bodyTextView.setText(
                        Helpers.trimTrailingWhitespace(Html.fromHtml(Html.fromHtml(comment.getBody()).toString()))
                );
                bodyTextView.setMovementMethod(new TextViewLinkHandler() {
                    @Override
                    public void onLinkClick(String url) {
                        EventBus.getDefault().post(new ViewContentEvent(null, url));
                    }
                });
                if (comment.getLevel() == 0) {
                    childCommentIndicator.setVisibility(View.GONE);
                } else {
                    childCommentIndicator.setVisibility(View.VISIBLE);
                }

                if (getAdapterPosition() == currentlySelectedPosition) {
                    rootLayout.setBackgroundResource(R.color.post_selected_background);
                    buttonsContainerTop.setVisibility(View.VISIBLE);
                    buttonsContainerBottom.setVisibility(View.VISIBLE);
                } else {
                    rootLayout.setBackgroundResource(0);
                    buttonsContainerTop.setVisibility(View.GONE);
                    buttonsContainerBottom.setVisibility(View.GONE);
                }

                if (getAdapterPosition() <= 1) {
                    previousButton.setEnabled(false);
                } else {
                    previousButton.setEnabled(true);
                }

                if (getAdapterPosition() == comments.size()) {
                    nextButton.setEnabled(false);
                } else {
                    nextButton.setEnabled(true);
                }

                // set child comment left spacing based on level
                itemView.setPadding((int) (comment.getLevel() *
                        context.getResources().getDimension(R.dimen.comment_left_spacing)), 0, 0, 0);

                // set child comment indicator color based on level
                childCommentIndicator.setBackgroundColor(childCommentIndicatorColors[comment.getLevel() % 5]);
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.root_layout:
                    case R.id.body_textview:
                        onItemSelected(getAdapterPosition());
                        break;
                    case R.id.next_parent_comment_button:
                        for (int i=getAdapterPosition()+1; i<=comments.size(); i++) {
                            if (comments.get(i-1).getLevel() == 0) {
                                onItemSelected(i);
                                linearLayoutManager.scrollToPositionWithOffset(i, itemView.getTop());
                                break;
                            }
                        }
                        break;
                    case R.id.previous_parent_comment_button:
                        for (int i=getAdapterPosition()-1; i >0; i--) {
                            if (comments.get(i-1).getLevel() == 0) {
                                onItemSelected(i);
                                linearLayoutManager.scrollToPositionWithOffset(i, itemView.getTop());
                                break;
                            }
                        }
                }
            }
        }
    }
}
