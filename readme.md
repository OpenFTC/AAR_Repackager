This tool repacks AAR files into a ZIP with the proper folder structure and metadata files for uploading to Bintray without modifying them in any way.

You can download a prebuilt version from the [releases page](https://github.com/OpenFTC/AAR_Repackager/releases).

```
Usage: java -jar AAR_Repackager.jar [options]
  Options:
  * -a, --artifact
      Artifact name
  * -g, --group
      Group name
  * -i, --input
      AAR input file
  * -o, --output
      ZIP Output file
    -s, --sources
      Sources JAR file (optional)
  * -v, --version
      Artifact version
    -h
      Print help

```