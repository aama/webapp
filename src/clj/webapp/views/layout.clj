
(ns webapp.views.layout
  (:require [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]
            [ring.util.response :refer [content-type response]]
            [compojure.response :refer [Renderable]]))

(defn utf-8-response [html]
  (content-type (response html) "text/html; charset=utf-8"))

(deftype RenderablePage [content]
  Renderable
  (render [this request]
    (utf-8-response
     (html5
      [:head
       [:title "Welcome to the Afroasiatic Morphological Archive"]
       (include-css "/css/screen.css")
       (include-css "/css/sm-core.css")
       (include-css "/css/sm-simple.css")
       (include-css "/css/dragtable.css")
       (include-js "/js/jquery-1.11.3.min.js")
       (include-js "/js/jquery-ui.min.js")
       (include-js "/js/jquery.smartmenus.min.js")
       (include-js "/js/jquery.dragtable.js")
       (include-js "/js/jquery.tablesorter.min.js")
       [:script {:type "text/javascript"}
        (str 
         "$(document).ready(function() {
            $('.sm').smartmenus({
              showFunction: function($ul, complete) {
                $ul.slideDown(250, complete);
              },
              hideFunction: function($ul, complete) {
                $ul.slideUp(250, complete);
              }
             }); 
             $('#handlerTable').dragtable({dragHandle:'.some-handle'});
             $('#handlerTable').tablesorter();
             // http://www.sanwebe.com/2014/01/how-to-select-all-deselect-checkboxes-jquery
             $('#selectall').click(function(event) {
               if(this.checked) {
                 $('.checkbox1').each(function() {
                  this.checked = true;               
                 });
               }else{
                 $('.checkbox1').each(function() {
                   this.checked = false;                       
                 });         
               }
             });
           });")]
       ;;[:script {:type "text/javascript"} 
       ;; (str "var context=\"" (:context request) "\";")]
       ;;(include-js "//code.jquery.com/jquery-2.0.2.min.js")
       ;;            "/js/colors.js"
       ;;            "/js/site.js")
       ]
      [:body content]
         ;;   <!-- FOOTER  -->
      ;;<div id="footer_wrap" class="outer">
;;	<footer class="inner">
;;          <p>Published with <a href="http://pages.github.com">GitHub Pages</a></p>
;;	</footer>
;;      </div>
;;    </body>
;;  </html>


      ))))

(defn base [& content]
  (RenderablePage. content
                   ))

(defn common [& content]
  (base    
   [:h2#clickable "Afroasiatic Morphological Archive"]
   [:ul {:class "sm sm-simple"}
    [:li (link-to "#" "Home")
     [:ul
      [:li (link-to "/aamaApp" "The AAMA Application")]
      [:li (link-to "/aamapdgmdata" "AAMA Paradigm Data")]
      [:li (link-to "#" "AAMA Languages")
       [:ul
        [:li (link-to "/langInfo" "Alphabetic")]
        [:li (link-to "/langInfoTree" "By Family")]]]
      [:li (link-to "#" "AAMA Bibliography")
       [:ul
        [:li (link-to "/bibInfoMaster" "General Bibliography")]
        [:li (link-to "/bibInfoSpecial" "Bibliography by Key Word")]
        ]]]]
    [:li (link-to "#" "Search/Display")
     [:ul
      [:li (link-to "#" "Archive Paradigm Search/Display")
       [:ul
        [:li (link-to "/pdgmindiv" "Single Paradigm Display")]
        [:li (link-to "/pdgmmultdef" "Multiple Paradigm Display (Default Format) ")]
        [:li (link-to "/pdgmcomblist" "Combine/Modify Paradigm (From List)")]
        [:li (link-to "/pdgmcombtabl" "Combine/modify Paradigm (From Table)")]]]
      [:li (link-to "/formsearch" "User-specified Form/Paradigm Display")]

      [:li (link-to "#" "Morphosyntactic Category Search")
       [:ul
        [:li (link-to "/pvlgpr" "Morphosyntactic Properties")]
        [:li (link-to "/pvlgvl" "Morphosyntactic Values")]
        [:li (link-to "/listlgpr"  "Properties by Language and POS")]
        [:li (link-to "/listlpv" "Morphological Prop-Val Indices by Language")]]]]]
    [:li (link-to "#" "Utilities")
     [:ul 
      [:li (link-to "#" "List and Index Generation:")
       [:ul
        [:li (link-to "/listmenulang" "Language Lists")]
        [:li (link-to "/bibIndexGen" "Bibliography Indices")]
        [:li (link-to "/listmenulpv" "Property/Value Lists")]
        [:li (link-to "/pdgmIndex" "Paradigm Lists")]
        [:li (link-to "/makeschemata" "New Schemata Section for LANG-pdgms.edn")]]]
      [:li (link-to "#" "Update:")
       [:ul 
        [:li (link-to "/update" "Update Local Datastore [Under Development]")]
        [:li (link-to "/upload" "Upload to Remote Repository [Under Development")]]]]]
    [:li (link-to "#" "Help")
     [:ul
      [:li (link-to "#" "The AAMA Application:")
       [:ul 
      [:li (link-to "/helpaamaversions" "AAMA Versions")]
      [:li (link-to "/helpinitializeapp" "How to Initialize A Local Application")]]]
      [:li (link-to "#" "Search/Display")
       [:ul
        [:li (link-to "/helpformsearch" "User-specified Form(set) Search")]
        [:li (link-to "/helppdgms" "Archive Paradigm Search")]
        [:li (link-to "/helppvdisp" "Morphosyntactic Category Search")]]]
      [:li (link-to "#" "Utilities")
       [:ul
        [:li (link-to "/helplistgen" "List Generation")]
        [:li (link-to "#" "Update/Upload")
         [:ul
          [:li (link-to "/helpwebupdate" "Webapp")]
          [:li (link-to "/helpclupdate" "Command Line")]]]]]]]]
   [:div.content content]))

;;(defn common [& content]
;;  (base
;;    [:div#usermenu
;;   [:div.menuitem (link-to "/" "Home")]
;;   [:div.menuitem (link-to "/pdgmpage" "Paradigms")]
;;   [:div.menuitem (link-to "/pvdisp" "Property-value Display")]
;;   [:div.menuitem (link-to "/utilities" "Utilities")]
;;   [:div.menuitem (link-to "/trial" "Trial")]]
;;    [:div.content content]))
