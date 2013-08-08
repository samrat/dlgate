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
    (include-css "/Gumby/css/gumby.css")
    (include-css "/css/layout.css")]
   [:body
    [:header {:class "row"}
     [:h1 "dlgate"]]
    body]))

(defn alert-box
  [alert-type message]
  (html [:div {:class "row"}
         [:div {:class "four columns"}]
         [:div {:class "four columns"}
          [:div {:class (str alert-type " alert")}
           message]]]))

(defn index
  [& {:keys [alert]}]
  (common "dlgate"
          (html [:div {:clss "twelve colgrid"}
                 (when-not (nil? alert)
                   (alert-box "info" alert))
                 [:div {:class "row"}
                  [:div {:class "three columns"}]
                  [:div {:class "six columns"}
                   "dlgate lets you send files to your Copy.com folder by simply pasting
  a URL. To get started, "
                   [:a {:href "/login"} "login "]
                   " or "
                   [:a {:href "https://copy.com?r=cPK0qZ"} " join Copy.com"]
                   "."]]] )))

(defn free-space
  [account-info]
  (format "%.2f" (float (/ (- (get-in account-info [:storage :quota])
                              (get-in account-info [:storage :used]))
                           (Math/pow 1024 3)))))

(defn logged-in
  [account-info & {:keys [alert]}]
  (common "dlgate"
          (html [:div {:class "twelve colgrid"}
                 (when-not (nil? alert)
                   (alert-box "success" alert))
                 [:div {:class "row"}
                  [:div {:class "three columns"}]
                  [:form {:action "/q"
                          :method "post"
                          :class "append field six columns"}
                   [:input {:type "url"
                            :name "url"
                            :class "xwide url input"
                            :placeholder "Paste URL here"}]
                   [:button {:type "submit"
                             :class "medium primary btn"}
                    [:i {:class "icon-download"}]
                    "Send"]]]

                 [:div {:class "row"}
                  [:div {:class "three columns"}]
                  [:div {:class "six columns"}
                   "You're logged in as "
                   [:strong (:first_name account-info)
                    " "
                    (:last_name account-info)
                    ". "]
                   "You have "
                   (free-space account-info)
                   " GB space left on your Copy folder."]]])))