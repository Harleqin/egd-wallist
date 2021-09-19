(ns egd-wallist.core
  (:require [instaparse.core :as instaparse
             :include-macros true]))

(instaparse/defparser wallist-parser
  "wallist = line (<newline> line)*
   <line> = (tagline / comment / player / empty)
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
   score = #'[\\d]+'
   game-results = game-result (<whitespace> game-result)*
   game-result = opponent-place result (<'/'> (colour handicap?)?)?
   opponent-place = #'\\d+'
   result = '+' | '-' | '=' | '?'
   colour = 'b' | 'w'
   handicap = #'[0-9]'")

(defn read-wallist [string]
  (wallist-parser string))
