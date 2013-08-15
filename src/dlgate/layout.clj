(ns dlgate.layout
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]))

(def tweet-button
  "<a href='https://twitter.com/share' class='twitter-share-button'
  data-url='http://dlgate.samrat.me' data-text='dlgate- Transfer files
  from the web to your Copy.com folder'>Tweet</a>
  <script>!function(d,s,id){var
  js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+'://platform.twitter.com/widgets.js';fjs.parentNode.insertBefore(js,fjs);}}(document,
  'script', 'twitter-wjs');</script>")

(def flattr
  "<script id='flattrbtn'>(function(i){var
  f,s=document.getElementById(i);f=document.createElement('iframe');f.src='//api.flattr.com/button/view/?uid=samrat&button=compact&url='+encodeURIComponent(document.URL);f.title='Flattr';f.height=20;f.width=110;f.style.borderWidth=0;s.parentNode.insertBefore(f,s);})('flattrbtn');</script>")

(def fb-javascript-sdk
  "<div id='fb-root'></div>
<script>(function(d, s, id) {
  var js, fjs = d.getElementsByTagName(s)[0];
  if (d.getElementById(id)) return;
  js = d.createElement(s); js.id = id;
  js.src = '//connect.facebook.net/en_US/all.js#xfbml=1';
  fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));</script>")

(def fb-button
  "<div class='fb-like' data-href='http://dlgate.samrat.me' data-width='100' data-layout='button_count' data-show-faces='false' data-send='false'></div>")

(def gplus-button
  "<!-- Place this tag where you want the +1 button to render. -->
<div class='g-plusone' data-size='medium'></div>

<!-- Place this tag after the last +1 button tag. -->
<script type='text/javascript'>
  (function() {
    var po = document.createElement('script'); po.type = 'text/javascript'; po.async = true;
    po.src = 'https://apis.google.com/js/plusone.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);
  })();
</script>")

(def google-analytics
  "<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-34425817-3', 'samrat.me');
  ga('send', 'pageview');

</script>")

(defn common
  [title & body]
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]
    [:title title]
    (include-css "/Gumby/css/gumby.css")
    (include-js "http://cdn.filesizejs.com/filesize.min.js")]
   [:body
    fb-javascript-sdk
    [:header {:class "row"}
     [:h1 [:a {:href "/"}
           [:span {:style "color:black;"}
            "dl"] "gate"]]
     [:h4 "Transfer files from the web to your Copy.com folder"]
     [:div {:class "social row"}
      [:div {:class "three columns"}]
      [:table {:class "six columns"}
       [:tr [:td fb-button]
        [:td tweet-button]
        [:td gplus-button]
        [:td flattr]]]]]
    body
    
    [:footer {:class "row"}
     [:div {:class "three columns"}]
     [:div {:class "six columns"}
      [:hr]
      [:h5 "A "
       [:a {:href "http://samrat.me"}
        "Samrat Man Singh"]
       " production. Made in Nepal."]]]

    google-analytics]))

(defn alert-box
  [alert-type message]
  (html [:div {:class "row"}
         [:div {:class "four columns"}]
         [:div {:class "four columns"}
          [:div {:class (str alert-type " alert")}
           message]]]))

(defn index
  [& {:keys [alert]}]
  (common "dlgate- Transfer files from the web to your Copy.com folder"
          (html [:div {:clss "twelve colgrid"}
                 (when-not (nil? alert)
                   (alert-box "info" alert))
                 [:div {:class "row"}
                  [:div {:class "three columns"}]
                  
                  [:form {:action "/save-url"
                          :method "post"
                          :class "append field six columns"}
                   [:input {:type "url"
                            :name "url"
                            :class "xwide url input"
                            :placeholder "Paste URL here"}]
                   [:button {:type "submit"
                             :class "medium primary btn"}
                    "Login"]]]
                 [:div {:class "row"}
                  [:div {:class "three columns"}]
                  [:div {:class "six columns"}
                   "dlgate lets you send files from the web to your Copy.com folder by
  simply pasting a URL. To get started, "
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

(def ga-logged-in
  "<script type=”text/javascript”>
_gaq.push([‘_setCustomVar’, 1, ‘status’, ‘logged_in’, 2]);
</script>")

(defn readable-size
  [bytes]
  (let [bytes (Integer/parseInt bytes)]
    (cond
     (< bytes (* 1024 1024)) (format " (%.2f KB)" (/ bytes 1024.))
     :else (format " (%.2f MB)"
                   (/ bytes
                      (* 1024.0 1024))))))

(defn logged-in
  [account-info & {:keys [alert prev-downloads url]}]
  (common "dlgate- Transfer files from the web to your Copy.com folder"
          (html [:div {:class "twelve colgrid"}
                 (when-not (nil? alert)
                   (alert-box "success" alert))
                 
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
                   " GB space left on your Copy folder."]]
                 [:br]
                 [:div {:class "row"}
                  [:div {:class "three columns"}]
                 
                  [:form {:action "/q"
                          :method "post"
                          :class "append field six columns"}
                   [:input {:type "url"
                            :name "url"
                            :value url
                            :class "xwide url input"
                            :placeholder "Paste URL here"}]
                   [:button {:type "submit"
                             :class "medium primary btn"}
                    [:i {:class "icon-download"}]
                    "Send"]]]

                 [:div {:class "row"}
                  [:div {:class "three columns"}]
                  [:div {:class "six columns"}
                   (when-not (empty? prev-downloads)
                     [:div
                      [:div {:class "row"}
                       [:h4 "Your most recent downloads"]]
                      [:table {:class ""}
                       [:tbody
                        (for [download prev-downloads]
                          [:tr
                           [:td [:a {:href (:url download)}
                                 (let [filename (:filename download)
                                       len (count (:filename download))]
                                   (if (> len 25)
                                     (str (subs filename 0 10)
                                          "..."
                                          (subs filename
                                                (- len 12) len))
                                     (:filename download)))]
                            (when-not (or (= "NA" (:size_bytes download))
                                          (nil? (:size_bytes download)))
                              (readable-size (:size_bytes download)))]
                           [:td
                            (status-label (str (:status download)))]])]]])]]
                 ga-logged-in])))



