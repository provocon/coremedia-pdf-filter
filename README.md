# PDF Filter Extension for CoreMedia Blueprints

Simple extension to introcude a PDF generating servlet filter.

HTML content for URLs following a certain URI pattern is transformed into PDF.

Since transformation might be too slow and CPU consuming for eager 
regeneration, a simple cachine mechanism is included.

The libraries, this extension depends on, allow for a relaxed licensing
footprint.


## Feedback

Please use the [issues][issues] section of this repository at [github][github]
for feedback.


## Compatibility

CoreMedia Blueprints CMS-9, LiveContext 3, and Content Cloud 10 are supported.

This extension can be integrated into blueprint workspaces starting from
platform version 1707. The latest version in production use is 2010. The latest
available version is meant for 2104. Use the corresponding branch for
integration. Also this README.md is version specific. So switch branches before
reading the integration details.


## Usage

The templates used on pages to be transformed may only output HTML which can
be handled by the [OpenHTMLtoPDF][openhtmltopdf] converter library which in
turn uses Apache PDF Box.

The filter transforms the HTML code if the requested URI matches a pattern
given by regular expressions. The default pattern is `\\?view=pdf`.

The pattern can optionally be changed through the container parameter 
`PDF_FILTER_URI_PATTERN`. (see spring details in the filter class code.)

Caching is triggered, if the HTML code contains the string
`CACHE WITHIN PROVOCON PDF FILTER FOR` followed by a caching time in seconds
like in

```
CACHE WITHIN PROVOCON PDF FILTER FOR 3600s
```

The storage path for cached PDF and HTML can optionally be configured through
the container parameter `PDF_FILTER_CACHE_FOLDER`. (see spring details in the
filter class code.)

## Integration

Add this extension as a git submodule for the extensions directory to your
project's workspace.

```
git submodule add https://github.com/provocon/coremedia-pdf-filter.git apps/cae/modules/extensions/pdf-filter
```

Activate the extension with the CoreMedia extension management tool.

```
mvn extensions:sync -Denable=pdf-filter -f workspace-configuration/extensions/pom.xml
```


## Test

When you make a call to your cae and the PDFFilter was successful integrated,
you can see an HTTP-Response-Header, like this:

```
HTTP/1.1 200
X-PDFFilter: available
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
...
```

For a quick check on a commandline you can use curl (example url): 

```
curl -I http://localhost:8080/blueprint/servlet/corporate-de-de
```


## Operations

From time to time or through your monitoring have a look at the tmp directory
of your CAE components - live or in preview.


## Goals

This extension mostly serves as an internal example of an extension separated
as a git submodule. Thus it serves as a tool to work towards clean workspaces
to ensure maintenance and updatability.


## Legal and Licensing

This extension itself is covered by the license given in the file "LICENSE"
next to this README. The components in use, which are not already part of the
CoreMedia workspace, are

* openhtmltopdf Licensed under the LGPL 2.1
* Apache PDF Box Licensed under the Apache 2.0 License
* Lombok Licensed under the MIT License


[openhtmltopdf]: https://github.com/danfickle/openhtmltopdf
[issues]: https://github.com/provocon/coremedia-pdf-filter/issues
[github]: https://github.com/provocon/coremedia-pdf-filter
[gitlab]: https://gitlab.com/provocon/coremedia-pdf-filter
