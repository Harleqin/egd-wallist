(ns egd-wallist.core
  (:require [instaparse.core :as instaparse
             :include-macros true]
            [clojure.string :as string]))

(instaparse/defparser wallist-parser
  "wallist = line (<newline> line)*
   <line> = (tagline / comment / player / <empty>)
   empty = #'[^\\r\\n]*'
   newline = #'\\r?\\n'
   whitespace = #'\\s+'
   tagline = <whitespace?> <';'> <whitespace?> tagname <'['> tagcontent <']'> <#'[^\\r\\n]*'>
   tagname = #'[A-Z]+'
   tagcontent = #'[^\\]]*'
   comment = <whitespace?> <';'> <' '?> #'[^\\r\\n]*'
   player = <whitespace?>
            place? <whitespace>
            last-name <whitespace>
            first-name <whitespace>
            rank <whitespace>
            country <whitespace>
            club <whitespace>
            scores <whitespace>
            game-results
            (<whitespace> #'[^\\r\\n]')?
   place = #'\\d+'
   last-name = #'\\S+'
   first-name = #'\\S+'
   rank = #'\\d\\S+'
   country = #'\\S+'
   club = #'\\S+'
   scores = score (<whitespace> score)*
   <score> = #'[\\d]+'
   game-results = game-result (<whitespace> game-result)*
   game-result = opponent-place result (<'/'> (colour handicap?)?)?
   opponent-place = #'\\d+'
   result = '+' | '-' | '=' | '?'
   colour = 'b' | 'w'
   handicap = #'[0-9]'")

(defn ensure-raw-wallist [thing]
  (or (and (vector? thing)
           (= (first thing) :wallist)
           thing)
      nil))

(defn mapify [t & ps]
  (into {:type t} ps))

(defn mapify-tags [{:keys [tag] :as wl}]
  (prn tag)
  (-> wl
      (assoc :tags (into {}
                         (map (fn [{:keys [tagname tagcontent]}]
                                [(keyword nil tagname) tagcontent]))
                         tag))
      (dissoc :tag)))

(defn combine-comments [{:keys [comment] :as wl}]
  (-> wl
      (assoc :comments (string/join \newline (map :comment comment)))
      (dissoc :comment)))

(defn number-players [{:keys [player] :as wl}]
  (-> wl
      (assoc :players (into []
                            (map-indexed (fn [i player]
                                           (assoc player :place (str (inc i)))))
                            player))
      (dissoc :player)))

(defn read-wallist [string]
  (some->> (wallist-parser string)
           ensure-raw-wallist
           (instaparse/transform {:game-result (partial mapify :result)
                                  :game-results (fn [& rs]
                                                  [:game-results (vec rs)])
                                  :scores (fn [& ss]
                                            [:scores (vec ss)])
                                  :player (partial mapify :player)
                                  :tagline (partial mapify :tag)
                                  :comment #(hash-map :type :comment
                                                      :comment %)})
           rest
           (group-by :type)
           mapify-tags
           combine-comments
           number-players))
