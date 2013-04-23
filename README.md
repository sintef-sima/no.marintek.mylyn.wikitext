This repository contains Ant tasks for reading an [Atlassian Confluence](http://www.atlassian.com/software/confluence/) Wiki and producing Eclipse Help, EPUB or XSL:FO documents. These tasks are based on the [Mylyn Docs](http://www.eclipse.org/projects/project.php?id=mylyn.docs) Wikitext *mediawiki-to-eclipse-help* task and share much of the same syntax:

* [**confluence-to-eclipse-help**](https://github.com/turesheim/no.marintek.mylyn.wikitext/wiki/confluence-to-eclipse-help) - Produces Eclipse Help.
* [**confluence-to-xslfo**](https://github.com/turesheim/no.marintek.mylyn.wikitext/wiki/confluence-to-xslfo) - Produces an XSL:FO file.
* [**confluence-to-epub**](https://github.com/turesheim/no.marintek.mylyn.wikitext/wiki/Confluence-to-EPUB) - Produces an EPUB file.

Please note that Atlassian has [removed the Wiki syntax from the Wiki](http://blogs.atlassian.com/2011/11/why-we-removed-wiki-markup-editor-in-confluence-4/) in Confluence 4! The *confluence-to-\** tasks may still work, but this has not been tested.

The EPUB task is a little special. It also supports all features of the Mylyn Docs EPUB task. The syntax is also the same for the EPUB related parts.

Note that version 1.1.0 of Mylyn Docs EPUB is required. This is currently only available in [weekly builds](http://download.eclipse.org/mylyn/snapshots/weekly/). The most recent release (with Eclipse Juno) is 1.0.0.

For more information on usage; check out the [wiki](https://github.com/turesheim/no.marintek.mylyn.wikitext/wiki).

Features
--------
* Downloads pages and attachments from a Confluence wiki using it's [SOAP](http://en.wikipedia.org/wiki/SOAP) [API](https://developer.atlassian.com/display/CONFDEV/Confluence+XML-RPC+and+SOAP+APIs).
* Pages (or chapters) are assembled in the order specified in Confluence and are downloaded recursivly.
* Authentication for logging in to the wki.
* Support for [LaTeX](http://en.wikipedia.org/wiki/LaTeX) math expressions. These are converted to PNG images and included into the final result.
* Support for video files; attachments with the *.mp4* suffix are treated as video files and inserted into HTML results using the HTML5 *video* tag.

Copyright
---------
The development of code was funded by [MARINTEK](http://www.sintef.no/Home/MARINTEK/) – The Norwegian Marine Technology Research Institute. Consequently; where not otherwise noted, this code is Copyright © 2011, 2012 MARINTEK.

License
-------
This code is published under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html) (EPL). Note
that the [LaTeX](http://en.wikipedia.org/wiki/LaTeX) support is not developed by MARINTEK and is distributed under the [GNU General Public License](http://www.gnu.org/licenses/gpl-2.0.html), see the [license file](no.marintek.mylyn.wikitext/blob/master/org.scilab.forge.jlatexmath/META-INF/LICENSE) for details.