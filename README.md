# Third - Simple Clojure Based Post App

##Author - Max Coursey Vanderbilt Advanced Programming, vu6385
##Professor - Dr. Jules White

##Overview
Based on luminus provided template utilizing re-frame, reagent, buddy.auth, Ring, reitit router, muuantaja (http api module), monger, mongo, selmer, bulma CSS.

Login/Authorization
- Authorization/authentication utilizing buddy.auth module
- Login, Register, Forgot/Reset Password pages

Retrieve Posts/Add New Post
- Post Page - Pull posts from Mongo backend data
- Create Post - allow new creation of posts by authenticated user
  
generated using Luminus version "3.75"


## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein run 
    lein figwheel

## License

Copyright © 2020
