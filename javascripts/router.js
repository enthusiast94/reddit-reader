/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "underscore",
    "backbone",
    "reddit",
    "views/nav",
    "views/nav2",
    "views/posts",
    "views/comments"
], function (_, Backbone, reddit, navView, navView2, postsView, commentsView) {
    "use strict";

    var AppRouter = Backbone.Router.extend({
        routes: {
            "(:param)": "showFrontPage",
            "r/:subreddit/comments/:postId(/:sort)": "showComments",
            "r/:subreddit(/:sort)": "showSubreddit"
        },
        showFrontPage: function (param) {
            param = param != null ? param : "hot";

            if (param.indexOf("access_token=") > -1) {
                param = param.split("&");

                var accessToken = param[0].substring("access_token=".length, param[0].length);
                var state = param[2].substring("state=".length, param[2].length);

                var self = this;
                reddit.auth({
                    accessToken: accessToken,
                    state: state,
                    success: function () {
                        navView.render();
                        navView2.render();
                        self.redirectToFrontPage();
                    }
                });
            } else if (param.indexOf("error=") > -1) {
                param = param.split("&");

                var error = param[1].substring("error=".length, param[1].length);

                alert("Authentication failed: " + error);

                navView.render();
                this.redirectToFrontPage();
            } else {
                var possibleSorts = ["hot", "new", "rising", "controversial", "top"];
                if (possibleSorts.indexOf(param) == -1) {
                    this.redirectToFrontPage();
                } else {
                    var subreddit = "Front page";
                    postsView.render(subreddit, param, localStorage.getItem("user"));
                    navView2.updateCurrentSubreddit(subreddit);
                }
            }
        },
        showSubreddit: function (subreddit, sort) {
            subreddit = subreddit != null ? subreddit : "Front page";
            sort = sort != null ? sort : "hot";

            postsView.render(subreddit, sort, localStorage.getItem("user"));
            navView2.updateCurrentSubreddit(subreddit);
        },
        showComments: function (subreddit, postId, sort) {
            // remove scroll event handler since it's only needed on posts page
            //$(window).unbind("scroll");

            sort = sort != null ? sort : "best";

            commentsView.render(subreddit, postId, sort);
            navView2.updateCurrentSubreddit(subreddit);
        },
        redirectToFrontPage: function () {
            this.navigate("#/");
        }
    });

    var init = function () {
        navView.render();
        navView2.render();

        new AppRouter();

        // begin monitoring hashchange events and dispatching routes
        Backbone.history.start();
    };

    return {
        init: init
    }
});