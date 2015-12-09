(set-env!
 :source-paths #{"src/clj" "src/cljs" "src/cljc"}
 :resource-paths #{"html"}
 :target-path "target"

 :dependencies '[
                 [org.clojure/clojure "1.7.0"]         ;; add CLJ
                 [org.clojure/clojurescript "1.7.170"] ;; add CLJS
                 [adzerk/boot-cljs "1.7.170-3"]
                 [pandeiro/boot-http "0.7.0"]
                 [adzerk/boot-reload "0.4.2"]
                 [adzerk/boot-cljs-repl "0.3.0"]       ;; add bREPL
                 [com.cemerick/piggieback "0.2.1"]     ;; needed by bREPL 
                 [weasel "0.7.0"]                      ;; needed by bREPL
                 [org.clojure/tools.nrepl "0.2.12"]    ;; needed by bREPL
                 [org.clojars.magomimmo/domina "2.0.0-SNAPSHOT"]
                 [hiccups "0.3.0"]
                 [compojure "1.4.0"]                   ;; for routing
                 [org.clojars.magomimmo/shoreleave-remote-ring "0.3.1"]
                 [org.clojars.magomimmo/shoreleave-remote "0.3.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [org.clojars.magomimmo/valip "0.4.0-SNAPSHOT"]
                 [enlive "1.1.6"]
                 ])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])

;;; testing task: add test/cljc to source-paths for CLJ/CLJS testing porpouse
(deftask testing
  "Add test/cljc for CLJ/CLJS testing purpouse"
  []
  (set-env! :source-paths #(conj % "test/cljc"))
  identity)

;;; add dev task
(deftask dev 
  "Launch immediate feedback dev environment
   
   Available --optimizations levels (default 'none'):
   
  * none         No optimizations. Bypass the Closure compiler completely.
  * whitespace   Remove comments, unnecessary whitespace, and punctuation.
  * simple       Whitespace + local variable and function parameter renaming.
  * advanced     Simple + aggressive renaming, inlining, dead code elimination.

  Source maps can be enabled via the --source-map flag. This provides what the
  browser needs to map locations in the compiled JavaScript to the corresponding
  locations in the original ClojureScript source files."

  [p port          PORT  int  "The port to listen on. (Default: 3000)"
   O optimizations LEVEL kw   "The optimization level"
   s source-map          bool "Create source maps for compiled JS."]
  (let [port (or port 3000)             ;; default 3000
        level (or optimizations :none)  ;; defalut none
        sm (if (= level :none)          ;; with none source-map is always true
             true
             source-map)]
    (comp
     (serve :port port 
            :dir "target"                                
            :handler 'modern-cljs.core/app ;; ring hanlder
            :resource-root "target"        ;; root classpath
            :reload true)                  ;; reload ns
     (watch)
     (reload)
     (cljs-repl) ;; before cljs
     (if sm
       (cljs :source-map sm :optimizations level)
       (cljs :optimizations level)))))
