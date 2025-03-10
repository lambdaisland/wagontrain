(ns lambdaisland.wagontrain-test
  (:require
   [datomic.api :as d]
   [io.pedestal.log :as log]
   [clojure.test :refer :all]
   [lambdaisland.wagontrain :as wagontrain]))

(def url "datomic:mem://wagontrain")

(defn create-db []
  (d/create-database url))

(defn destory-db []
  (d/delete-database url))

(defn datomic-test-fixture [f]
  (create-db)
  (f)
  (destory-db))

(use-fixtures :each datomic-test-fixture)

(deftest add-schema
  (testing "add schema"
    (let [target-schema [[:user/uuid :uuid "Unique user identifier"]
                         [:user/name :string "User name"]
                         [:profile-link/user :ref "User this link belongs too"]]
          migrations [{:label :add-init-schema
                       :tx-data (wagontrain/inflate-schema target-schema)}]
          conn (d/connect url)
          _ @(d/transact conn wagontrain/schema)]
      (wagontrain/migrate! conn migrations)
      (is (wagontrain/applied? conn :add-init-schema)))))

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

