(set-env!
 :dependencies  '[[org.clojure/clojure     "1.8.0"]
                  [boot/core               "2.7.2"]
                  [degree9/boot-exec       "1.0.0"]
                  [degree9/boot-semver     "1.4.4"]]
 :resource-paths   #{"src"})

(require
 '[boot.util :as util]
 '[degree9.boot-semver :refer :all]
 '[degree9.boot-semgit :refer :all]
 '[degree9.boot-semgit.workflow :refer :all])

(task-options!
 pom {:project 'degree9/boot-semgit
      :description "Semantic Git usage for boot projects."
      :url         "https://github.com/degree9/boot-semgit"
      :scm {:url "https://github.com/degree9/boot-semgit"}}
 target {:dir #{"target"}})

(deftask develop
  "Build boot-semgit for development."
  []
  (comp
   (watch)
   (version :develop true
            :minor 'inc
            :patch 'zero
            :pre-release 'snapshot)
   (target)
   (build-jar)))

(deftask deploy
  "Build boot-semgit and deploy to clojars."
  []
  (comp
   (version)
   (target)
   (build-jar)
   (push-release)))
