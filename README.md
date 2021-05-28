# biking2

[![Build Status](https://github.com/michael-simons/biking2/workflows/build/badge.svg)](https://github.com/michael-simons/biking2/actions) [![Test coverage](https://sonarcloud.io/api/project_badges/measure?project=eu.michael-simons%3Abiking2&metric=coverage)](https://sonarcloud.io/dashboard?id=eu.michael-simons%3Abiking2) [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=eu.michael-simons%3Abiking2&metric=alert_status)](https://sonarcloud.io/dashboard?id=eu.michael-simons%3Abiking2)

## Abstract

This is a project where i try out Java 8, Spring / Spring Boot and AngularJS. The project is live at [biking.michael-simons.eu][1]. To find out more, checkout the [about page][2] or start reading the series of blog posts:

[Developing a web application with Spring Boot, AngularJS and Java 8][3]

You may also want to have a look at the client companion of this app, [BikingFX][4]

## Architecture 

The projects architecture is [completely documented][6] inside the [arc42][7] structure, using the AsciiDoc format from the [arc42-template][8].

## NOTE

You'll need [GPSBabel][5] to build this project without errors and to use the tracks / log database. Errors running the `TracksControllerTest` are the result of a missing `gpsbabel` binary in the path.

Per default, biking2 looks at `/opt/local/bin/gpsbabel` for the GPSBabel binary. This can be overwritten by adding an application.properties to the root of the project containing the following property:

    biking2.gpsBabel = path/to/gpsbabel/binary

GPSBabel is available for all major operating systems, including windows. So please check if it's available on your platform if biking2 doesn't compile for you.

Also you should use the provided `mvnw` respectively `mvnw.cmd` script for building.
If not, you need to export the following Java opts:

    --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-exports java.base/sun.nio.ch=ALL-UNNAMED

[1]: http://biking.michael-simons.eu
[2]: http://biking.michael-simons.eu/about
[3]: http://info.michael-simons.eu/2014/02/20/developing-a-web-application-with-spring-boot-angularjs-and-java-8/
[4]: https://github.com/michael-simons/bikingFX
[5]: http://www.gpsbabel.org
[6]: http://biking.michael-simons.eu/docs/index.html
[7]: http://arc42.de
[8]: https://github.com/arc42/arc42-template
