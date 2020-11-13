(ns rickmoynihan.xml
  (:refer-clojure :exclude [update])
  (:require
   [clojure.java.io :as io]
   [clojure.data.xml :as xml]
   [clojure.set :as set]
   [clojure.walk :as walk])
  (:import [java.net URLEncoder]))

(defn build-prefix-codec
  "Given a prefix map of the form {:prefix-key \"http://uri/string\"}
  return a map containing a :prefix-encoder and :prefix-decoder
  function."
  [m]
  (let [->prefix (reduce-kv (fn [acc k v]
                              (assoc acc (str "xmlns." (URLEncoder/encode v)) (name k)))
                            {}
                            m)
        ->base (set/map-invert ->prefix)]

    {:prefix-encoder (fn prefix-encoder
                       [k]
                       (if-let [keyns (->prefix (namespace k))]
                         (keyword keyns (name k))
                         k))
     :prefix-decoder (fn prefix-decoder
                       [k]
                       (let [base (->base (namespace k))]
                         (keyword base (name k))))}))

(defn- tag-val-replacer [tag->val]
  (fn [{:keys [tag content] :as el}]
    (let [replacement-val (tag->val tag ::default)]
      (if (= ::default replacement-val)
        el
        (assoc el :content replacement-val)))))

(defn apply-codec-to-xml [codec xml-data]
  (->> xml-data
       (walk/postwalk (fn [form]
                        (if (and (map? form) (:tag form))
                            (-> form
                                (clojure.core/update :tag codec)
                                (clojure.core/update :attrs #(zipmap (->> % keys (map codec))
                                                                     (->> % vals))))
                            form)))))

(defn apply-transforms [{:keys [transforms]} xml-data]
  xml-data)

(defn update [{:keys [xml-file prefixes] :as opts}]
  (let [{:keys [prefix-encoder prefix-decoder]} (build-prefix-codec prefixes)]

    (->> xml-file
         io/reader
         xml/parse
         (apply-codec-to-xml prefix-encoder)
         (apply-transforms opts)
         (apply-codec-to-xml prefix-decoder))))

(comment

  (def prefixes {:mvn "http://maven.apache.org/POM/4.0.0"
                 :xsi "http://www.w3.org/2001/XMLSchema-instance"})

  (-> "pom.xml"
      io/reader
      (xml/event-seq {}))



  (update {:pom-file "pom.xml"})



(= (xml/parse-str "<foo:title xmlns:foo=\"http://www.w3.org/1999/xhtml2\">Example title</foo:title>")
   (xml/parse-str "<bar:title xmlns:bar=\"http://www.w3.org/1999/xhtml2\">Example title</bar:title>"))

  )
