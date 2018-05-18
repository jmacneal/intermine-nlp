(defproject intermine-nlp "0.1.0-SNAPSHOT"
  :description "intermine-nlp provides translation from natural (English) language to PathQuery queries."
  :url "http://example.com/FIXME"
  :license {:name "Gnu Lesser General Public License Version 2.1"
            :url "https://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.intermine/imcljs "0.5.1"]
                 [org.clojure/core.async "0.2.395"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 [clojure-opennlp "0.4.0"]
                 ;; [com.zensols.nlp/parse "0.1.4"]
                 ]
  :main ^:skip-aot intermine-nlp.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
