package no.marintek.mylyn.wikitext.confluence.core;

public interface PageMapping {
	/**
	 * provide a relative mapping for the given page name.
	 * 
	 * @param pageName
	 *            the name of the page, for example "Mylyn/User_Guide"
	 * @return a relative or absolute URL, or null if no mapping is available or
	 *         if the default mapping should apply.
	 */
	String mapPageNameToHref(String pageName);
}
