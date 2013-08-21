# dlgate

Transfer files from the web to your Copy.com folder

## Prerequisites

You will need [Leiningen][1], [Redis](http://redis.io) and
[Postgresql](http://postgresql.org) installed.

[1]: https://github.com/technomancy/leiningen

## Running Locally

### Redis & Postgres
Grab your Copy.com API key and secret and export the `COPY_KEY` and
`COPY_SECRET` environment variables to them.

Run redis-server and create a Postgres database. You might need to
change some configuration(Redis port or Postgres database name)
inside `project.clj` depending on how you set things up.

Then, run:

    lein run
    

## License

Copyright © 2013 Samrat Man Singh
