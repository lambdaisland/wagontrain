(ns lambdaisland.wagontrain-test
  (:require
   [datomic.api :as d]
   [io.pedestal.log :as log]
   [clojure.test :refer :all]
   [lambdaisland.wagontrain :as wagontrain]))

(def uri "datomic:mem://wagontrain")

(defn create-db []
  (d/create-database uri))

(defn destory-db []
  (d/delete-database uri))

(defn datomic-test-fixture [f]
  (create-db)
  (f)
  (destory-db))

(use-fixtures :each datomic-test-fixture)

(deftest schema-inflate
  (testing "inflate schema"
    (let [thin-schema [[:user/uuid :uuid "Unique user identifier"]
                       [:user/name :string "User name"]
                       [:profile-link/user :ref "User this link belongs too"]]
          fat-schema [{:db/ident :user/uuid,
                       :db/valueType :db.type/uuid,
                       :db/doc "Unique user identifier",
                       :db/cardinality :db.cardinality/one}
                      {:db/ident :user/name,
                       :db/valueType :db.type/string,
                       :db/doc "User name",
                       :db/cardinality :db.cardinality/one}
                      {:db/ident :profile-link/user,
                       :db/valueType :db.type/ref,
                       :db/doc "User this link belongs too",
                       :db/cardinality :db.cardinality/one}]]
      (is (= fat-schema (wagontrain/inflate-schema thin-schema))))))

