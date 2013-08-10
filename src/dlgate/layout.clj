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

(defn status-label
  [status]
  [:span {:class (str (condp = status
                        "COMPLETE" "success"
                        "PENDING" "default"
                        "FAILED" "warning")
                      " label")
          :style "float:right;"}
   status])

(defn logged-in
  [account-info & {:keys [alert prev-downloads]}]
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
                   " GB space left on your Copy folder."
                   [:br]
                   (when-not (nil? prev-downloads)
                     [:div
                      [:div {:class "row"}
                       [:h4 "Your most recent downloads"]]
                      (for [download prev-downloads]
                        [:div {:class "row"}
                         [:a {:href (:url download)}
                          (let [filename (:filename download)
                                len (count (:filename download))]
                            (if (> len 25)
                              (str (subs filename 0 10)
                                   "..."
                                   (subs filename
                                         (- len 12) len))
                              (:filename download)))]
                         (status-label (str (:status download)))
                         [:hr]])])]]])))

