(set-env!
 :dependencies  '[[org.clojure/clojure     "1.8.0"]
                  [boot/core               "2.7.1"]
                  [adzerk/bootlaces        "0.1.13" :scope "test"]
                  [degree9/boot-exec       "0.5.0"]
                  [degree9/boot-semver     "1.4.3"]]
 :resource-paths   #{"src"})

(require
 '[boot.util :as util]
 '[adzerk.bootlaces :refer :all]
 '[degree9.boot-semver :refer :all]
 '[degree9.boot-semgit :refer :all]
 '[degree9.boot-semgit.workflow :refer :all])

(task-options!
 pom {:project 'degree9/boot-semgit
      :version (get-version)
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
            :pre-release 'snapshot
            :generate 'degree9.boot-semgit.version)
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
