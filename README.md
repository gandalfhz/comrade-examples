# Comrade examples

This repo contains examples for [comrade](https://github.com/gandalfhz/comrade),
showing how to combine a site and a RESTful API in the same app.

The example handler is
[here](https://github.com/gandalfhz/comrade-examples/blob/master/src/comrade_examples/handler.clj).

## Running

To start a web server for the application, run:

    lein ring server

Visit [http://localhost:3000](http://localhost:3000) in your browser.

## Details

The main example contains a toy example of an app where users can store
keys and values, and where an administrator has the ability to log in
and clear out users' store data.

The example customizes the standard comrade app by passing an additional
authorization function, checking each request against a list of sessions,
in order to be able to expire sessions from the server side.

Additionally, static files with no extension are served up as html files.

## License

Copyright © 2016 Gandalf Hernandez

Distributed under the Apache 2.0 license.
