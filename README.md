This repository contains Ant tasks for culling an [Atlassian Confluence](http://www.atlassian.com/software/confluence/) Wiki and producing Eclipse Help, EPUB or PDF documents. These tasks are based on the [Mylyn Docs](http://www.eclipse.org/projects/project.php?id=mylyn.docs) Wikitext *mediawiki-to-eclipse-help* task and share much of the same syntax:

* *confluence-to-eclipse-help* - Produces Eclipse Help.
* *confluence-to-pdf* - Produces a PDF file.
* *confluence-to-epub* - Produces an EPUB file.

The EPUB task is a little special. It also supports all features of the Mylyn Docs EPUB task. The syntax is also the same for the EPUB related parts.

Note that version 1.1.0 of Mylyn Docs EPUB is required. This is currently only available in [weekly builds](http://download.eclipse.org/mylyn/snapshots/weekly/).

Features
--------
* Automatically downloads pages and attachments from a Confluence wiki.
* Pages (or chapters) are assembled in the order specified in Confluence.
* Authentication for logging in to the wki.
* Support for LaTeX math expressions. These are converted to PNG images and included into the final result.
* Support for video files; attachments with the *.mp4* suffix are treated as video files and inserted into HTML results using the HTML5 video tag.

Copyright
---------
Where not otherwise noted, this code is Copyright © 2011, 2012 [MARINTEK](http://www.sintef.no/Home/MARINTEK/), The Norwegian Marine Technology Research Institute.

License
-------
This code is published under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html) (EPL). Note
that the [LaTeX](http://en.wikipedia.org/wiki/LaTeX) support is distributed under the [GNU General Public License](http://www.gnu.org/licenses/gpl-2.0.html).

