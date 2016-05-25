(ns ctia.flows.autoload-test
  (:require [ctia.flows.autoload :as auto]
            [ctia.flows.hooks :as h]
            [ctia.test-helpers.core :as helpers]
            [clojure.test :refer [deftest is join-fixtures use-fixtures]]))

(use-fixtures :once helpers/fixture-schema-validation)

(use-fixtures :each (join-fixtures [helpers/fixture-properties:clean
                                    helpers/fixture-properties:atom-store
                                    helpers/fixture-properties:hook-classes
                                    helpers/fixture-ctia-fast]))

;; -----------------------------------------------------------------------------
;; Test autoloaded hooks

(def obj {:x "x" :y 0 :z {:foo "bar"}})

(deftest check-autoloaded-hooks
  (is (= (h/apply-hooks :entity obj
                        :hook-type :before-create)
         (into obj
               {"autoloaded1 - initialized" "passed-from-autoloaded-jar1"
                "autoloaded2 - initialized" "passed-from-autoloaded-jar2"
                "HookExample1" "Passed in HookExample1"
                "HookExample2" "Passed in HookExample2"})))
  (is (= (h/apply-hooks :entity obj
                        :hook-type :after-create
                        :read-only? true)
         obj)))
