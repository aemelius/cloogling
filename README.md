# cloogling

Clojure + google + bing

# What is Cloogling?

- I am a command line tool written in Clojure
- Given a search key, I query both Google Custom Search API and Bing Search API, retrieveing the first 10 results from each engine
- I aggregate the results by giving priority to google entries (the last entry returned by google will still come first than the first entry returned by bing)
- In case the two queries share a set of results, I will show you
- I will display a very simple metric of the similarity between the two searches (the percentage of shared results divided by the number of entries returned by google; the result order is not taken into account in this metric)

# What is the output of Cloogling?

Please see the `output_samples` folder.


# How to run Cloogling

With [leiningen](http://leiningen.org/ "Leiningen"), of course.

### Step 1: run the tests

`lein test`

### Step 2: create a `config.json` file

Create a `config.json` file including the credentials and and api-keys to connect to Google and Bing API's. The source includes an example of config file named `config.json.example`.

### Step 3: web cloogling is fun

`lein run --quote-args "chick corea"`

# Notes from the developer's journal

## Learning Clojure

This is my first project in Clojure.

I used the following resources to gather a basic understanding of the language:

- [Try Clojure] (http://www.tryclj.com/ "Try Clojure")
- [Clojure for the Brave and True](http://www.braveclojure.com/ "Clojure for the Brave and True")
  - [Ch. 1](http://www.braveclojure.com/getting-started/ "Ch. 1")
  - [Ch. 3](http://www.braveclojure.com/do-things/       "Ch. 2")
- [Test-driving Clojure in Light Table] (https://www.youtube.com/watch?v=H_teKHH_Rk0 "")

Of course, I also googled a lot (but I did not bing at all!).


## Tools of the craft

- Lighttable
- [Leiningen](http://leiningen.org/ "Leiningen")

# License

Copyright © 2016 Simone Bruno

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

