(ns congressional-records-app.db
  (:require [cljs.reader]
            [cljs.spec.alpha :as s]
            [re-frame.core :as re-frame]))

;; TODO: remove todo spec
(s/def ::id int?)
(s/def ::title string?)
(s/def ::done boolean?)
(s/def ::todo (s/keys :req-un [::id ::title ::done]))
(s/def ::todos (s/and                                       ;; should use the :kind kw to s/map-of (not supported yet)
                (s/map-of ::id ::todo)                     ;; in this map, each todo is keyed by its :id
                #(instance? PersistentTreeMap %)))           ;; is a sorted-map (not just a map)

(s/def ::showing                                            ;; what todos are shown to the user?
  #{:all                                                    ;; all todos are shown
    :active                                                 ;; only todos whose :done is false
    :done})                                                   ;; only todos whose :done is true

(s/def ::db (s/keys :req-un [::todos ::showing]))

;; -- Default app-db Value  ---------------------------------------------------
;;
;; When the application first starts, this will be the value put in app-db
;; Unless, of course, there are todos in the LocalStore (see further below)
;; Look in:
;;   1.  `core.cljs` for  "(dispatch-sync [:initialise-db])"
;;   2.  `events.cljs` for the registration of :initialise-db handler
;;

(def default-db           ;; what gets put into app-db by default.
  {:todos   (sorted-map)  ;; an empty list of todos. Use the (int) :id as the key
   :showing :all})        ;; show all todos


;; -- Local Storage  ----------------------------------------------------------
;;
;; Part of the todomvc challenge is to store todos in LocalStorage, and
;; on app startup, reload the todos from when the program was last run.
;; But the challenge stipulates to NOT load the setting for the "showing"
;; filter. Just the todos.
;;

(def ls-key "todos-reframe")                         ;; localstore key

(defn todos->local-store
  "Puts todos into localStorage"
  [todos]
  (.setItem js/localStorage ls-key (str todos)))

;; -- cofx Registrations  -----------------------------------------------------

;; Use `reg-cofx` to register a "coeffect handler" which will inject the todos
;; stored in localstore.
;;
;; To see it used, look in `events.cljs` at the event handler for `:initialise-db`.
;; That event handler has the interceptor `(inject-cofx :local-store-todos)`
;; The function registered below will be used to fulfill that request.
;;
;; We must supply a `sorted-map` but in LocalStore it is stored as a `map`.
;;
(re-frame/reg-cofx
 :local-store-todos
 (fn [cofx _]
      ;; put the localstore todos into the coeffect under :local-store-todos
   (assoc cofx :local-store-todos
             ;; read in todos from localstore, and process into a sorted map
          (into (sorted-map)
                (some->> (.getItem js/localStorage ls-key)
                         (cljs.reader/read-string))))))    ;; EDN map -> map
