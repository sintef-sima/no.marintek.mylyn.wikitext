This repository contains certain enhancements to the [Atlassian Confluence](http://www.atlassian.com/software/confluence/)
support in Mylyn WikiText. It was originally intended to be contributed to the 
[Eclipse](http://www.eclipse.org) [Mylyn](http://www.eclipse.org/mylyn) project
but this could not be done due to the fact that integration with commercial 
products does not fit well within the [Mylyn charter](http://wiki.eclipse.org/Mylyn/Charter).
The related discussion can be found in [Eclipse bug 335280](https://bugs.eclipse.org/bugs/show_bug.cgi?id=335280).

This code is published under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html) (EPL). 
and is thus freely available. 

The enhanced Confluence integration features include:
 
* Two ANT tasks that can be used to produce Eclipse Help and XSL:FO (for further
  transformation into PDF) from a Confluence wiki. 
* In addition to the simple rendering of markup code we've also added support 
  for rendering equations expressed in [LaTeX](http://www.latex-project.org/).
  This is done utilizing [JLaTeXMath](http://forge.scilab.org/index.php/p/jlatexmath/)
  math to do the actual rendering.
* The document outline is used to generate PDF bookmarks which works similar to
  a table of contents.
* Various improvements to rendering of tables and images.

Note that the current version of the plug-in is somewhat limited as certain
issues and improvements must be done in Mylyn Docs in order for the new features
to work. Hopefully these can be resolved soon. The Eclipse bug reports are as follows:

* [336592](https://bugs.eclipse.org/bugs/show_bug.cgi?id=336592): XslfoDocumentBuilder should be able to generate a bookmark tree
* [336683](https://bugs.eclipse.org/bugs/show_bug.cgi?id=336683): XslFoDocumentBuilder should allow for basic page styling
* [336905](https://bugs.eclipse.org/bugs/show_bug.cgi?id=336905): Confluence markup parser should handle table attributes, sub/super-script and escaped characters]
* [336813](https://bugs.eclipse.org/bugs/show_bug.cgi?id=336813): XslFoDocumentBuilder should apply table attributes