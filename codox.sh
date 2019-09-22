#!/bin/bash

rm -rf codox
mkdir codox
git clone git@github.com:pmensik/ews-clojure.git codox
cd codox
git symbolic-ref HEAD refs/heads/gh-pages
rm .git/index
git clean -fdx
cd ..

lein codox

cd codox
git add .
git commit -am "Add project documentation"
git push -u origin gh-pages
cd ..
