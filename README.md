This repository contains certain enhancements to the Confluence support in Mylyn
WikiText that could not be committed to the [Eclipse](http://www.eclipse.org) 
repository due to it's integration with a commercial product namely 
[Atlassials Confluence](http://www.atlassian.com/software/confluence/). 

It was originally intended to be contributed to the Mylyn project but this could
not be done due to the fact that integration with commercial products does not 
fit well within the [Mylyn charter](http://wiki.eclipse.org/Mylyn/Charter). The
discussion can be found in [Eclipse bug 335280](https://bugs.eclipse.org/bugs/show_bug.cgi?id=335280).

However this code is published under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html)(EPL). 
and is thus freely available.

The enhanced Confluence integration features include:
 
* Two ANT tasks that can be used to produce Eclipse Help and XSL:FO (for further
  transformation into PDF) from a Confluence wiki. 
* In addition to the simple rendering of markup code we've also added support 
  for rendering equations expressed in [LaTeX](http://www.latex-project.org/). 
* There is a plug-in for Confluence that allows you to do everything LateX so 
  the ANT tasks will attempt to use the markup from that. Note that our support 
  is limited to math expressions, utilizing [JLaTeXMath](http://forge.scilab.org/index.php/p/jlatexmath/)
  math to do the actual job.
