(ns dlgate.layout
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]))

(defn common
  [title & body]
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]
    [:title title]
    ;;(include-css "http://yui.yahooapis.com/pure/0.2.1/pure-min.css")
    ;;(include-css "//netdna.bootstrapcdn.com/font-awesome/3.2.1/css/font-awesome.css")
    (include-css "/css/layout.css")]
   [:body
    [:div {:id "header" :class "pure-g"}
     [:h1 {:class "pure-u-1"}
      "dlgate"]]
    body]))

(defn index
  []
  (common "dlgate"
          (html [:a {:href "/login"} "Login with Copy."])))