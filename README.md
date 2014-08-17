# gaffer [![Build Status](https://travis-ci.org/jingweno/gaffer.svg?branch=master)](https://travis-ci.org/jingweno/gaffer)

[Foreman](https://github.com/ddollar/foreman) on JVM. It manages Procfile-based applications.

## Requirement

`gaffer` requires Java 1.7.0 or greater to run and develop.

## Installation

##### Homebrew

If you're running OSX, you could:

```
$ brew tap jingweno/gaffer
$ brew install gaffer
```

##### Precompiled Packages

Download the [latest release](https://github.com/jingweno/gaffer/releases) and unzip it.

##### Compile from Source

To compile `gaffer` from source, you'll need to have [maven](http://maven.apache.org/) installed, and then run:

```
$ mvn package
$ unzip target/gaffer-0.0.1-SNAPSHOT-dist.zip -d target
$ ./target/gaffer-0.0.1-SNAPSHOT-dist/bin/gaffer help
```

## Procfile

A `Procfile` should contain both a name for the process and the command used to run it.
For example:

```
$ cat Procfile
web: bundle exec thin start
job: bundle exec rake jobs:work
```
A process name may contain letters, numbers and the underscore character.

## Usage

```sh
$ gaffer start
$ gaffer start web
$ gaffer start -f Procfile.test -c worker=2
$ gaffer run java -jar target/hello-world-0.0.1-SNAPSHOT.jar server
```

## Implementation

`gaffer` takes advantage of [akka](http://akka.io/)'s [actors](http://en.wikipedia.org/wiki/Actor_model) to [supervise](http://doc.akka.io/docs/akka/snapshot/general/supervision.html) child-processes. More details to come as a blog post.

## Why

`gaffer` does almost the same thing as `foreman`.
However, it empowers existing JVM build tools to manage Procfile-like applications.
To give you a taste on what I'm working on, assuming you declare the following in Maven's `pom.xml`:

```xml
<plugin>
  <groupId>com.owenou</groundId>
  <artifactId>maven-gaffer</artifactId>
  <processes>
    <process>
      <name>web</name>
      <main>web.Main</main>
    </process>
    <process>
      <name>job</name>
      <shell>./bin/job start</shell>
    </process>
  </processes>
</plugin>
```

Running `mvn gaffer:start` will start all the processes associated with your app just as you run it from the `gaffer` CLI.

## Roadmap

* maven plugin
* gradle plugin

## License

See [License.md](https://github.com/jingweno/gaffer/blob/master/LICENSE.md).
