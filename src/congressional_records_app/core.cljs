(ns congressional-records-app.core
  (:require-macros [secretary.core :refer [defroute]])
  (:require [goog.events :as events]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [secretary.core :as secretary]
            [congressional-records-app.events] ;; These two are only required to make the compiler
            [congressional-records-app.subs]   ;; load them (see docs/Basic-App-Structure.md)
            [congressional-records-app.views]
            [devtools.core :as devtools]
            [day8.re-frame.http-fx])
  (:import [goog History]
           [goog.history EventType]))

; (println "Hello world now with less info")
; (def item secrets/soopersecret)
(devtools/install!)
(enable-console-print!)

;; Put an initial value into app-db.
;; The event handler for `:initialise-db` can be found in `events.cljs`
;; Using the sync version of dispatch means that value is in
;; place before we go onto the next step.
(dispatch-sync [:initialise-db])

;; -- Routes and History ------------------------------------------------------
;; Although we use the secretary library below, that's mostly a historical
;; accident. You might also consider using:
;;   - https://github.com/DomKM/silk
;;   - https://github.com/juxt/bidi
;; We don't have a strong opinion.
;;
(defroute "/" [] (dispatch [:set-showing :all]))
(defroute "/:filter" [filter] (dispatch [:set-showing (keyword filter)]))

(def history
  (doto (History.)
    (events/listen EventType.NAVIGATE
                   (fn [event] (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(r/render [congressional-records-app.views/todo-app]
  (js/document.getElementById  "app"))

;; Must have reload
(defn reload []
  (js/console.log "Reload"))
  ; (def list-of-names (r/atom #{}))
  ; (fetch-names)
