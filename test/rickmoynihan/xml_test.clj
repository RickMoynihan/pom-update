(ns rickmoynihan.xml-test
  (:require [clojure.data.xml :as xml]
            [clojure.java.io :as io]
            [clojure.test :as t :refer [deftest testing is]]
            [rickmoynihan.xml :as sut]))

(def prefixes {:mvn "http://maven.apache.org/POM/4.0.0"
               :xsi "http://www.w3.org/2001/XMLSchema-instance"})

(def some-xml "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">
  <groupId>rickmoynihan</groupId>
  <artifactId>pom</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</project>")

(deftest build-prefix-codec-test
  (testing "Prefix encoding & decoding"
    (let [{:keys [prefix-encoder prefix-decoder]} (sut/build-prefix-codec prefixes)
          xml-data (-> some-xml
                       (xml/parse-str :include-node? #{:element}))
          encoded-data (sut/apply-codec-to-xml prefix-encoder xml-data)]

      (testing "Tags are prefixed"
        (is (= #{:mvn/project :mvn/version :mvn/artifactId :mvn/groupId}
               (set (map
                     :tag
                     (tree-seq map? :content encoded-data))))))

      (testing "Attributes are prefixed"
        (is (= #{:mvn/project :mvn/version :mvn/artifactId :mvn/groupId}
               (set (map
                     :tag
                     (tree-seq map? :content encoded-data))))))

      (testing "Round trip codecs returns original data"
        (is (= xml-data
               (sut/apply-codec-to-xml prefix-decoder
                                       encoded-data)))))))
