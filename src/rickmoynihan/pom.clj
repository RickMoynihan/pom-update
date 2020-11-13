(ns rickmoynihan.pom
  (:refer-clojure :exclude [update])
  (:require
   [clojure.java.io :as io]
   [clojure.data.xml :as xml]))


(def artifact-id-tag :xmlns.http%3A%2F%2Fmaven.apache.org%2FPOM%2F4.0.0/artifactId)
(def group-id-tag :xmlns.http%3A%2F%2Fmaven.apache.org%2FPOM%2F4.0.0/groupId)
(def version-tag :xmlns.http%3A%2F%2Fmaven.apache.org%2FPOM%2F4.0.0/version)
(def scm-tag :xmlns.http%3A%2F%2Fmaven.apache.org%2FPOM%2F4.0.0/scm)
(def scm-tag-tag :xmlns.http%3A%2F%2Fmaven.apache.org%2FPOM%2F4.0.0/tag)

(defn- tag-val-replacer [tag->transformer el]
  (if (xml/element? el)
    (let [update-fn (get tag->transformer (:tag el) identity)]
      (clojure.core/update el :content update-fn))
    el))

(defn- box [v]
  (fn [old-val]
    (if (some? v)
      [v]
      old-val)))

(defn- build-transforms [{:keys [mvn/version
                                mvn/group-id
                                mvn/artifact-id
                                scm/tag] :as _opts}]
  {version-tag (box version)
   group-id-tag (box group-id)
   artifact-id-tag (box artifact-id)
   scm-tag (fn [content]
             (->> content
                  (mapv (partial tag-val-replacer {scm-tag-tag (box tag)}))))})

(defn- transform-pom
  "Takes a map of options and assoc's a new ::transformed-pom key with
  the transformed pom as a string of xml."
  [{:keys [pom-file
           mvn/version
           mvn/group-id
           mvn/artifact-id
           scm/tag] :as opts}]
  (let [transforms (build-transforms opts)
        t-pom (-> (or pom-file "pom.xml")
                  io/reader
                  xml/parse
                  (clojure.core/update :content
                                       #(mapv (partial tag-val-replacer transforms)
                                             %))
                  xml/emit-str)]
    (assoc opts ::transformed-pom t-pom)))

(defmulti write-pom
  :pom/write-strategy)

(defmethod write-pom :overwrite [{:keys [pom/input-file ::transformed-pom] :as opts}]
  (spit input-file transformed-pom))

(defmethod write-pom :new-file [{:keys [pom/input-file pom/output-file ::transformed-pom] :as opts}]
  (assert (some? output-file) "The :new-file :pom/write-strategy requires :pom/output-file to be specified.")
  (spit output-file transformed-pom))

(def default-opts {:pom/write-strategy :new-file})

(defn update
  "NOTE: This function is suitable and intended for -X invocation using
   the clojure CLI tools.

   Updates pom/input-file by rewriting the corresponding tags in the
  pom. Only a subset of pom keys are currently supported for
  overwriting they are:

   :mvn/version
   :mvn/artifact-id
   :mvn/group-id
   :scm/tag

   Their values are expected to be string literals.

   :pom/write-stragegy  (default-mode :new-file)

   Two modes are supported :overwrite, or :new-file.  If :overwrite is set then
   the modified pom is written over :pom/input-file.  If :new-file is set then
   we expect the :pom/output-file key to specify a new filename to write the
   changed pom to."
  [opts]
  (-> (merge default-opts opts)
      transform-pom
      write-pom))

(comment


  (update {:pom/input-file "pom.xml"
           :pom/write-strategy :new-file
           :pom/output-file "updated-pom.xml"
           :mvn/version "1"
           :scm/tag "1"})

  )
