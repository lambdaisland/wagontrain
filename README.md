# Wagontrain

**Wagontrain** is a lightweight, idempotent schema migration library for [Datomic](https://www.datomic.com/). It enables **versioned**, **repeatable**, and **reversible** schema and data changes, making it easier to manage schema evolution in production Datomic systems.

[![cljdoc badge](https://cljdoc.org/badge/com.lambdaisland/wagontrain)](https://cljdoc.org/d/com.lambdaisland/wagontrain)
[![Clojars Project](https://img.shields.io/clojars/v/com.lambdaisland/wagontrain.svg)](https://clojars.org/com.lambdaisland/wagontrain)


## Features

- ✅ Apply schema/data migrations **only once** (idempotent)
- 🔁 Easily **rollback** applied migrations
- 🔍 Check if a migration has already been applied
- ⚙️ Support both literal vectors and functions for the `:tx-data` field in each migration.
- 🧠 Provide a **compact DSL** for writing Datomic schemas

## Installation

Add to your `deps.edn` (Clojure CLI):

```
com.lambdaisland/wagontrain {:mvn/version "0.6.15"}
```

Or to your `project.clj` (Leiningen):

```
[com.lambdaisland/wagontrain "0.6.15"]
```

## Getting Started

```
(require '[datomic.api :as d]
         '[lambdaisland.wagontrain :as wagontrain])

(def url "datomic:mem://mydb")
(d/create-database url)
(def conn (d/connect url))

(def migrations
  [{:label :create-user-type
    :tx-data [{:db/ident       :user/type
               :db/valueType   :db.type/string
               :db/cardinality :db.cardinality/one}]}])

(wagontrain/migrate! conn migrations)
```

## Core Concepts

### Idempotency

Each migration must have a unique `:label` (a keyword). Wagontrain ensures that a migration is only applied once, even if `migrate!` is called multiple times.

### tx-data

Each migration accepts `:tx-data` as either:

- a vector of datoms
- a function that returns such a vector

### Rollbacks

In wagontrain, rollback doesn't mean reverting the database state like a traditional RDBMS. Instead, it means creating a new transaction that logically undoes the effects of a previously applied migration, using a Datomic's retract transaction.

A migration can be rolled back if it has been applied `(applied?)`.

Rollback migrations are created automatically using the `rollback!` function.

## API Overview

### migrate!

```
(wagontrain/migrate! conn migrations)
```

Applies migrations in order. Skips any already-applied ones.

### applied?

```
(wagontrain/applied? conn :create-user-type)
;; => true or false
```

Checks whether a migration has already been applied.

### rollback!

```
(wagontrain/rollback! conn :create-user-type)
```

Reverses a previously applied migration.

## inflate-schema

Wagontrain includes a helper function to define schema in a more concise form.

```
(def schema
  [[:user/id :uuid "Unique user ID" :identity]
   [:user/roles :keyword "User roles" :many]
   [:user/profile :ref "User profile" :component]])

(def txes (wagontrain/inflate-schema schema))

@(d/transact conn txes)
```

Supported flags:

- `:many` – sets cardinality to `many`
- `:identity` – sets uniqueness to `:db.unique/identity`
- `:value` – sets uniqueness to `:db.unique/value`
- `:component` – sets `:db/isComponent` to `true`

## Example: Schema + Data Migration

```
(def domain-schema
 "We can use inflate-schema, so domain-schema can be written in a more condensed way."
 [
  [:user/uuid :uuid "Unique user identifier"]
  [:user/contacts :ref "people you connected to"]
  ...
 ])

(def migrations
  [{:label :add-initial-schedule
    :tx-data #(data/load-schedule "compass/schedule.edn")}

   {:label :add-live-set
    :tx-data [{:session.type/name  "Live Set"
               :session.type/color "var(--workshop-color)"
               :db/ident           :session.type/live-set}]}])

(defn init-conn [{:keys [url]}]
  (d/create-database url)
  (let [conn (d/connect url)
        txes (concat (wagontrain/inflate-schema domain-schema) wagontrain/schema)]
    @(transact conn txes) 
    (wagontrain/migrate! conn migrations)
    conn))
```

<!-- opencollective -->

## Documentation

See full API docs on [cljdoc.org](https://cljdoc.org/d/com.lambdaisland/wagontrain)


## Lambda Island Open Source

Thank you! wagontrain is made possible thanks to our generous backers. [Become a
backer on OpenCollective](https://opencollective.com/lambda-island) so that we
can continue to make wagontrain better.

<a href="https://opencollective.com/lambda-island">
<img src="https://opencollective.com/lambda-island/organizations.svg?avatarHeight=46&width=800&button=false">
<img src="https://opencollective.com/lambda-island/individuals.svg?avatarHeight=46&width=800&button=false">
</a>
<img align="left" src="https://github.com/lambdaisland/open-source/raw/master/artwork/lighthouse_readme.png">

&nbsp;

wagontrain is part of a growing collection of quality Clojure libraries created and maintained
by the fine folks at [Gaiwan](https://gaiwan.co).

Pay it forward by [becoming a backer on our OpenCollective](http://opencollective.com/lambda-island),
so that we continue to enjoy a thriving Clojure ecosystem.

You can find an overview of all our different projects at [lambdaisland/open-source](https://github.com/lambdaisland/open-source).

&nbsp;

&nbsp;
<!-- /opencollective -->

<!-- contributing -->
## Contributing

We warmly welcome patches to wagontrain. Please keep in mind the following:

- adhere to the [LambdaIsland Clojure Style Guide](https://nextjournal.com/lambdaisland/clojure-style-guide)
- write patches that solve a problem 
- start by stating the problem, then supply a minimal solution `*`
- by contributing you agree to license your contributions as MPL 2.0
- don't break the contract with downstream consumers `**`
- don't break the tests

We would very much appreciate it if you also

- update the CHANGELOG and README
- add tests for new functionality

We recommend opening an issue first, before opening a pull request. That way we
can make sure we agree what the problem is, and discuss how best to solve it.
This is especially true if you add new dependencies, or significantly increase
the API surface. In cases like these we need to decide if these changes are in
line with the project's goals.

`*` This goes for features too, a feature needs to solve a problem. State the problem it solves first, only then move on to solving it.

`**` Projects that have a version that starts with `0.` may still see breaking changes, although we also consider the level of community adoption. The more widespread a project is, the less likely we're willing to introduce breakage. See [LambdaIsland-flavored Versioning](https://github.com/lambdaisland/open-source#lambdaisland-flavored-versioning) for more info.
<!-- /contributing -->

<!-- license -->
## License

Copyright &copy; 2022-2025 Arne Brasseur and Contributors

Licensed under the term of the Mozilla Public License 2.0, see LICENSE.
<!-- /license -->

