# PDF Filter Extension for CoreMedia Blueprints

Simple extension to introcude a PDF generating servlet filter.

HTML content for URLs following a certain URI pattern is transformed into PDF.

Since transformation might be too slow and CPU consuming for eager 
regeneration, a simple cachine mechanism is included.

The libraries this extension depends on allow for a relaxed licensing footprint.


## Compatibility

This extension can be integrated into blueprint workspaces starting from
platform version 1707. The latest tested version to be used is 1904. Use the
corresponding branch for integration.

## Usage

The templates used on pages to be transformed may only output HTML which can
be handled by the [OpenHTMLtoPDF][openhtmltopdf] converter library which in turn 
uses Apache PDF Box.

The filter transforms the HTML code if the requested URI matches a pattern
given by regular expressions. The default pattern is `\\?view=pdf`.

Caching is triggered, if the HTML code contains the string
`CACHE WITHIN PROVOCON PDF FILTER FOR` followed by a caching time in seconds
like in

```
CACHE WITHIN PROVOCON PDF FILTER FOR 3600s
```


## Integration

Add this extension as a git submodule for the extensions directory to your
project's workspace.

```
git submodule add https://github.com/provocon/coremedia-pdf-filter.git apps/cae/modules/extensions/pdf-filter
```

Add the root module to `apps/cae/modules/extensions/pom.xml`

```
...
    <module>external-preview</module>
    <module>pdf-filter</module>
    <module>custom-topic-pages</module>
...
```

Add the extension to the list of managed extensions at the end of 
`workspace-config/extensions/managed-extensions.txt` (or the extensions 
collection file in use in your project):

```
echo "pdf-filter" >> workspace-configuration/extensions/managed-extensions.txt
```

Update the extension dependencies via the Coremedia extension tool like with

```
mvn dependency:copy -Dartifact=com.coremedia.tools.extensions:extensions:LATEST:jar:all -DlocalRepositoryDirectory=extensions-tool -Dtransitive=false -DoutputDirectory=tool -Dmdep.stripVersion=true -Dmdep.stripClassifier=true
java -jar tool/extensions.jar --task synchronize --extension-config-file  workspace-configuration/extensions/extension-config.properties --task-input-file workspace-configuration/extensions/managed-extensions.txt
```

You will encounter dependency convergence problem in recent workspaces which
can be resolved in the blueprint-parent pom.

Add in blueprint-parent/pom.xml: (dependencyManagement-Section)

```
      <!-- conflict resolution -->
      <dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox</artifactId>
        <version>2.0.15</version>
      </dependency>
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


## Feedback

Please use the [issues][issues] section of this repository at [github][github] 
for feedback. 

[openhtmltopdf]: https://github.com/danfickle/openhtmltopdf
[issues]: https://github.com/provocon/coremedia-pdf-filter/issues
[github]: https://github.com/provocon/coremedia-pdf-filter
[gitlab]: https://gitlab.com/provocon/coremedia-pdf-filter
