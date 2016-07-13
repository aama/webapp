(ns webapp.core     
  (:use [jayq.core :only [$]])
  (:require [jayq.core :as jq]))

;;(defn handle-click []
;;  (js/alert " Cf. cljs code in  src/cljs/webapp/core.cljs"))

;;(def clickable (.getElementById js/document "clickable"))

;;(.addEventListener clickable "click" handle-click)

(def $clickable ($ :#clickable))
(def $selection ($ :#selection))

;; following gives "[object Object] has just been selected" (also w/o 'value')
;; w/o 'str' just gives [object Object]
(jq/bind $selection :click (fn [evt] (js/alert (str ($ :#selection.value) " has just been selected"))))
 
(jq/bind $clickable :click (fn [evt] (js/alert "Cf. cljs code in  src/cljs/webapp/core.cljs")))





























