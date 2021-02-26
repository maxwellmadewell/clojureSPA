(ns third.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [third.core-test]))

(doo-tests 'third.core-test)

