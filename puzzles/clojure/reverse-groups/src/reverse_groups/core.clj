(ns reverse-groups.core
  (:gen-class))
(require '[clojure.string :as str])

; Load in command line: (require 'reverse-groups.core :reload)

;(defn -main
;  "I don't do a whole lot ... yet."
;  [& args]
;  (println (rev-group '(a b c d e f g) 2)))

; Parse a line: "1,2,3,4,5;2"  --> (("1" "2" "3" "4" "5") 2)
(defn parse-prob [prob-string]
  (let [parts (str/split prob-string #";")
        ;no (print parts)
        elts (str/split (first parts) #",")]
    (list elts (read-string (second parts)))))

; Core function
(defn rev-group [elts k]
  (if (< (count elts) k) elts
    (concat (reverse (take k elts)) 
                     (rev-group (take-last (- (count elts) k) elts) k))))

; Top-level function to tie everything together
(defn solve [prob-string]
  (str/join "," (apply rev-group (parse-prob prob-string))))